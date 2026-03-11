package com.java_payfast.java_payfast.service;

import com.java_payfast.java_payfast.config.PayfastConfig;
import com.java_payfast.java_payfast.dto.PaymentFormData;
import com.java_payfast.java_payfast.dto.PaymentRequest;
import com.java_payfast.java_payfast.util.PayfastSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayfastService {

    private final PayfastConfig config;

    public PaymentFormData createPaymentFormData(PaymentRequest request) {
        String paymentId = request.getMPaymentId() != null
                ? request.getMPaymentId()
                : UUID.randomUUID().toString();

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", config.getMerchantId());
        fields.put("merchant_key", config.getMerchantKey());
        fields.put("return_url", config.getReturnUrl());
        fields.put("cancel_url", config.getCancelUrl());
        fields.put("notify_url", config.getNotifyUrl());

        if (request.getNameFirst() != null) fields.put("name_first", request.getNameFirst());
        if (request.getNameLast() != null) fields.put("name_last", request.getNameLast());
        if (request.getEmailAddress() != null) fields.put("email_address", request.getEmailAddress());
        if (request.getCellNumber() != null) fields.put("cell_number", request.getCellNumber());

        fields.put("m_payment_id", paymentId);
        fields.put("amount", request.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("item_name", request.getItemName());

        if (request.getItemDescription() != null) {
            fields.put("item_description", request.getItemDescription());
        }

        String signature = PayfastSignatureUtil.generateSignature(fields, config.getPassphrase());
        fields.put("signature", signature);

        log.info("Payment form data created for payment ID: {}", paymentId);

        return PaymentFormData.builder()
                .actionUrl(config.getProcessUrl())
                .formFields(fields)
                .build();
    }

    public boolean handleItn(LinkedHashMap<String, String> itnData) {
        String receivedSignature = itnData.get("signature");
        if (receivedSignature == null) {
            log.warn("ITN received without signature");
            return false;
        }

        boolean isValid = PayfastSignatureUtil.verifySignature(itnData, config.getPassphrase(), receivedSignature);

        if (!isValid) {
            log.warn("ITN signature verification failed for payment: {}", itnData.get("m_payment_id"));
            return false;
        }

        String paymentStatus = itnData.get("payment_status");
        String paymentId = itnData.get("m_payment_id");
        String pfPaymentId = itnData.get("pf_payment_id");
        String amountGross = itnData.get("amount_gross");

        log.info("ITN received - Payment ID: {}, PF Payment ID: {}, Status: {}, Amount: {}",
                paymentId, pfPaymentId, paymentStatus, amountGross);

        if ("COMPLETE".equalsIgnoreCase(paymentStatus)) {
            log.info("Payment {} completed successfully", paymentId);
            return true;
        }

        log.warn("Payment {} has status: {}", paymentId, paymentStatus);
        return false;
    }
}
