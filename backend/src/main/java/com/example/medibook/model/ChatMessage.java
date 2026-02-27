package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="chat_messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name="doctor_id", nullable = false)
  private DoctorProfile doctor;

  @ManyToOne(optional = false)
  @JoinColumn(name="patient_id", nullable = false)
  private AppUser patient;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private MessageSender sender;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(name="is_read", nullable = false)
  @Builder.Default
  private boolean isRead = false;

  @Column(name="created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
