package com.example.medibook.repo;

import com.example.medibook.model.TimeSlot;
import com.example.medibook.model.TimeSlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {
  // ... existing methods
  List<TimeSlot> findByDoctorIdAndStartAtBetweenOrderByStartAtAsc(UUID doctorId, Instant start, Instant end);

  List<TimeSlot> findByDoctorIdAndStartAtBetweenAndStatusOrderByStartAt(
    UUID doctorId, Instant from, Instant to, TimeSlotStatus status
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from TimeSlot s where s.id = :id")
  Optional<TimeSlot> findByIdForUpdate(@Param("id") UUID id);

  void deleteByDoctorIdAndStartAtBetween(UUID doctorId, Instant from, Instant to);
}
