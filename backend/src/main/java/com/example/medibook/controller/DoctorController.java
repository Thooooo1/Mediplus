package com.example.medibook.controller;

import com.example.medibook.dto.AppointmentDtos;
import com.example.medibook.dto.CatalogDtos;
import com.example.medibook.dto.DoctorDtos;
import com.example.medibook.model.*;
import com.example.medibook.model.AppointmentStatus;
import com.example.medibook.repo.*;
import com.example.medibook.security.UserPrincipal;
import com.example.medibook.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctorController {

  private final AppointmentService appointmentService;
  private final DoctorProfileRepository doctorRepo;
  private final AppointmentRepository appointmentRepo;
  private final MedicalRecordRepository recordRepo;
  private final ChatMessageRepository chatRepo;
  private final AppUserRepository userRepo;
  private final TimeSlotRepository timeSlotRepo;

  @Value("${app.timezone:Asia/Ho_Chi_Minh}")
  private String timezone;

  private DoctorProfile getDoctor(UserPrincipal p) {
      return doctorRepo.findByUserId(p.getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
  }

  @GetMapping("/appointments")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<AppointmentDtos.AppointmentRes> appointments(
    @RequestParam("from") String from,
    @RequestParam("to") String to,
    @AuthenticationPrincipal UserPrincipal p
  ) {
    var doctor = getDoctor(p);
    ZoneId zone = ZoneId.of(timezone);
    Instant fromI = LocalDate.parse(from).atStartOfDay(zone).toInstant();
    Instant toI = LocalDate.parse(to).plusDays(1).atStartOfDay(zone).toInstant();

    List<Appointment> list = appointmentService.doctorAppointments(doctor.getId(), fromI, toI);
    return list.stream().map(this::mapAppointment).toList();
  }

  @GetMapping("/stats")
  @PreAuthorize("hasRole('DOCTOR')")
  public DoctorDtos.StatsRes getStats(@AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      ZoneId zone = ZoneId.of(timezone);
      LocalDate today = LocalDate.now(zone);
      Instant startDay = today.atStartOfDay(zone).toInstant();
      Instant endDay = today.plusDays(1).atStartOfDay(zone).toInstant();

      LocalDate monday = today.with(DayOfWeek.MONDAY);
      Instant startWeek = monday.atStartOfDay(zone).toInstant();
      Instant endWeek = monday.plusDays(7).atStartOfDay(zone).toInstant();

      List<Appointment> todayAppts = appointmentRepo.findByDoctorIdAndTimeSlotStartAtBetween(doctor.getId(), startDay, endDay);
      List<Appointment> weekAppts = appointmentRepo.findByDoctorIdAndTimeSlotStartAtBetween(doctor.getId(), startWeek, endWeek);

      long pending = weekAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.BOOKED).count();
      long totalPatients = appointmentRepo.countDistinctPatientsByDoctorId(doctor.getId());

      return new DoctorDtos.StatsRes(todayAppts.size(), pending, weekAppts.size(), totalPatients);
  }

  @GetMapping("/patients")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<DoctorDtos.PatientRes> getPatients(@AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      List<Appointment> allAppts = appointmentRepo.findByDoctorIdOrderByCreatedAtDesc(doctor.getId());
      if (allAppts.isEmpty()) return List.of();

      Map<UUID, List<Appointment>> byPatient = allAppts.stream().collect(Collectors.groupingBy(a -> a.getPatient().getId()));

      return byPatient.entrySet().stream().map(entry -> {
          List<Appointment> apps = entry.getValue();
          AppUser patient = apps.get(0).getPatient();

          Optional<Appointment> lastVisit = apps.stream()
              .filter(a -> a.getTimeSlot().getStartAt().isBefore(Instant.now()))
              .max(Comparator.comparing(a -> a.getTimeSlot().getStartAt()));

          String status = lastVisit.map(a -> a.getStatus().name()).orElse("NEW");
          String note = lastVisit.map(Appointment::getPatientNote).orElse("");
          Instant lastDate = lastVisit.map(a -> a.getTimeSlot().getStartAt()).orElse(null);

          return new DoctorDtos.PatientRes(
              patient.getId(), patient.getFullName(), patient.getEmail(), patient.getPhone(),
              null, lastDate, status, note, apps.size()
          );
      }).sorted(Comparator.comparing(DoctorDtos.PatientRes::lastVisit, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
      .toList();
  }

  @GetMapping("/patients/{id}")
  @PreAuthorize("hasRole('DOCTOR')")
  public DoctorDtos.PatientDetailRes getPatientDetail(@PathVariable("id") UUID id, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var patient = userRepo.findById(id).orElseThrow(() ->
          new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

      List<Appointment> apps = appointmentRepo.findByDoctorIdAndPatientIdOrderByCreatedAtDesc(doctor.getId(), id);
      List<MedicalRecord> records = recordRepo.findByDoctorIdAndPatientIdOrderByVisitDateDesc(doctor.getId(), id);

      return new DoctorDtos.PatientDetailRes(
          patient.getId(), patient.getFullName(), patient.getEmail(), patient.getPhone(),
          apps.stream().map(this::mapAppointment).toList(),
          records.stream().map(r -> new DoctorDtos.RecordRes(
              r.getId(), r.getPatient().getId(), r.getPatient().getFullName(),
              r.getDiagnosis(), r.getNotes(), r.getVisitDate(), r.getType(), r.getCreatedAt()
          )).toList()
      );
  }

  @GetMapping("/records")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<DoctorDtos.RecordRes> getRecords(@AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      return recordRepo.findByDoctorIdOrderByVisitDateDesc(doctor.getId()).stream()
          .map(r -> new DoctorDtos.RecordRes(
              r.getId(), r.getPatient().getId(), r.getPatient().getFullName(),
              r.getDiagnosis(), r.getNotes(), r.getVisitDate(), r.getType(), r.getCreatedAt()
          )).toList();
  }

  @PostMapping("/records")
  @PreAuthorize("hasRole('DOCTOR')")
  @Transactional
  public DoctorDtos.RecordRes createRecord(@RequestBody DoctorDtos.CreateRecordReq req, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var patient = userRepo.findById(req.patientId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

      ZoneId zone = ZoneId.of(timezone);
      Instant visitDate = LocalDate.parse(req.date()).atStartOfDay(zone).toInstant();

      MedicalRecord r = MedicalRecord.builder()
          .doctor(doctor).patient(patient).diagnosis(req.diagnosis())
          .notes(req.notes()).visitDate(visitDate).type(req.type())
          .build();

      r = recordRepo.save(r);
      return new DoctorDtos.RecordRes(r.getId(), patient.getId(), patient.getFullName(), r.getDiagnosis(), r.getNotes(), r.getVisitDate(), r.getType(), r.getCreatedAt());
  }

  @DeleteMapping("/records/{id}")
  @PreAuthorize("hasRole('DOCTOR')")
  @Transactional
  public void deleteRecord(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var record = recordRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
      
      if (!record.getDoctor().getId().equals(doctor.getId())) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own records");
      }
      
      recordRepo.delete(record);
  }

  @GetMapping("/messages/contacts")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<DoctorDtos.ContactRes> getContacts(@AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      List<ChatMessage> latestMsgs = chatRepo.findLatestMessagesByDoctor(doctor.getId());

      return latestMsgs.stream().map(m -> new DoctorDtos.ContactRes(
          m.getPatient().getId(), m.getPatient().getFullName(), null,
          m.getContent(), m.getCreatedAt(),
          chatRepo.countUnreadForConversation(doctor.getId(), m.getPatient().getId())
      )).toList();
  }

  @GetMapping("/messages/{patientId}")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<DoctorDtos.MessageRes> getMessages(@PathVariable UUID patientId, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      List<ChatMessage> msgs = chatRepo.findByDoctorIdAndPatientIdOrderByCreatedAtAsc(doctor.getId(), patientId);

      msgs.forEach(m -> {
          if (m.getSender() == MessageSender.PATIENT && !m.isRead()) {
              m.setRead(true);
              chatRepo.save(m);
          }
      });

      return msgs.stream().map(m -> new DoctorDtos.MessageRes(
          m.getId(), m.getSender().name().toLowerCase(), m.getContent(), m.getCreatedAt()
      )).toList();
  }

  @PostMapping("/messages/{patientId}")
  @PreAuthorize("hasRole('DOCTOR')")
  @Transactional
  public DoctorDtos.MessageRes sendMessage(@PathVariable UUID patientId, @RequestBody DoctorDtos.SendMessageReq req, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var patient = userRepo.findById(patientId).orElseThrow();

      ChatMessage m = ChatMessage.builder()
          .doctor(doctor).patient(patient).sender(MessageSender.DOCTOR)
          .content(req.text()).isRead(false)
          .build();

      m = chatRepo.save(m);
      return new DoctorDtos.MessageRes(m.getId(), "doctor", m.getContent(), m.getCreatedAt());
  }

  @GetMapping("/profile")
  @PreAuthorize("hasRole('DOCTOR')")
  public DoctorDtos.ProfileRes getProfile(@AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var user = doctor.getUser();
      return new DoctorDtos.ProfileRes(user.getFullName(), user.getEmail(), user.getPhone(), doctor.getSpecialty().getName(), doctor.getBio());
  }

  @PutMapping("/profile")
  @PreAuthorize("hasRole('DOCTOR')")
  @Transactional
  public void updateProfile(@RequestBody DoctorDtos.UpdateProfileReq req, @AuthenticationPrincipal UserPrincipal p) {
      var doctor = getDoctor(p);
      var user = doctor.getUser();

      if (req.fullName() != null && !req.fullName().isBlank()) user.setFullName(req.fullName());
      if (req.phone() != null) user.setPhone(req.phone());
      userRepo.save(user);

      if (req.bio() != null) doctor.setBio(req.bio());
      doctorRepo.save(doctor);
  }

  @GetMapping("/time-slots")
  @PreAuthorize("hasRole('DOCTOR')")
  public List<CatalogDtos.SlotRes> getTimeSlots(
      @AuthenticationPrincipal UserPrincipal p,
      @RequestParam("from") String from,
      @RequestParam("to") String to
  ) {
      var doctor = getDoctor(p);
      ZoneId zone = ZoneId.of(timezone);
      Instant start = LocalDate.parse(from).atStartOfDay(zone).toInstant();
      Instant end = LocalDate.parse(to).plusDays(1).atStartOfDay(zone).toInstant();

      List<TimeSlot> slots = timeSlotRepo.findByDoctorIdAndStartAtBetweenOrderByStartAtAsc(doctor.getId(), start, end);
      List<Appointment> appointments = appointmentRepo.findByDoctorIdAndTimeSlotStartAtBetween(doctor.getId(), start, end);

      Map<UUID, Appointment> apptMap = appointments.stream()
          .collect(Collectors.toMap(a -> a.getTimeSlot().getId(), a -> a));

      return slots.stream()
          .map(s -> {
              Appointment a = apptMap.get(s.getId());
              return new CatalogDtos.SlotRes(
                  s.getId(), s.getStartAt(), s.getEndAt(), s.getStatus().name(),
                  a != null ? a.getId() : null,
                  a != null ? a.getPatient().getFullName() : null,
                  a != null ? a.getPatientNote() : null
              );
          })
          .toList();
  }

  private AppointmentDtos.AppointmentRes mapAppointment(Appointment a) {
    return new AppointmentDtos.AppointmentRes(
      a.getId(), a.getStatus().name(), a.getDoctor().getId(),
      a.getDoctor().getUser().getFullName(),
      a.getDoctor().getAvatarUrl(),
      a.getDoctor().getSpecialty() != null ? a.getDoctor().getSpecialty().getName() : null,
      a.getDoctor().getClinicName(),
      a.getDoctor().getTitle(),
      a.getDoctor().getYearsExperience(),
      a.getPatient().getFullName(),
      a.getTimeSlot().getStartAt(), a.getTimeSlot().getEndAt(), a.getPatientNote()
    );
  }
}
