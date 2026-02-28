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
        
        String key = resendApiKey != null ? resendApiKey.trim() : "";
        status.put("resendApiKeyPresent", !key.isEmpty());
        status.put("resendApiKeyLength", key.length());
        
        if (!key.isEmpty() && key.length() > 10) {
            status.put("resendApiKeyMasked", key.substring(0, 5) + "..." + key.substring(key.length() - 4));
        } else {
            status.put("resendApiKeyMasked", "Key too short or missing");
        }
        
        return status;
    }

    @GetMapping("/send-test")
    public Map<String, Object> sendTest(@RequestParam String to) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = mailService.sendHtmlDebug(to, "MediBook Debug Test", 
                "<h1>MediBook Connection Test</h1><p>If you see this, connectivity to Resend is working!</p>");
            result.put("success", true);
            result.put("rawResponse", response);
            result.put("advice", "If status is 403/422, ensure 'fromEmail' is exactly 'onboarding@resend.dev' OR verified in Resend Dashboard.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
