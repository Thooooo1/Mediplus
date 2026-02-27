package com.example.medibook.controller;

import com.example.medibook.dto.CatalogDtos;
import com.example.medibook.model.TimeSlot;
import com.example.medibook.service.CatalogService;
import com.example.medibook.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService catalogService;
  private final SlotService slotService;

  @Value("${app.timezone:Asia/Ho_Chi_Minh}")
  private String timezone;

  @GetMapping("/api/specialties")
  public List<CatalogDtos.SpecialtyRes> specialties() {
    return catalogService.specialties();
  }

  @GetMapping("/api/doctors")
  public org.springframework.data.domain.Page<CatalogDtos.DoctorRes> doctors(
    @RequestParam(value = "specialtyId", required = false) UUID specialtyId,
    @RequestParam(value = "q", required = false) String q,
    @RequestParam(value = "minPrice", required = false) Long minPrice,
    @RequestParam(value = "maxPrice", required = false) Long maxPrice,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "9") int size
  ) {
    return catalogService.doctors(specialtyId, q, minPrice, maxPrice, org.springframework.data.domain.PageRequest.of(page, size));
  }

  @GetMapping("/api/doctors/{id}")
  public CatalogDtos.DoctorRes doctor(@PathVariable("id") UUID id) {
    return catalogService.doctorDetail(id);
  }

  @GetMapping("/api/doctors/{id}/slots")
  public List<CatalogDtos.SlotRes> slots(@PathVariable("id") UUID id, @RequestParam("date") String date) {
    ZoneId zone = ZoneId.of(timezone);
    LocalDate d = LocalDate.parse(date);
    List<TimeSlot> list = slotService.availableSlots(id, d, zone);
    return list.stream()
      .map(s -> new CatalogDtos.SlotRes(
          s.getId(), s.getStartAt(), s.getEndAt(), s.getStatus().name(),
          null, null, null
      ))
      .toList();
  }
}
