package com.example.medibook.service;

import com.example.medibook.dto.CatalogDtos;
import com.example.medibook.model.DoctorProfile;
import com.example.medibook.repo.DoctorProfileRepository;
import com.example.medibook.repo.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

  private final SpecialtyRepository specialtyRepo;
  private final DoctorProfileRepository doctorRepo;

  public List<CatalogDtos.SpecialtyRes> specialties() {
    return specialtyRepo.findAll().stream()
      .map(s -> new CatalogDtos.SpecialtyRes(s.getId(), s.getCode(), s.getName()))
      .toList();
  }

  public org.springframework.data.domain.Page<CatalogDtos.DoctorRes> doctors(UUID specialtyId, String q, Long minPrice, Long maxPrice, org.springframework.data.domain.Pageable pageable) {
    org.springframework.data.domain.Page<DoctorProfile> page;
    
    if (minPrice != null || maxPrice != null) {
        long min = minPrice != null ? minPrice : 0L;
        long max = maxPrice != null ? maxPrice : Long.MAX_VALUE;
        page = doctorRepo.findByPriceRange(min, max, pageable);
    } else if (specialtyId != null) {
        page = doctorRepo.findBySpecialtyId(specialtyId, pageable);
    } else if (q != null && !q.isBlank()) {
        page = doctorRepo.searchDoctors(q.trim(), pageable);
    } else {
        page = doctorRepo.findAll(pageable);
    }

    return page.map(this::mapDoctor);
  }

  public CatalogDtos.DoctorRes doctorDetail(UUID id) {
    DoctorProfile d = doctorRepo.findById(id).orElseThrow();
    return mapDoctor(d);
  }

  private CatalogDtos.DoctorRes mapDoctor(DoctorProfile d) {
    return new CatalogDtos.DoctorRes(
      d.getId(),
      d.getUser().getFullName(),
      d.getTitle(),
      d.getSpecialty().getName(),
      d.getConsultFeeVnd(),
      d.getClinicName(),
      d.getYearsExperience(),
      d.getBio(),
      d.getAvatarUrl(),
      d.getRating(),
      d.getRatingCount(),
      d.getCountry()
    );
  }
}
