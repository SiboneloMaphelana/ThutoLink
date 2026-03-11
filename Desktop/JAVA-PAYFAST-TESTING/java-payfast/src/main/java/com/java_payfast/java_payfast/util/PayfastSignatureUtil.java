package com.java_payfast.java_payfast.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PayfastSignatureUtil {

    private PayfastSignatureUtil() {}

    public static String generateSignature(LinkedHashMap<String, String> data, String passphrase) {
        String paramString = data.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue().trim()))
                .collect(Collectors.joining("&"));

        if (passphrase != null && !passphrase.isBlank()) {
            paramString += "&passphrase=" + urlEncode(passphrase.trim());
        }

        return md5(paramString);
    }

    public static boolean verifySignature(Map<String, String> data, String passphrase, String receivedSignature) {
        LinkedHashMap<String, String> filteredData = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (!"signature".equals(key) && value != null && !value.isBlank()) {
                filteredData.put(key, value);
            }
        });

        String calculatedSignature = generateSignature(filteredData, passphrase);
        return calculatedSignature.equals(receivedSignature);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
