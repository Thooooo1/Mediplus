package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "working_hours",
  uniqueConstraints = @UniqueConstraint(name="uq_working_hours_doctor_day", columnNames = {"doctor_id","day_of_week"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WorkingHour {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name="doctor_id", nullable = false)
  private DoctorProfile doctor;

  @Column(name="day_of_week", nullable = false)
  private short dayOfWeek; // 1..7

  @Column(name="start_time", nullable = false)
  private LocalTime startTime;

  @Column(name="end_time", nullable = false)
  private LocalTime endTime;

  @Column(name="slot_minutes", nullable = false)
  @Builder.Default
  private short slotMinutes = 30;

  @Column(name="break_start")
  private LocalTime breakStart;

  @Column(name="break_end")
  private LocalTime breakEnd;
}
