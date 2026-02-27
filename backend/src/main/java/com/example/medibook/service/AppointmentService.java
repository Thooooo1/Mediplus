package com.example.medibook.service;

import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.exception.ConflictException;
import com.example.medibook.exception.NotFoundException;
import com.example.medibook.model.*;
import com.example.medibook.repo.AppUserRepository;
import com.example.medibook.repo.AppointmentRepository;
import com.example.medibook.repo.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

  private final TimeSlotRepository slotRepo;
  private final AppointmentRepository appointmentRepo;
  private final AppUserRepository userRepo;
  private final ApplicationEventPublisher publisher;

  @Transactional
  public Appointment book(UUID slotId, UUID patientId, String note) {
    TimeSlot slot = slotRepo.findByIdForUpdate(slotId)
      .orElseThrow(() -> new NotFoundException("Slot not found"));

    if (slot.getStatus() != TimeSlotStatus.AVAILABLE) {
      throw new ConflictException("Slot already booked");
    }

    slot.setStatus(TimeSlotStatus.BOOKED);

    AppUser patient = userRepo.findById(patientId)
      .orElseThrow(() -> new NotFoundException("User not found"));

    Appointment appt = Appointment.builder()
      .doctor(slot.getDoctor())
      .patient(patient)
      .timeSlot(slot)
      .status(AppointmentStatus.BOOKED)
      .patientNote(note)
      .createdAt(Instant.now())
      .updatedAt(Instant.now())
      .build();

    Appointment saved = appointmentRepo.save(appt);
    publisher.publishEvent(new AppointmentBookedEvent(saved.getId()));
    log.info("Appointment booked: {} for patient {} with doctor {}", saved.getId(), patient.getEmail(), slot.getDoctor().getUser().getFullName());
    return saved;
  }

  public List<Appointment> myAppointments(UUID patientId) {
    return appointmentRepo.findMyAppointments(patientId);
  }

  public Appointment findById(UUID id) {
    return appointmentRepo.findById(id).orElseThrow(() -> new NotFoundException("Appointment not found"));
  }

  public List<Appointment> doctorAppointments(UUID doctorId, Instant from, Instant to) {
    return appointmentRepo.findDoctorAppointments(doctorId, from, to);
  }

  @Transactional
  public void cancel(UUID appointmentId, UUID userId, String userRole) {
    Appointment a = appointmentRepo.findById(appointmentId)
      .orElseThrow(() -> new NotFoundException("Appointment not found"));

    boolean isPatient = a.getPatient().getId().equals(userId);
    boolean isDoctor = a.getDoctor().getUser().getId().equals(userId);

    if (userRole.equals("DOCTOR")) {
      if (!isDoctor) throw new ConflictException("Not your appointment");
    } else {
      if (!isPatient) throw new ConflictException("Not your appointment");
    }

    if (a.getStatus() != AppointmentStatus.BOOKED && a.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new ConflictException("Cannot cancel appointment with status: " + a.getStatus());
    }

    TimeSlot slot = slotRepo.findByIdForUpdate(a.getTimeSlot().getId())
      .orElseThrow(() -> new NotFoundException("Slot not found"));

    a.setStatus(AppointmentStatus.CANCELLED);
    a.setCancelledAt(Instant.now());
    a.setUpdatedAt(Instant.now());
    slot.setStatus(TimeSlotStatus.AVAILABLE);

    log.info("Appointment {} cancelled by {} (role={})", appointmentId, userId, userRole);
  }

  @Transactional
  public void confirm(UUID appointmentId, UUID doctorUserId) {
    Appointment a = appointmentRepo.findById(appointmentId)
      .orElseThrow(() -> new NotFoundException("Appointment not found"));

    if (!a.getDoctor().getUser().getId().equals(doctorUserId)) {
      throw new ConflictException("Not your appointment");
    }
    if (a.getStatus() != AppointmentStatus.BOOKED) {
      throw new ConflictException("Can only confirm BOOKED appointments");
    }

    a.setStatus(AppointmentStatus.CONFIRMED);
    a.setUpdatedAt(Instant.now());
    log.info("Appointment {} confirmed by doctor {}", appointmentId, doctorUserId);
  }

  @Transactional
  public void complete(UUID appointmentId, UUID doctorUserId) {
    Appointment a = appointmentRepo.findById(appointmentId)
      .orElseThrow(() -> new NotFoundException("Appointment not found"));

    if (!a.getDoctor().getUser().getId().equals(doctorUserId)) {
      throw new ConflictException("Not your appointment");
    }
    if (a.getStatus() != AppointmentStatus.CONFIRMED && a.getStatus() != AppointmentStatus.BOOKED) {
      throw new ConflictException("Can only complete BOOKED or CONFIRMED appointments");
    }

    a.setStatus(AppointmentStatus.COMPLETED);
    a.setUpdatedAt(Instant.now());
    log.info("Appointment {} completed by doctor {}", appointmentId, doctorUserId);
  }
}
