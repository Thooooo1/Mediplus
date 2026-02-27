package com.example.medibook.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringNormalizationUtils {

    public static String toEmailFormat(String fullName) {
        if (fullName == null || fullName.isBlank()) return "user";

        // Remove accents
        String nfdNormalizedString = Normalizer.normalize(fullName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccents = pattern.matcher(nfdNormalizedString).replaceAll("");

        // Replace đ/Đ
        noAccents = noAccents.replace("đ", "d").replace("Đ", "D");

        // Lowercase and trim
        String clean = noAccents.toLowerCase().trim();

        // Split by whitespace
        String[] parts = clean.split("\\s+");
        if (parts.length < 2) return clean + "@gmail.com";

        // Expected format: [last_part].[first_part]@gmail.com
        // Note: For Vietnamese names like "Nguyễn Văn Minh", parts are ["nguyen", "van", "minh"]
        // We want "minh.nguyen@gmail.com"
        String first = parts[parts.length - 1]; // First name
        String last = parts[0];                // Last name/surname
        
        return first + "." + last + "@gmail.com";
    }
}
