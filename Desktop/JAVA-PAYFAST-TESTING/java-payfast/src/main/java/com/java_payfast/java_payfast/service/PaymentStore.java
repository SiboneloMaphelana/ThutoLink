package com.java_payfast.java_payfast.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentStore {

    private final ConcurrentHashMap<String, BigDecimal> payments = new ConcurrentHashMap<>();

    public void save(String paymentId, BigDecimal amount) {
        payments.put(paymentId, amount);
    }

    public BigDecimal getExpectedAmount(String paymentId) {
        return payments.get(paymentId);
    }

    public boolean exists(String paymentId) {
        return payments.containsKey(paymentId);
    }
}
