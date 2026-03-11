package com.java_payfast.java_payfast.service;

import com.java_payfast.java_payfast.config.PayfastConfig;
import com.java_payfast.java_payfast.util.PayfastSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayfastItnValidationClient {

    private final PayfastConfig config;
    private final RestClient restClient = RestClient.builder().build();

    public boolean validateWithPayfast(LinkedHashMap<String, String> itnData) {
        String payload = PayfastSignatureUtil.toValidationPayload(itnData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.List.of(MediaType.TEXT_PLAIN));

        try {
            String response = restClient.post()
                    .uri(config.getValidateUrl())
                    .headers(h -> h.addAll(headers))
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.info("PayFast validation response: {}", response);
            return response != null && response.trim().equalsIgnoreCase("VALID");
        } catch (Exception e) {
            log.error("Error validating ITN with PayFast", e);
            return false;
        }
    }
}
