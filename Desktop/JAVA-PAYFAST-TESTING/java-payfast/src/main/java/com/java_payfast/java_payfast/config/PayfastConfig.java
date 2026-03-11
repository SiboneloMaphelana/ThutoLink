package com.java_payfast.java_payfast.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payfast")
public class PayfastConfig {

    private String merchantId;
    private String merchantKey;
    private String passphrase;

    private boolean sandbox;
    private String sandboxUrl;
    private String liveUrl;

    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;

    /**
     * Optional but recommended:
     * expected PayFast hostnames/IPs for ITN source validation.
     * Keep this configurable so you can update without code changes.
     */
    private List<String> allowedHosts = new ArrayList<>();

    public String getProcessUrl() {
        return sandbox ? sandboxUrl : liveUrl;
    }

    public String getValidateUrl() {
        String processUrl = getProcessUrl();
        String baseUrl = processUrl.substring(0, processUrl.indexOf("/eng/"));
        return baseUrl + "/eng/query/validate";
    }
}