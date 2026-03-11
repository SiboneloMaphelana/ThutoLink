package com.java_payfast.java_payfast.controller;

import com.java_payfast.java_payfast.dto.PaymentFormData;
import com.java_payfast.java_payfast.dto.PaymentRequest;
import com.java_payfast.java_payfast.service.PayfastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

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

    @PostMapping("/notify")
    public ResponseEntity<String> handleItn(@RequestParam Map<String, String> itnParams) {
        log.info("ITN notification received");
        LinkedHashMap<String, String> orderedParams = new LinkedHashMap<>(itnParams);
        boolean success = payfastService.handleItn(orderedParams);
        return success ? ResponseEntity.ok("OK") : ResponseEntity.badRequest().body("FAILED");
    }
}
