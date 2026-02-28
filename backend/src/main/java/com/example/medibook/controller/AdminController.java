package com.example.medibook.controller;

import com.example.medibook.dto.AppointmentDtos;
import com.example.medibook.exception.BadRequestException;
import com.example.medibook.model.*;
import com.example.medibook.repo.*;
import com.example.medibook.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.listener.NotificationListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

  private final AppUserRepository userRepo;
  private final SpecialtyRepository specialtyRepo;
  private final DoctorProfileRepository doctorRepo;
  private final WorkingHourRepository workingHourRepo;
  private final AppointmentRepository appointmentRepo;
  private final SlotService slotService;
  private final PasswordEncoder encoder;
  private final com.example.medibook.service.MailService mailService;
  private final ApplicationEventPublisher publisher;
  private final NotificationListener notificationListener;

  @Value("${app.mail.enabled:false}")
  private boolean mailEnabled;

  @Value("${app.timezone:Asia/Ho_Chi_Minh}")
  private String timezone;

  @GetMapping("/test-email")
  public String testEmail(@RequestParam("to") String to, @RequestParam(value = "secret", required = false) String secret) {
    if (!"medi-check".equals(secret)) {
      return "Error: Missing or invalid secret parameter.";
    }
    try {
      mailService.sendHtml(to, "[MediBook] Kiểm tra hệ thống Email", 
        "<h3>Chào bạn,</h3><p>Nếu bạn nhận được email này, có nghĩa là API Key mới của bạn đã hoạt động chính xác!</p>" +
        "<p>Trạng thái MAIL_ENABLED hiện tại: <b>" + mailEnabled + "</b></p>");
      return "Email sent to " + to + ". Current MAIL_ENABLED status: " + mailEnabled;
    } catch (Exception e) {
      log.error("[TestEmail] Failed: {}", e.getMessage());
      return "Error: " + e.getMessage() + ". MAIL_ENABLED: " + mailEnabled;
    }
  }

  @GetMapping("/test-notif")
  @Transactional
  public String testNotif(@RequestParam("id") String idStr, @RequestParam(value = "secret", required = false) String secret) {
    if (!"medi-check".equals(secret)) return "Forbidden";
    
    UUID appointmentId;
    try {
      String processedId = idStr;
      if (processedId.startsWith("[") && processedId.endsWith("]")) {
          processedId = processedId.substring(1, processedId.length() - 1);
      }
      if (processedId.startsWith("#")) {
          processedId = processedId.substring(1);
      }
      
      final String finalId = processedId;

      if (finalId.length() == 36) {
        appointmentId = UUID.fromString(finalId);
      } else {
        // Search by prefix (Short ID)
        log.info("[DeepDebug] Searching for appointment with short ID prefix: {}", finalId);
        List<Appointment> matches = appointmentRepo.findAll().stream()
            .filter(a -> a.getId().toString().toUpperCase().startsWith(finalId.toUpperCase()))
            .toList();
        
        if (matches.isEmpty()) return "Error: No appointment found starting with " + finalId;
        if (matches.size() > 1) return "Error: Multiple appointments found starting with " + finalId + ". Please provide more characters.";
        appointmentId = matches.get(0).getId();
      }

      log.info("[DeepDebug] Manually triggering notification for appt: {}", appointmentId);
      AppointmentBookedEvent event = new AppointmentBookedEvent(appointmentId);
      return notificationListener.handleAppointmentBookedDebug(event);
    } catch (Exception e) {
      log.error("[DeepDebug] Error: {}", e.getMessage());
      return "Error: " + e.getMessage();
    }
  }

  // ─── Doctor Management ─────────────────────────────────

  @PostMapping("/doctors")
  @PreAuthorize("hasRole('ADMIN')")
  public UUID createDoctor(@RequestBody AppointmentDtos.CreateDoctorReq req) {
    if (req.email() == null || req.password() == null || req.fullName() == null || req.specialtyId() == null) {
      throw new BadRequestException("Missing fields");
    }
    String email = req.email().trim().toLowerCase();
    if (userRepo.findByEmail(email).isPresent()) throw new BadRequestException("Email exists");

    Specialty sp = specialtyRepo.findById(req.specialtyId()).orElseThrow();

    AppUser du = userRepo.save(AppUser.builder()
      .email(email)
      .fullName(req.fullName().trim())
      .passwordHash(encoder.encode(req.password()))
      .role(Role.DOCTOR)
      .enabled(true)
      .build());

    DoctorProfile dp = doctorRepo.save(DoctorProfile.builder()
      .user(du)
      .specialty(sp)
      .title(req.title())
      .consultFeeVnd(req.consultFeeVnd())
      .clinicName(req.clinicName())
      .yearsExperience(req.yearsExperience())
      .bio(req.bio())
      .build());

    log.info("Created doctor {} with profile {}", du.getEmail(), dp.getId());
    return dp.getId();
  }

  // ─── Working Hours ─────────────────────────────────────

  @GetMapping("/doctors/{id}/working-hours")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AppointmentDtos.WorkingHourReq> getWorkingHours(@PathVariable("id") UUID id) {
    return workingHourRepo.findByDoctorId(id).stream()
      .map(wh -> new AppointmentDtos.WorkingHourReq(
        wh.getDayOfWeek(),
        wh.getStartTime().toString(),
        wh.getEndTime().toString(),
        wh.getSlotMinutes(),
        wh.getBreakStart() != null ? wh.getBreakStart().toString() : null,
        wh.getBreakEnd() != null ? wh.getBreakEnd().toString() : null
      ))
      .toList();
  }

  @PutMapping("/doctors/{id}/working-hours")
  @PreAuthorize("hasRole('ADMIN')")
  public void setWorkingHours(@PathVariable("id") UUID id, @RequestBody List<AppointmentDtos.WorkingHourReq> reqs) {
    DoctorProfile doctor = doctorRepo.findById(id).orElseThrow();
    workingHourRepo.findByDoctorId(id).forEach(workingHourRepo::delete);

    for (var r : reqs) {
      WorkingHour wh = WorkingHour.builder()
        .doctor(doctor)
        .dayOfWeek(r.dayOfWeek())
        .startTime(java.time.LocalTime.parse(r.startTime()))
        .endTime(java.time.LocalTime.parse(r.endTime()))
        .slotMinutes(r.slotMinutes())
        .breakStart(r.breakStart() == null || r.breakStart().isBlank() ? null : java.time.LocalTime.parse(r.breakStart()))
        .breakEnd(r.breakEnd() == null || r.breakEnd().isBlank() ? null : java.time.LocalTime.parse(r.breakEnd()))
        .build();
      workingHourRepo.save(wh);
    }
  }

  @PostMapping("/doctors/{id}/generate-slots")
  @PreAuthorize("hasRole('ADMIN')")
  public int generateSlots(@PathVariable("id") UUID id, @RequestParam("from") String from, @RequestParam("to") String to) {
    ZoneId zone = ZoneId.of(timezone);
    return slotService.generateSlots(id, LocalDate.parse(from), LocalDate.parse(to), zone);
  }

  // ─── Admin Stats ───────────────────────────────────────

  @GetMapping("/stats")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> getStats() {
    long totalUsers = userRepo.count();
    long totalDoctors = doctorRepo.count();
    long totalAppointments = appointmentRepo.count();

    ZoneId zone = ZoneId.of(timezone);
    LocalDate today = LocalDate.now(zone);
    java.time.Instant startDay = today.atStartOfDay(zone).toInstant();
    java.time.Instant endDay = today.plusDays(1).atStartOfDay(zone).toInstant();

    long todayAppointments = appointmentRepo.countByTimeSlotStartAtBetween(startDay, endDay);
    long pendingAppointments = appointmentRepo.countByStatus(AppointmentStatus.BOOKED);

    return Map.of(
      "totalUsers", totalUsers,
      "totalDoctors", totalDoctors,
      "totalAppointments", totalAppointments,
      "todayAppointments", todayAppointments,
      "pendingAppointments", pendingAppointments
    );
  }

  // ─── Admin Appointments List ───────────────────────────

  @GetMapping("/appointments")
  @PreAuthorize("hasRole('ADMIN')")
  public Page<AppointmentDtos.AppointmentRes> listAppointments(
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "20") int size
  ) {
    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Appointment> result;
    if (status != null && !status.isBlank()) {
      try {
        AppointmentStatus st = AppointmentStatus.valueOf(status.toUpperCase());
        result = appointmentRepo.findByStatus(st, pr);
      } catch (IllegalArgumentException e) {
        result = appointmentRepo.findAll(pr);
      }
    } else {
      result = appointmentRepo.findAll(pr);
    }

    return result.map(a -> new AppointmentDtos.AppointmentRes(
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
    ));
  }

  // ─── Admin Users Management ────────────────────────────

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public Page<Map<String, Object>> listUsers(
    @RequestParam(value = "q", required = false) String q,
    @RequestParam(value = "role", required = false) String role,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "20") int size
  ) {
    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<AppUser> result;
    if (q != null && !q.isBlank()) {
      result = userRepo.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pr);
    } else if (role != null && !role.isBlank()) {
      try {
        result = userRepo.findByRole(Role.valueOf(role.toUpperCase()), pr);
      } catch (IllegalArgumentException e) {
        result = userRepo.findAll(pr);
      }
    } else {
      result = userRepo.findAll(pr);
    }

    return result.map(u -> Map.<String, Object>of(
      "id", u.getId(),
      "email", u.getEmail(),
      "fullName", u.getFullName(),
      "role", u.getRole().name(),
      "enabled", u.isEnabled(),
      "createdAt", u.getCreatedAt().toString()
    ));
  }

  @PutMapping("/users/{id}/toggle-enabled")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> toggleUserEnabled(@PathVariable("id") UUID id) {
    AppUser u = userRepo.findById(id).orElseThrow();
    u.setEnabled(!u.isEnabled());
    userRepo.save(u);
    log.info("Toggled user {} enabled={}", u.getEmail(), u.isEnabled());
    return Map.of("id", u.getId(), "enabled", u.isEnabled());
  }
}
