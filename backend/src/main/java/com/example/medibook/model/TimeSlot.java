package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="time_slots",
  uniqueConstraints = @UniqueConstraint(name="uq_slot_doctor_start", columnNames = {"doctor_id","start_at"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TimeSlot {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name="doctor_id", nullable = false)
  private DoctorProfile doctor;

  @Column(name="start_at", nullable = false)
  private Instant startAt;

  @Column(name="end_at", nullable = false)
  private Instant endAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private TimeSlotStatus status = TimeSlotStatus.AVAILABLE;

  @Column(name="created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
