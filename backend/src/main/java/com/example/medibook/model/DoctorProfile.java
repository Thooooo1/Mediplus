package com.example.medibook.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "doctor_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(optional = false)
  @JoinColumn(name="user_id", nullable = false, unique = true)
  private AppUser user;

  @ManyToOne(optional = false)
  @JoinColumn(name="specialty_id", nullable = false)
  private Specialty specialty;

  private String title;
  private String country;

  @Column(name="clinic_name")
  private String clinicName;

  @Column(length = 1000)
  private String bio;

  @Column(name="years_experience")
  private Integer yearsExperience;

  @Column(name="consult_fee_vnd")
  private Long consultFeeVnd;

  @Column(name="avatar_url", length = 500)
  private String avatarUrl;

  private Double rating;

  @Column(name="rating_count")
  private Integer ratingCount;
}
