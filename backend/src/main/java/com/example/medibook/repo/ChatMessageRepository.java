package com.example.medibook.repo;

import com.example.medibook.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByDoctorIdAndPatientIdOrderByCreatedAtAsc(UUID doctorId, UUID patientId);

    @Query("SELECT m FROM ChatMessage m WHERE m.doctor.id = :doctorId AND m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.doctor.id = :doctorId GROUP BY m2.patient.id) ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessagesByDoctor(@Param("doctorId") UUID doctorId);
    
    // Count unread messages for doctor (where sender is PATIENT)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.doctor.id = :doctorId AND m.sender = 'PATIENT' AND m.isRead = false")
    long countUnreadForDoctor(@Param("doctorId") UUID doctorId);
    
    // Count unread for specific conversation
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.doctor.id = :doctorId AND m.patient.id = :patientId AND m.sender = 'PATIENT' AND m.isRead = false")
    long countUnreadForConversation(@Param("doctorId") UUID doctorId, @Param("patientId") UUID patientId);
}
