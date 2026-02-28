package com.example.medibook.controller;

import com.example.medibook.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class MailDebugController {

    private final MailService mailService;

    @Value("${app.resend.apiKey:}")
    private String resendApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @GetMapping("/mail-status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mailEnabled", mailEnabled);
        status.put("fromEmail", fromEmail);
        status.put("resendApiKeyPresent", resendApiKey != null && !resendApiKey.trim().isEmpty());
        status.put("resendApiKeyLength", resendApiKey != null ? resendApiKey.length() : 0);
        return status;
    }

    @GetMapping("/send-test")
    public Map<String, Object> sendTest(@RequestParam String to) {
        Map<String, Object> result = new HashMap<>();
        try {
            mailService.sendHtml(to, "MediBook Test Email", "<h1>Test</h1><p>This is a test email from MediBook Debugger.</p>");
            result.put("success", true);
            result.put("message", "Attempted to send email to " + to + ". Check server logs for API response.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
