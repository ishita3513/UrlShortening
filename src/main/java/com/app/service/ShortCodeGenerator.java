package com.app.service;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String generate(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        StringBuilder builder = new StringBuilder();
        long value = id;
        while (value > 0) {
            int remainder = (int) (value % 62);
            builder.append(ALPHABET.charAt(remainder));
            value /= 62;
        }

        String encoded = builder.reverse().toString();
        if (encoded.length() > 7) {
            encoded = encoded.substring(0, 7);
        } else if (encoded.length() < 7) {
            encoded = String.format("%1$-7s", encoded).replace(' ', '0');
        }
        return encoded;
    }
}
