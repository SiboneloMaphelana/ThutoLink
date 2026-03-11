package com.java_payfast.java_payfast.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class PayfastSignatureUtil {

    private PayfastSignatureUtil() {
    }

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

    public static boolean verifySignature(LinkedHashMap<String, String> data,
                                          String passphrase,
                                          String receivedSignature) {
        String calculatedSignature = generateSignature(data, passphrase);

        return MessageDigest.isEqual(
                calculatedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Builds the validation payload exactly as posted, excluding the signature field.
     * Useful for server-to-server ITN confirmation.
     */
    public static String toValidationPayload(LinkedHashMap<String, String> data) {
        return data.entrySet().stream()
                .filter(e -> !"signature".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue() == null ? "" : e.getValue().trim()))
                .collect(Collectors.joining("&"));
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
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}