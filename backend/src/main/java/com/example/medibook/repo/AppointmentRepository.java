package com.example.medibook.repo;

import com.example.medibook.model.Appointment;
import com.example.medibook.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // For MailListener
    @Query("SELECT a FROM Appointment a JOIN FETCH a.doctor d JOIN FETCH d.user u JOIN FETCH a.patient p WHERE a.id = :id")
    Optional<Appointment> findDetailsById(UUID id);

    // For patient's appointments
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot WHERE a.patient.id = :patientId ORDER BY a.timeSlot.startAt DESC")
    List<Appointment> findMyAppointments(@Param("patientId") UUID patientId);

    // For doctor's date-range appointments
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot WHERE a.doctor.id = :doctorId AND a.timeSlot.startAt BETWEEN :from AND :to")
    List<Appointment> findDoctorAppointments(@Param("doctorId") UUID doctorId, @Param("from") Instant from, @Param("to") Instant to);

    // For Doctor Dashboard stats
    List<Appointment> findByDoctorIdAndTimeSlotStartAtBetween(UUID doctorId, Instant start, Instant end);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countDistinctPatientsByDoctorId(@Param("doctorId") UUID doctorId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot WHERE d.id = :doctorId ORDER BY a.createdAt DESC")
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(@Param("doctorId") UUID doctorId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot WHERE d.id = :doctorId AND a.patient.id = :patientId ORDER BY a.createdAt DESC")
    List<Appointment> findByDoctorIdAndPatientIdOrderByCreatedAtDesc(@Param("doctorId") UUID doctorId, @Param("patientId") UUID patientId);

    // ─── Admin queries ───────────────────────────────────
    long countByStatus(AppointmentStatus status);
    long countByTimeSlotStartAtBetween(Instant from, Instant to);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot WHERE a.status = :status",
           countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    Page<Appointment> findByStatus(@Param("status") AppointmentStatus status, Pageable pageable);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor d JOIN FETCH d.user LEFT JOIN FETCH d.specialty JOIN FETCH a.timeSlot",
           countQuery = "SELECT COUNT(a) FROM Appointment a")
    Page<Appointment> findAll(Pageable pageable);
}
