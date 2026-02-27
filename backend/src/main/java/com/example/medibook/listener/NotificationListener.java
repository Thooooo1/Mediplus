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
    private final com.example.medibook.service.MailService mailService;

    @org.springframework.beans.factory.annotation.Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

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

        // Send Email to Doctor
        if (mailEnabled) {
            String docEmail = appt.getDoctor().getUser().getEmail();
            String html = String.format("""
                <div style="font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #e5e7eb; border-radius:12px; padding:24px;">
                    <h2 style="color:#2563eb;">Lịch khám mới — MediBook</h2>
                    <p>Chào Bác sĩ <strong>%s</strong>,</p>
                    <p>Bạn vừa có một lịch hẹn mới được đặt qua hệ thống:</p>
                    <ul style="list-style:none; padding:0;">
                        <li><strong>Bệnh nhân:</strong> %s</li>
                        <li><strong>Thời gian:</strong> %s</li>
                        <li><strong>Ghi chú:</strong> %s</li>
                    </ul>
                    <a href="https://medibook-v2.vercel.app/doctor/appointments.html" style="display:inline-block; padding:12px 24px; background:#2563eb; color:white; text-decoration:none; border-radius:8px; margin-top:16px;">Xem chi tiết lịch hẹn</a>
                </div>
                """, 
                appt.getDoctor().getUser().getFullName(),
                appt.getPatient().getFullName(),
                appt.getTimeSlot().getStartAt().toString(),
                appt.getPatientNote() != null ? appt.getPatientNote() : "Không có"
            );
            mailService.sendHtml(docEmail, "MediBook — Thông báo lịch khám mới", html);
        }

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

            // Send Email to Admin
            if (mailEnabled) {
                String adminHtml = String.format("""
                    <div style="font-family:Arial,sans-serif; padding:20px; border:1px solid #eee;">
                        <h3 style="color:#ef4444;">[Hệ thống] Có lịch hẹn mới vừa đặt</h3>
                        <p><strong>Bệnh nhân:</strong> %s</p>
                        <p><strong>Bác sĩ:</strong> %s</p>
                        <p><strong>Thời gian:</strong> %s</p>
                    </div>
                    """, 
                    appt.getPatient().getFullName(),
                    appt.getDoctor().getUser().getFullName(),
                    appt.getTimeSlot().getStartAt().toString()
                );
                mailService.sendHtml(admin.getEmail(), "[Admin Alert] Lịch khám mới được đặt", adminHtml);
            }
            }
        }

        // 3. Send Email to Patient
        if (mailEnabled) {
            String patientEmail = appt.getPatient().getEmail();
            String patientHtml = String.format("""
                <div style="font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #10b981; border-radius:12px; padding:24px;">
                    <h2 style="color:#10b981;">Xác nhận đặt lịch thành công — MediBook</h2>
                    <p>Chào bạn <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã tin tưởng sử dụng dịch vụ của MediBook. Lịch hẹn của bạn đã được ghi nhận:</p>
                    <div style="background:#f0fdf4; padding:16px; border-radius:8px;">
                        <p><strong>Bác sĩ:</strong> %s</p>
                        <li><strong>Chuyên khoa:</strong> %s</li>
                        <p><strong>Thời gian:</strong> %s</p>
                        <p><strong>Địa điểm:</strong> %s</p>
                    </div>
                    <p style="margin-top:16px;">Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>
                </div>
                """, 
                appt.getPatient().getFullName(),
                appt.getDoctor().getUser().getFullName(),
                appt.getDoctor().getSpecialty() != null ? appt.getDoctor().getSpecialty().getName() : "Đang cập nhật",
                appt.getTimeSlot().getStartAt().toString(),
                appt.getDoctor().getClinicName()
            );
            mailService.sendHtml(patientEmail, "MediBook — Xác nhận lịch hẹn thành công", patientHtml);
        }

        log.info("Notifications created and emails sent (if enabled) for appointment {}", appt.getId());
    }
}
