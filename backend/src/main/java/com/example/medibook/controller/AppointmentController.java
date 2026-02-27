package com.example.medibook.controller;

import com.example.medibook.dto.AppointmentDtos;
import com.example.medibook.model.Appointment;
import com.example.medibook.security.UserPrincipal;
import com.example.medibook.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;

  @PostMapping("/book")
  @PreAuthorize("hasRole('USER')")
  public AppointmentDtos.AppointmentRes book(
    @Valid @RequestBody AppointmentDtos.BookReq req,
    @AuthenticationPrincipal UserPrincipal p
  ) {
    Appointment a = appointmentService.book(req.timeSlotId(), p.getId(), req.note());
    return map(a);
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('USER') or hasRole('DOCTOR')")
  public List<AppointmentDtos.AppointmentRes> my(@AuthenticationPrincipal UserPrincipal p) {
    if (p.getRole().equals("DOCTOR")) {
      return List.of(); // Doctors use /api/doctor/appointments instead
    }
    return appointmentService.myAppointments(p.getId()).stream().map(this::map).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
  public AppointmentDtos.AppointmentRes getById(@PathVariable("id") UUID id, @AuthenticationPrincipal UserPrincipal p) {
    Appointment appt = appointmentService.findById(id);

    boolean isPatient = appt.getPatient().getId().equals(p.getId());
    boolean isDoctor = appt.getDoctor().getUser().getId().equals(p.getId());
    boolean isAdmin = p.getRole().equals("ADMIN");

    if (!isPatient && !isDoctor && !isAdmin) {
      throw new com.example.medibook.exception.ConflictException("Access denied");
    }
    return map(appt);
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('USER') or hasRole('DOCTOR')")
  public void cancel(@PathVariable("id") UUID id, @AuthenticationPrincipal UserPrincipal p) {
    appointmentService.cancel(id, p.getId(), p.getRole());
  }

  @PostMapping("/{id}/confirm")
  @PreAuthorize("hasRole('DOCTOR')")
  public void confirm(@PathVariable("id") UUID id, @AuthenticationPrincipal UserPrincipal p) {
    appointmentService.confirm(id, p.getId());
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasRole('DOCTOR')")
  public void complete(@PathVariable("id") UUID id, @AuthenticationPrincipal UserPrincipal p) {
    appointmentService.complete(id, p.getId());
  }

  private AppointmentDtos.AppointmentRes map(Appointment a) {
    return new AppointmentDtos.AppointmentRes(
      a.getId(),
      a.getStatus().name(),
      a.getDoctor().getId(),
      a.getDoctor().getUser().getFullName(),
      a.getDoctor().getAvatarUrl(),
      a.getDoctor().getSpecialty() != null ? a.getDoctor().getSpecialty().getName() : null,
      a.getDoctor().getClinicName(),
      a.getDoctor().getTitle(),
      a.getDoctor().getYearsExperience(),
      a.getPatient().getFullName(),
      a.getTimeSlot().getStartAt(),
      a.getTimeSlot().getEndAt(),
      a.getPatientNote()
    );
  }
}
