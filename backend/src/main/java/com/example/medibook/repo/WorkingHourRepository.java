package com.example.medibook.repo;

import com.example.medibook.model.WorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, UUID> {
  List<WorkingHour> findByDoctorId(UUID doctorId);
  Optional<WorkingHour> findByDoctorIdAndDayOfWeek(UUID doctorId, short dayOfWeek);
}
