package com.example.medibook.dto;

import java.time.Instant;
import java.util.UUID;

public class CatalogDtos {
  public record SpecialtyRes(UUID id, String code, String name) {}
  public record DoctorRes(
    UUID id, String fullName, String title, String specialtyName,
    Long consultFeeVnd, String clinicName, Integer yearsExperience, String bio,
    String avatarUrl, Double rating, Integer ratingCount, String country,
    String status
  ) {}
  public record SlotRes(
      UUID id,
      Instant startAt,
      Instant endAt,
      String status,
      UUID appointmentId,
      String patientName,
      String note
  ) {}
}
