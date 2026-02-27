package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="appointments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name="doctor_id", nullable = false)
  private DoctorProfile doctor;

  @ManyToOne(optional = false)
  @JoinColumn(name="patient_id", nullable = false)
  private AppUser patient;

  @OneToOne(optional = false)
  @JoinColumn(name="time_slot_id", nullable = false, unique = true)
  private TimeSlot timeSlot;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private AppointmentStatus status = AppointmentStatus.BOOKED;

  @Column(name="patient_note", length = 1000)
  private String patientNote;

  @Column(name="created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name="updated_at", nullable = false)
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @Column(name="cancelled_at")
  private Instant cancelledAt;
}
