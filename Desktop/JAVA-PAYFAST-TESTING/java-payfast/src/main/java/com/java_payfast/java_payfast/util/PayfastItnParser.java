package com.java_payfast.java_payfast.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public final class PayfastItnParser {

    private PayfastItnParser() {
    }

    public static LinkedHashMap<String, String> parseOrdered(String rawBody) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        if (rawBody == null || rawBody.isBlank()) {
            return map;
        }

        String[] pairs = rawBody.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            map.put(key, value);
        }

        return map;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}