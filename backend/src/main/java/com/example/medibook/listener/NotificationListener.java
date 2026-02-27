package com.example.medibook.listener;

import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.model.AppUser;
import com.example.medibook.model.Appointment;
import com.example.medibook.model.Notification;
import com.example.medibook.model.Role;
import com.example.medibook.repo.AppUserRepository;
import com.example.medibook.repo.AppointmentRepository;
import com.example.medibook.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepo;
    private final AppointmentRepository appointmentRepo;
    private final AppUserRepository userRepo;

    @EventListener
    @Transactional
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        Appointment appt = appointmentRepo.findById(event.appointmentId()).orElse(null);
        if (appt == null) return;

        String title = "Lịch hẹn mới";
        String message = String.format("Bệnh nhân %s đã đặt lịch vào lúc %s", 
            appt.getPatient().getFullName(), 
            appt.getTimeSlot().getStartAt().toString());

        // 1. Notify the Doctor
        Notification docNotif = Notification.builder()
            .user(appt.getDoctor().getUser())
            .title(title)
            .message(message)
            .type("APPOINTMENT_BOOKED")
            .relatedId(appt.getId())
            .build();
        notificationRepo.save(docNotif);

        // 2. Notify all Admins
        List<AppUser> admins = userRepo.findByRole(Role.ADMIN);
        for (AppUser admin : admins) {
            Notification adminNotif = Notification.builder()
                .user(admin)
                .title("Admin: " + title)
                .message(message + " (Bác sĩ: " + appt.getDoctor().getUser().getFullName() + ")")
                .type("APPOINTMENT_BOOKED")
                .relatedId(appt.getId())
                .build();
            notificationRepo.save(adminNotif);
        }

        log.info("Notifications created for appointment {}", appt.getId());
    }
}
