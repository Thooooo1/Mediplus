package com.example.medibook.repo;

import com.example.medibook.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    @Query("SELECT r FROM MedicalRecord r JOIN FETCH r.patient JOIN FETCH r.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty WHERE r.doctor.id = :doctorId ORDER BY r.visitDate DESC")
    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(@Param("doctorId") UUID doctorId);

    @Query("SELECT r FROM MedicalRecord r JOIN FETCH r.patient JOIN FETCH r.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty WHERE r.doctor.id = :doctorId AND r.patient.id = :patientId ORDER BY r.visitDate DESC")
    List<MedicalRecord> findByDoctorIdAndPatientIdOrderByVisitDateDesc(@Param("doctorId") UUID doctorId, @Param("patientId") UUID patientId);
}
