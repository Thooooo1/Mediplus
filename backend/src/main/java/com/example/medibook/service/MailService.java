package com.example.medibook.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.resend.apiKey:}")
    private String resendApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendHtml(String to, String subject, String htmlContent) {
        if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
            sendViaResend(to, subject, htmlContent);
        } else {
            sendViaSmtp(to, subject, htmlContent);
        }
    }

    /**
     * For Debugging: Returns the actual response body from Resend for analysis.
     */
    public String sendHtmlDebug(String to, String subject, String htmlContent) {
        String cleanKey = resendApiKey != null ? resendApiKey.trim() : "";
        if (!cleanKey.isEmpty()) {
            log.info("[MailDebug] Manual test attempt to: {}", to);
            
            String effectiveFrom = fromEmail;
            if (fromEmail.contains("@gmail.com") || fromEmail.contains("local")) {
                effectiveFrom = "onboarding@resend.dev";
            }

            String jsonBody = String.format("""
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": %s
                }
                """, effectiveFrom, to, subject, escapeJson(htmlContent));

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.resend.com/emails"))
                        .header("Authorization", "Bearer " + cleanKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return String.format("Status: %d | Body: %s | UsedFrom: %s", 
                    response.statusCode(), response.body(), effectiveFrom);
            } catch (Exception e) {
                return "Debug Error: " + e.getMessage();
            }
        }
        return "Resend API Key is missing. Check your environment variables.";
    }


    private void sendViaResend(String to, String subject, String html) {
        String cleanKey = resendApiKey != null ? resendApiKey.trim() : "";
        log.info("[Mail] Attempting to send via Resend API to: {}", to);
        
        // Resilience: If using a personal email as sender for non-verified domains, Resend often fails.
        // We use a safe default if the current sender looks like a personal email or placeholder.
        String effectiveFrom = fromEmail;
        if (fromEmail.contains("@gmail.com") || fromEmail.contains("local") || fromEmail.contains("example.com")) {
            effectiveFrom = "MediBook <onboarding@resend.dev>";
            log.warn("[Mail] Using safe-sender 'onboarding@resend.dev' instead of '{}' for reliability.", fromEmail);
        }

        String jsonBody = String.format("""
            {
                "from": "%s",
                "to": ["%s"],
                "subject": "%s",
                "html": %s
            }
            """, 
            effectiveFrom, 
            to, 
            subject, 
            escapeJson(html)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + cleanKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[Mail] Resend Response: Status={}, Body={}", response.statusCode(), response.body());
            
            if (response.statusCode() == 401) {
                log.error("[Mail] 401 Unauthorized: The RESEND_API_KEY is definitively invalid. Please check Render Environment Variables.");
            } else if (response.statusCode() >= 400) {
                log.error("[Mail] Resend Error: {}. Ensure from address is verified or use onboarding@resend.dev", response.body());
            }
        } catch (Exception e) {
            log.error("[Mail] Resend API failed: {}. Falling back to SMTP if possible...", e.getMessage());
        }
    }

    private void sendViaSmtp(String to, String subject, String html) {
        log.info("[Mail] Attempting to send via SMTP (Google) to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[Mail] SMTP Send Success to: {}", to);
        } catch (Exception e) {
            log.error("[Mail] SMTP Send failed to {}: {}. Note: SMTP is often blocked on Render.", to, e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '/' -> sb.append("\\/");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 32 || c > 126) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u").append("0".repeat(4 - hex.length())).append(hex);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
