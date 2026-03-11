package com.java_payfast.java_payfast.service;

import com.java_payfast.java_payfast.config.PayfastConfig;
import com.java_payfast.java_payfast.dto.PaymentFormData;
import com.java_payfast.java_payfast.dto.PaymentRequest;
import com.java_payfast.java_payfast.util.PayfastItnParser;
import com.java_payfast.java_payfast.util.PayfastSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayfastService {

    private final PayfastConfig config;
    private final PayfastIpValidationService ipValidationService;
    private final PayfastItnValidationClient itnValidationClient;
    private final PaymentStore paymentStore;

    public PaymentFormData createPaymentFormData(PaymentRequest request) {
        String paymentId = (request.getMPaymentId() != null && !request.getMPaymentId().isBlank())
                ? request.getMPaymentId().trim()
                : UUID.randomUUID().toString();

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", config.getMerchantId());
        fields.put("merchant_key", config.getMerchantKey());
        fields.put("return_url", config.getReturnUrl());
        fields.put("cancel_url", config.getCancelUrl());
        fields.put("notify_url", config.getNotifyUrl());

        addIfPresent(fields, "name_first", request.getNameFirst());
        addIfPresent(fields, "name_last", request.getNameLast());
        addIfPresent(fields, "email_address", request.getEmailAddress());
        addIfPresent(fields, "cell_number", request.getCellNumber());

        fields.put("m_payment_id", paymentId);
        fields.put("amount", request.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("item_name", request.getItemName());

        addIfPresent(fields, "item_description", request.getItemDescription());

        String signature = PayfastSignatureUtil.generateSignature(fields, config.getPassphrase());
        fields.put("signature", signature);

        paymentStore.save(paymentId, request.getAmount().setScale(2, RoundingMode.HALF_UP));
        log.info("Payment form data created and stored for payment ID: {}", paymentId);

        return PaymentFormData.builder()
                .actionUrl(config.getProcessUrl())
                .formFields(fields)
                .build();
    }

    public boolean handleItn(String rawBody, String remoteIp) {
        LinkedHashMap<String, String> itnData = PayfastItnParser.parseOrdered(rawBody);

        if (itnData.isEmpty()) {
            log.warn("Empty ITN body received");
            return false;
        }

        String receivedSignature = itnData.remove("signature");
        if (receivedSignature == null || receivedSignature.isBlank()) {
            log.warn("ITN received without signature");
            return false;
        }

        boolean signatureValid = PayfastSignatureUtil.verifySignature(
                itnData,
                config.getPassphrase(),
                receivedSignature
        );

        if (!signatureValid) {
            log.warn("ITN signature verification failed for payment: {}", itnData.get("m_payment_id"));
            log.warn("Ordered ITN data used for verification: {}", itnData);
            return false;
        }

        boolean sourceValid = ipValidationService.isValidSource(remoteIp);
        if (!sourceValid) {
            log.warn("ITN source validation failed for payment: {}", itnData.get("m_payment_id"));
            return false;
        }

        boolean serverValidation = itnValidationClient.validateWithPayfast(itnData);
        if (!serverValidation) {
            log.warn("PayFast server validation failed for payment: {}", itnData.get("m_payment_id"));
            return false;
        }

        String merchantId = itnData.get("merchant_id");
        if (!config.getMerchantId().equals(merchantId)) {
            log.warn("Merchant ID mismatch. Expected {}, got {}", config.getMerchantId(), merchantId);
            return false;
        }

        String paymentId = itnData.get("m_payment_id");
        String pfPaymentId = itnData.get("pf_payment_id");
        String paymentStatus = itnData.get("payment_status");
        String amountGross = itnData.get("amount_gross");

        log.info("ITN received - Payment ID: {}, PF Payment ID: {}, Status: {}, Amount: {}",
                paymentId, pfPaymentId, paymentStatus, amountGross);

        BigDecimal expectedAmount = paymentStore.getExpectedAmount(paymentId);

        if (expectedAmount == null) {
            log.warn("No local order found for payment ID: {}", paymentId);
            return false;
        }

        BigDecimal receivedAmount;
        try {
            receivedAmount = new BigDecimal(amountGross).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.warn("Invalid amount_gross received for payment {}: {}", paymentId, amountGross);
            return false;
        }

        if (expectedAmount.setScale(2, RoundingMode.HALF_UP).compareTo(receivedAmount) != 0) {
            log.warn("Amount mismatch for payment {}. Expected {}, got {}",
                    paymentId, expectedAmount, receivedAmount);
            return false;
        }

        if ("COMPLETE".equalsIgnoreCase(paymentStatus)) {
            // TODO:
            // 1. load order by paymentId
            // 2. ensure idempotency (do not process same pfPaymentId twice)
            // 3. mark order paid
            // 4. store full ITN payload for audit
            log.info("Payment {} completed successfully", paymentId);
            return true;
        }

        log.warn("Payment {} has status: {}", paymentId, paymentStatus);
        return false;
    }

    private void addIfPresent(LinkedHashMap<String, String> fields, String key, String value) {
        if (value != null && !value.isBlank()) {
            fields.put(key, value.trim());
        }
    }
}