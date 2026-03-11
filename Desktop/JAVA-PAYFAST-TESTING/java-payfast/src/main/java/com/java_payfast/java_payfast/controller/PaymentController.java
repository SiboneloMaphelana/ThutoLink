package com.java_payfast.java_payfast.controller;

import com.java_payfast.java_payfast.dto.PaymentFormData;
import com.java_payfast.java_payfast.dto.PaymentRequest;
import com.java_payfast.java_payfast.service.PayfastService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PayfastService payfastService;

    @PostMapping("/create")
    public ResponseEntity<PaymentFormData> createPayment(@RequestBody PaymentRequest request) {
        log.info("Creating payment for item: {}", request.getItemName());
        PaymentFormData formData = payfastService.createPaymentFormData(request);
        return ResponseEntity.ok(formData);
    }

    @PostMapping(
            value = "/notify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<String> handleItn(@RequestBody String rawBody, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        log.info("ITN notification received from IP: {}", remoteAddr);

        boolean success = payfastService.handleItn(rawBody, remoteAddr);

        if (!success) {
            log.warn("ITN processing failed, but returning 200 to acknowledge receipt");
        }

        return ResponseEntity.ok(success ? "OK" : "FAILED");
    }
}