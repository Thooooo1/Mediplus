package com.example.medibook.dto;

import java.time.Instant;
import java.util.UUID;

public class DoctorDtos {
    public record StatsRes(
        long todayAppointments,
        long pendingAppointments,
        long weekAppointments,
        long totalPatients
    ) {}

    public record PatientRes(
        UUID id,
        String name,
        String email,
        String phone,
        String avatarUrl,
        Instant lastVisit,
        String status,
        String latestNote,
        long appointmentCount
    ) {}

    public record RecordRes(
        UUID id,
        UUID patientId,
        String patientName,
        String diagnosis,
        String notes,
        Instant visitDate,
        String type,
        Instant createdAt
    ) {}

    public record CreateRecordReq(
        UUID patientId,
        String diagnosis,
        String notes,
        String date, // yyyy-MM-dd
        String type
    ) {}

    public record ContactRes(
        UUID patientId,
        String name,
        String avatarUrl,
        String lastMsg,
        Instant time,
        long unread
    ) {}

    public record MessageRes(
        UUID id,
        String from, // "doctor" or "patient"
        String text,
        Instant time
    ) {}

    public record SendMessageReq(
        String text
    ) {}

    public record UpdateProfileReq(
        String fullName,
        String phone,
        String specialty,
        String bio
    ) {}

    public record ProfileRes(
        String fullName,
        String email,
        String phone,
        String specialty,
        String bio
    ) {}

    public record PatientDetailRes(
        UUID id,
        String fullName,
        String email,
        String phone,
        java.util.List<AppointmentDtos.AppointmentRes> appointments,
        java.util.List<RecordRes> records
    ) {}
}
