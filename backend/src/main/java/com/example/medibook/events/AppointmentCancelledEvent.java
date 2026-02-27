package com.example.medibook.events;

import java.util.UUID;

public record AppointmentCancelledEvent(UUID appointmentId, String cancelledBy) {}
