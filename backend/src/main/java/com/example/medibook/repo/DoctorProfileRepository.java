package com.example.medibook.repo;

import com.example.medibook.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
  org.springframework.data.domain.Page<DoctorProfile> findBySpecialtyId(UUID specialtyId, org.springframework.data.domain.Pageable pageable);

  @org.springframework.data.jpa.repository.Query("SELECT d FROM DoctorProfile d WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.specialty.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :q, '%'))")
  org.springframework.data.domain.Page<DoctorProfile> searchDoctors(@org.springframework.data.repository.query.Param("q") String q, org.springframework.data.domain.Pageable pageable);

  Optional<DoctorProfile> findByUserId(UUID userId);
}
