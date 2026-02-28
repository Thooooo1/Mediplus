package com.example.medibook.listener;

import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.events.AppointmentCancelledEvent;
import com.example.medibook.events.AppointmentConfirmedEvent;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        handleAppointmentBookedDebug(event);
    }

    public String handleAppointmentBookedDebug(AppointmentBookedEvent event) {
        StringBuilder report = new StringBuilder();
        report.append("--- Notification Debug Report ---\n");
        report.append("Event ID: ").append(event.appointmentId()).append("\n");
        report.append("Mail Enabled Status: ").append(mailEnabled).append("\n");
        
        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            String msg = "[NotifDebug] Appointment " + event.appointmentId() + " NOT FOUND in listener.";
            log.warn(msg);
            return report.append("ERROR: ").append(msg).toString();
        }

        report.append("Appointment found: ").append(appt.getId()).append("\n");
        report.append("Patient: ").append(appt.getPatient().getFullName()).append("\n");
        report.append("Doctor: ").append(appt.getDoctor().getUser().getFullName()).append("\n");

        String title = "Lịch hẹn mới";
        String message = String.format("Bệnh nhân %s đã đặt lịch vào lúc %s", 
            appt.getPatient().getFullName(), 
            appt.getTimeSlot().getStartAt().toString());
        
        log.info("[NotifDebug] Start handling appointment {}. Patient: {}. mailEnabled={}", appt.getId(), appt.getPatient().getFullName(), mailEnabled);
        
        String officialAdminEmail = "tnguyenanh189@gmail.com";

        // 1. Notify the Doctor
        Notification docNotif = Notification.builder()
            .user(appt.getDoctor().getUser())
            .title(title)
            .message(message)
            .type("APPOINTMENT_BOOKED")
            .relatedId(appt.getId())
            .build();
        notificationRepo.save(docNotif);
        log.info("[NotifDebug] Saved in-app notification for doctor: {}", appt.getDoctor().getUser().getEmail());

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
            try {
                mailService.sendHtml(docEmail, "MediBook — Thông báo lịch khám mới", html);
            } catch (Exception e) {
                log.error("[NotifDebug] Failed to send email to doctor: {}", e.getMessage());
            }
        }

        // 2. Notify all Admins + Always notify officialAdminEmail
        List<AppUser> admins = userRepo.findByRole(Role.ADMIN);
        log.info("[NotifDebug] Found {} admins in DB to notify.", admins.size());

        // Ensure officialAdminEmail is always in the loop for email
        boolean officialAdminInDb = admins.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(officialAdminEmail));
        
        for (AppUser admin : admins) {
            Notification adminNotif = Notification.builder()
                .user(admin)
                .title("Admin: " + title)
                .message(message + " (Bác sĩ: " + appt.getDoctor().getUser().getFullName() + ")")
                .type("APPOINTMENT_BOOKED")
                .relatedId(appt.getId())
                .build();
            notificationRepo.save(adminNotif);
            log.info("[NotifDebug] Saved in-app notification for admin: {}", admin.getEmail());

            // Send Email to Admin
            if (mailEnabled) {
                sendAdminEmail(admin.getEmail(), appt);
            }
        }

        // 2b. Force email to officialAdminEmail if not already sent as an admin
        if (mailEnabled && !officialAdminInDb) {
            log.info("[NotifDebug] Force sending email to official super-admin: {}", officialAdminEmail);
            report.append("Sending to official admin: ").append(officialAdminEmail).append(" -> ");
            report.append(sendAdminEmailDebug(officialAdminEmail, appt)).append("\n");
        }

        // ... existing logic for patient/in-app notifs ...
        report.append("Execution finished successfully.");
        return report.toString();
    }

    private String sendAdminEmailDebug(String email, Appointment appt) {
        // reuse existing HTML generator but return status
        String adminHtml = String.format("""
            <div style="font-family:Arial,sans-serif; padding:20px; border:1px solid #eee; border-radius:8px;">
                <h3 style="color:#ef4444;">[Hệ thống] Có lịch hẹn mới vừa đặt (Debug)</h3>
                <p><strong>Bệnh nhân:</strong> %s</p>
                <p><strong>Bác sĩ:</strong> %s</p>
                <p><strong>Thời gian:</strong> %s</p>
            </div>
            """, 
            appt.getPatient().getFullName(),
            appt.getDoctor().getUser().getFullName(),
            appt.getTimeSlot().getStartAt().toString()
        );
        return mailService.sendHtmlDebug(email, "[Debug] Lịch khám mới", adminHtml);
    }

    @EventListener
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            log.warn("[NotifDebug] Cancelled Appointment {} NOT FOUND in listener.", event.appointmentId());
            return;
        }

        String title = "Lịch hẹn đã bị hủy";
        String message = String.format("Lịch hẹn lúc %s của bệnh nhân %s đã bị hủy bởi %s",
            appt.getTimeSlot().getStartAt().toString(),
            appt.getPatient().getFullName(),
            event.cancelledBy().equals("DOCTOR") ? "Bác sĩ" : "Bệnh nhân");

        // 1. Notify the OTHER party
        AppUser targetUser = event.cancelledBy().equals("DOCTOR") ? appt.getPatient() : appt.getDoctor().getUser();
        Notification cancelNotif = Notification.builder()
            .user(targetUser)
            .title(title)
            .message(message)
            .type("APPOINTMENT_CANCELLED")
            .relatedId(appt.getId())
            .build();
        notificationRepo.save(cancelNotif);

        // 2. Notify Admins
        List<AppUser> admins = userRepo.findByRole(Role.ADMIN);
        for (AppUser admin : admins) {
            notificationRepo.save(Notification.builder()
                .user(admin)
                .title("Admin: " + title)
                .message(message)
                .type("APPOINTMENT_CANCELLED")
                .relatedId(appt.getId())
                .build());
        }

        // 3. Send Email to Both
        if (mailEnabled) {
            String patientEmail = appt.getPatient().getEmail();
            String docEmail = appt.getDoctor().getUser().getEmail();
            
            String html = String.format("""
                <div style="font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #ef4444; border-radius:12px; padding:24px;">
                    <h2 style="color:#ef4444;">Thông báo hủy lịch hẹn — MediBook</h2>
                    <p>Chào bạn,</p>
                    <p>Chúng tôi rất tiếc phải thông báo rằng lịch hẹn sau đã bị hủy:</p>
                    <div style="background:#fef2f2; padding:16px; border-radius:8px; border-left:4px solid #ef4444;">
                        <p><strong>Bệnh nhân:</strong> %s</p>
                        <p><strong>Bác sĩ:</strong> %s</p>
                        <p><strong>Thời gian:</strong> %s</p>
                        <p><strong>Người thực hiện hủy:</strong> %s</p>
                    </div>
                    <p style="margin-top:16px;">Vui lòng truy cập hệ thống nếu bạn muốn đặt lại lịch mới.</p>
                </div>
                """, 
                appt.getPatient().getFullName(),
                appt.getDoctor().getUser().getFullName(),
                appt.getTimeSlot().getStartAt().toString(),
                event.cancelledBy().equals("DOCTOR") ? "Bác sĩ" : "Bệnh nhân"
            );

            try {
                mailService.sendHtml(patientEmail, "MediBook — Thông báo hủy lịch hẹn", html);
                mailService.sendHtml(docEmail, "MediBook — Thông báo hủy lịch hẹn", html);
            } catch (Exception e) {
                log.error("[NotifDebug] Failed to send cancellation email: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            log.warn("[NotifDebug] Confirmed Appointment {} NOT FOUND in listener.", event.appointmentId());
            return;
        }

        // Notify Patient
        Notification confNotif = Notification.builder()
            .user(appt.getPatient())
            .title("Lịch hẹn đã được xác nhận")
            .message(String.format("Bác sĩ %s đã xác nhận lịch hẹn của bạn vào lúc %s",
                appt.getDoctor().getUser().getFullName(),
                appt.getTimeSlot().getStartAt().toString()))
            .type("APPOINTMENT_CONFIRMED")
            .relatedId(appt.getId())
            .build();
        notificationRepo.save(confNotif);

        // Send Email to Patient
        if (mailEnabled) {
            String patientEmail = appt.getPatient().getEmail();
            String html = String.format("""
                <div style="font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #2563eb; border-radius:12px; padding:24px;">
                    <h2 style="color:#2563eb;">Lịch hẹn đã được xác nhận — MediBook</h2>
                    <p>Chào bạn <strong>%s</strong>,</p>
                    <p>Bác sĩ đã xác nhận lịch hẹn của bạn. Thông tin chi tiết:</p>
                    <div style="background:#eff6ff; padding:16px; border-radius:8px;">
                        <p><strong>Bác sĩ:</strong> %s</p>
                        <p><strong>Thời gian:</strong> %s</p>
                        <p><strong>Địa điểm:</strong> %s</p>
                    </div>
                    <p style="margin-top:16px;">Hẹn gặp lại bạn tại phòng khám!</p>
                </div>
                """, 
                appt.getPatient().getFullName(),
                appt.getDoctor().getUser().getFullName(),
                appt.getTimeSlot().getStartAt().toString(),
                appt.getDoctor().getClinicName()
            );
            try {
                mailService.sendHtml(patientEmail, "MediBook — Lịch hẹn đã được xác nhận", html);
            } catch (Exception e) {
                log.error("[NotifDebug] Failed to send confirmation email to patient: {}", e.getMessage());
            }
        }
    }

    private void sendAdminEmail(String email, Appointment appt) {
        String adminHtml = String.format("""
            <div style="font-family:Arial,sans-serif; padding:20px; border:1px solid #eee; border-radius:8px;">
                <h3 style="color:#ef4444;">[Hệ thống] Có lịch hẹn mới vừa đặt</h3>
                <p><strong>Bệnh nhân:</strong> %s</p>
                <p><strong>Bác sĩ:</strong> %s</p>
                <p><strong>Thời gian:</strong> %s</p>
                <p><strong>Ghi chú từ BN:</strong> %s</p>
                <hr style="border:0; border-top:1px solid #eee;">
                <p style="font-size:12px; color:#666;">Thông báo này được gửi tự động từ hệ thống MediBook.</p>
            </div>
            """, 
            appt.getPatient().getFullName(),
            appt.getDoctor().getUser().getFullName(),
            appt.getTimeSlot().getStartAt().toString(),
            appt.getPatientNote() != null ? appt.getPatientNote() : "Không có"
        );
        try {
            log.info("[NotifDebug] Attempting to send Admin email to: {}", email);
            mailService.sendHtml(email, "[Admin Alert] Lịch khám mới được đặt", adminHtml);
        } catch (Exception e) {
            log.error("[NotifDebug] Failed to send email to {}: {}", email, e.getMessage());
        }
    }
}
