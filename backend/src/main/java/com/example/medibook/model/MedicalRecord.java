package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="medical_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name="doctor_id", nullable = false)
  private DoctorProfile doctor;

  @ManyToOne(optional = false)
  @JoinColumn(name="patient_id", nullable = false)
  private AppUser patient;

  @Column(nullable = false, length = 255)
  private String diagnosis;

  @Column(length = 2000)
  private String notes;

  @Column(name="visit_date", nullable = false)
  private Instant visitDate;

  // e.g. "Khám tổng quát", "Tái khám"
  @Column(length = 50)
  private String type;

  @Column(name="created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
