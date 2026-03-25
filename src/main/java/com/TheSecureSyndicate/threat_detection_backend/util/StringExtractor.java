package com.TheSecureSyndicate.threat_detection_backend.util;

import java.util.ArrayList;
import java.util.List;

public class StringExtractor {

    public static List<String> extractStrings(byte[] fileBytes, int minLength) {
        List<String> strings = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (byte b : fileBytes) {
            char c = (char) b;
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || isSymbol(c)) {
                current.append(c);
            } else {
                if (current.length() >= minLength) {
                    strings.add(current.toString());
                }
                current.setLength(0);
            }
        }

        if (current.length() >= minLength) {
            strings.add(current.toString());
        }

        return strings;
    }

    private static boolean isSymbol(char c) {
        return "!@#$%^&*()-_=+[]{}|;:',.<>/?".indexOf(c) >= 0;
    }
}
