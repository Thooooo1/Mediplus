package com.example.medibook.service;

import com.example.medibook.exception.BadRequestException;
import com.example.medibook.model.*;
import com.example.medibook.repo.DoctorProfileRepository;
import com.example.medibook.repo.TimeSlotRepository;
import com.example.medibook.repo.WorkingHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlotService {

  private final DoctorProfileRepository doctorRepo;
  private final WorkingHourRepository workingHourRepo;
  private final TimeSlotRepository slotRepo;

  public List<TimeSlot> availableSlots(UUID doctorId, LocalDate date, ZoneId zone) {
    Instant from = date.atStartOfDay(zone).toInstant();
    Instant to = date.plusDays(1).atStartOfDay(zone).toInstant();
    return slotRepo.findByDoctorIdAndStartAtBetweenAndStatusOrderByStartAt(
      doctorId, from, to, TimeSlotStatus.AVAILABLE
    );
  }

  @Transactional
  public int generateSlots(UUID doctorId, LocalDate fromDate, LocalDate toDate, ZoneId zone) {
    if (toDate.isBefore(fromDate)) throw new BadRequestException("toDate must be >= fromDate");

    DoctorProfile doctor = doctorRepo.findById(doctorId).orElseThrow();

    // Instant from = fromDate.atStartOfDay(zone).toInstant();
    // Instant to = toDate.plusDays(1).atStartOfDay(zone).toInstant();
    // Removed destructive delete to support idempotent updates
    // slotRepo.deleteByDoctorIdAndStartAtBetween(doctorId, from, to);

    int count = 0;
    LocalDate d = fromDate;
    while (!d.isAfter(toDate)) {
      short dow = (short) d.getDayOfWeek().getValue(); // 1..7
      WorkingHour wh = workingHourRepo.findByDoctorIdAndDayOfWeek(doctorId, dow).orElse(null);
      if (wh != null) count += generateForDay(doctor, wh, d, zone);
      d = d.plusDays(1);
    }
    return count;
  }

  private int generateForDay(DoctorProfile doctor, WorkingHour wh, LocalDate date, ZoneId zone) {
    List<TimeSlot> newSlots = new ArrayList<>();

    LocalDateTime start = LocalDateTime.of(date, wh.getStartTime());
    LocalDateTime end = LocalDateTime.of(date, wh.getEndTime());

    // Fetch existing slots for this day to avoid duplicates/overlaps
    Instant dayStart = date.atStartOfDay(zone).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
    List<TimeSlot> existingSlots = slotRepo.findByDoctorIdAndStartAtBetweenOrderByStartAtAsc(doctor.getId(), dayStart, dayEnd);

    int minutes = wh.getSlotMinutes();
    LocalDateTime cur = start;

    while (!cur.plusMinutes(minutes).isAfter(end)) {
      LocalDateTime next = cur.plusMinutes(minutes);

      boolean inBreak = wh.getBreakStart() != null && wh.getBreakEnd() != null
        && !cur.toLocalTime().isBefore(wh.getBreakStart())
        && cur.toLocalTime().isBefore(wh.getBreakEnd());

      if (!inBreak) {
        Instant slotStart = cur.atZone(zone).toInstant();
        Instant slotEnd = next.atZone(zone).toInstant();

        // Check compatibility with existing slots
        boolean conflict = existingSlots.stream().anyMatch(existing -> {
          // Overlap check: (StartA < EndB) and (EndA > StartB)
          return slotStart.isBefore(existing.getEndAt()) && slotEnd.isAfter(existing.getStartAt());
        });

        if (!conflict) {
          newSlots.add(TimeSlot.builder()
            .doctor(doctor)
            .startAt(slotStart)
            .endAt(slotEnd)
            .status(TimeSlotStatus.AVAILABLE)
            .build());
        }
      }
      cur = next;
    }

    if (!newSlots.isEmpty()) {
      slotRepo.saveAll(newSlots);
    }
    return newSlots.size();
  }
}
