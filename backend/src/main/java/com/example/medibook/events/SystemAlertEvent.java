package com.example.medibook.events;

import java.util.Map;

public record SystemAlertEvent(
    String type,
    String title,
    String message,
    Map<String, String> details
) {}
