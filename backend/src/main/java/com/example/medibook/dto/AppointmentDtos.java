package com.example.medibook.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class AppointmentDtos {

  public record BookReq(@NotNull UUID timeSlotId, String note) {}

  public record AppointmentRes(
    UUID id, String status,
    UUID doctorId, String doctorName,
    String doctorAvatar, String doctorSpecialty, String clinicName,
    String doctorTitle, Integer doctorYearsExperience,
    String patientName,
    Instant startAt, Instant endAt,
    String note
  ) {}

  public record WorkingHourReq(
    short dayOfWeek,
    String startTime,   // HH:mm
    String endTime,     // HH:mm
    short slotMinutes,
    String breakStart,  // HH:mm optional
    String breakEnd     // HH:mm optional
  ) {}

  public record CreateDoctorReq(
    String email, String fullName, String password,
    UUID specialtyId,
    String title, Long consultFeeVnd, String clinicName,
    Integer yearsExperience, String bio
  ) {}
}
