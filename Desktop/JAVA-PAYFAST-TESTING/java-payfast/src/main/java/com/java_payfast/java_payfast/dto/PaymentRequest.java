package com.java_payfast.java_payfast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String nameFirst;
    private String nameLast;
    private String emailAddress;
    private String cellNumber;
    private BigDecimal amount;
    private String itemName;
    private String itemDescription;
    private String mPaymentId;
}
