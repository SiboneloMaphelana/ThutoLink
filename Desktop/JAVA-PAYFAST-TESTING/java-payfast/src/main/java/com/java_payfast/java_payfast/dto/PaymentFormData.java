package com.java_payfast.java_payfast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFormData {

    private String actionUrl;
    private Map<String, String> formFields;
}
