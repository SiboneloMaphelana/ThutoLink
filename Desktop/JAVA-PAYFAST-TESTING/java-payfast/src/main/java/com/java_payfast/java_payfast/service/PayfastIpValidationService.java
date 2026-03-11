package com.java_payfast.java_payfast.service;

import com.java_payfast.java_payfast.config.PayfastConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayfastIpValidationService {

    private final PayfastConfig config;

    public boolean isValidSource(String remoteIp) {
        if (remoteIp == null || remoteIp.isBlank()) {
            return false;
        }

        if (config.getAllowedHosts() == null || config.getAllowedHosts().isEmpty()) {
            log.warn("No PayFast allowedHosts configured. Source validation is disabled.");
            return true; // change to false if you want hard enforcement
        }

        Set<String> allowedIps = new HashSet<>();

        for (String host : config.getAllowedHosts()) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress address : addresses) {
                    allowedIps.add(address.getHostAddress());
                }
            } catch (Exception e) {
                log.warn("Could not resolve PayFast host: {}", host, e);
            }
        }

        boolean valid = allowedIps.contains(remoteIp);
        if (!valid) {
            log.warn("ITN source IP {} is not in resolved PayFast IP list {}", remoteIp, allowedIps);
        }

        return valid;
    }
}