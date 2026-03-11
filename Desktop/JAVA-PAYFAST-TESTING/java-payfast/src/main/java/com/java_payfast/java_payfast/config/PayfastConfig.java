package com.java_payfast.java_payfast.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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

    public String getProcessUrl() {
        return sandbox ? sandboxUrl : liveUrl;
    }
}
