package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 190)
  private String email;

  @Column(name="full_name", nullable = false, length = 120)
  private String fullName;

  @Column(length = 30)
  private String phone;

  @Column(name="password_hash", nullable = false, length = 120)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  @Builder.Default
  private boolean enabled = true;

  @Column(name="created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();
}
