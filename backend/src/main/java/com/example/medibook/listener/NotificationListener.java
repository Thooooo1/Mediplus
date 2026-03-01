package com.example.medibook.listener;

import com.example.medibook.controller.AdminController;
import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.events.AppointmentCancelledEvent;
import com.example.medibook.events.AppointmentConfirmedEvent;
import com.example.medibook.events.SystemAlertEvent;
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

import com.example.medibook.utils.EmailTemplateUtils;
import java.util.List;
import java.util.Map;

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

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("[Notif] Mail Enabled Status: {}", mailEnabled);
        AdminController.addLog("NotificationListener initialized. Mail Enabled: " + mailEnabled);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        String logMsg = "Handling booked event (Post-Commit v2.3): " + event.appointmentId();
        log.info("[Notif-v2.3] " + logMsg);
        AdminController.addLog(logMsg);
        
        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            AdminController.addLog("CRITICAL: Appointment still not found even after Commit: " + event.appointmentId());
            return;
        }
        AdminController.addLog("Found appointment for: " + appt.getPatient().getFullName());

        String title = "Lịch hẹn mới";
        String message = String.format("Bệnh nhân %s đã đặt lịch vào lúc %s", 
            appt.getPatient().getFullName(), 
            appt.getTimeSlot().getStartAt().toString());
        
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

        if (mailEnabled) {
            log.info("[Notif] Sending email to Doctor: {}", appt.getDoctor().getUser().getEmail());
            String docEmail = appt.getDoctor().getUser().getEmail();
            String html = EmailTemplateUtils.getProfessionalTemplate(
                "THÔNG BÁO LỊCH KHÁM MỚI",
                "Bác sĩ " + appt.getDoctor().getUser().getFullName(),
                "Bạn vừa có một lịch hẹn mới được đặt qua hệ thống MediBook. Vui lòng kiểm tra và xác nhận lịch hẹn này.",
                Map.of(
                    "Bệnh nhân", appt.getPatient().getFullName(),
                    "Thời gian", appt.getTimeSlot().getStartAt().toString(),
                    "Phòng khám", appt.getDoctor().getClinicName(),
                    "Ghi chú", appt.getPatientNote() != null ? appt.getPatientNote() : "Không có"
                ),
                "https://medibook-v2.vercel.app/doctor/appointments.html",
                "Xem chi tiết lịch hẹn",
                "#2563eb"
            );
            mailService.sendHtml(docEmail, "MediBook — Thông báo lịch khám mới", html);
        }

        // 2. Notify Admins
        List<AppUser> admins = userRepo.findByRole(Role.ADMIN);
        boolean officialAdminInDb = admins.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(officialAdminEmail));
        
        for (AppUser admin : admins) {
            notificationRepo.save(Notification.builder()
                .user(admin)
                .title("Admin: " + title)
                .message(message + " (Bác sĩ: " + appt.getDoctor().getUser().getFullName() + ")")
                .type("APPOINTMENT_BOOKED")
                .relatedId(appt.getId())
                .build());
            
            if (mailEnabled) {
                log.info("[Notif] Attempting to send email to Admin: {}", admin.getEmail());
                notifyAdmin(admin.getEmail(), appt, "[Admin Alert v2.4] CÓ LỊCH HẸN MỚI");
            }
        }

        if (mailEnabled && !officialAdminInDb) {
            notifyAdmin(officialAdminEmail, appt, "[Admin Alert v2.4] CÓ LỊCH HẸN MỚI");
        }

        // 3. Notify Patient
        if (mailEnabled) {
            String patientEmail = appt.getPatient().getEmail();
            String patientHtml = EmailTemplateUtils.getProfessionalTemplate(
                "ĐẶT LỊCH KHÁM THÀNH CÔNG",
                appt.getPatient().getFullName(),
                "Chúc mừng bạn đã đặt lịch khám thành công tại MediBook. Dưới đây là thông tin chi tiết lịch hẹn của bạn.",
                Map.of(
                    "Bác sĩ", appt.getDoctor().getUser().getFullName(),
                    "Thời gian", appt.getTimeSlot().getStartAt().toString(),
                    "Địa điểm", appt.getDoctor().getClinicName(),
                    "Chuyên khoa", appt.getDoctor().getSpecialty().getName()
                ),
                "https://medibook-v2.vercel.app/patient/appointments.html",
                "Quản lý lịch hẹn",
                "#10b981"
            );
            mailService.sendHtml(patientEmail, "MediBook — Xác nhận lịch hẹn thành công", patientHtml);
        }

        notificationRepo.save(Notification.builder()
            .user(appt.getPatient())
            .title("Đặt lịch thành công")
            .message(message)
            .type("APPOINTMENT_CONFIRMED")
            .relatedId(appt.getId())
            .build());

        log.info("[Notif] All notifications processed for appointment {}", appt.getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        String logMsg = "Handling cancelled event (Post-Commit v2.3): " + event.appointmentId();
        log.info("[Notif-v2.3] " + logMsg);
        AdminController.addLog(logMsg);

        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            AdminController.addLog("CRITICAL: Cancelled appt not found after Commit: " + event.appointmentId());
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
            
            String html = EmailTemplateUtils.getProfessionalTemplate(
                "THÔNG BÁO HỦY LỊCH HẸN",
                "Bạn",
                "Chúng tôi rất tiếc phải thông báo rằng lịch hẹn của bạn đã bị hủy trên hệ thống MediBook.",
                Map.of(
                    "Bệnh nhân", appt.getPatient().getFullName(),
                    "Bác sĩ", appt.getDoctor().getUser().getFullName(),
                    "Thời gian", appt.getTimeSlot().getStartAt().toString(),
                    "Người thực hiện hủy", event.cancelledBy().equals("DOCTOR") ? "Bác sĩ" : "Bệnh nhân"
                ),
                null, null,
                "#ef4444"
            );

            try {
                mailService.sendHtml(patientEmail, "MediBook — Thông báo hủy lịch hẹn", html);
                mailService.sendHtml(docEmail, "MediBook — Thông báo hủy lịch hẹn", html);
                
                // Notify Admin using shared helper
                String officialAdminEmail = "tnguyenanh189@gmail.com";
                notifyAdmin(officialAdminEmail, appt, "[Admin Alert v2.4] LỊCH HẸN ĐÃ BỊ HỦY");
                AdminController.addLog("Cancellation emails process completed.");
            } catch (Exception e) {
                AdminController.addLog("FAILED to send cancellation email: " + e.getMessage());
                log.error("[NotifDebug] Failed to send cancellation email: {}", e.getMessage());
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAppointmentConfirmed(AppointmentConfirmedEvent event) {
        AdminController.addLog("Handling confirmed event (Post-Commit v2.3): " + event.appointmentId());
        
        Appointment appt = appointmentRepo.findDetailsById(event.appointmentId()).orElse(null);
        if (appt == null) {
            AdminController.addLog("CRITICAL: Confirmed appt not found after Commit: " + event.appointmentId());
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
            String html = EmailTemplateUtils.getProfessionalTemplate(
                "LỊCH HẸN ĐÃ ĐƯỢC XÁC NHẬN",
                appt.getPatient().getFullName(),
                "Bác sĩ đã xác nhận lịch hẹn của bạn. Thông tin chi tiết lịch hẹn như sau:",
                Map.of(
                    "Bác sĩ", appt.getDoctor().getUser().getFullName(),
                    "Thời gian", appt.getTimeSlot().getStartAt().toString(),
                    "Địa điểm", appt.getDoctor().getClinicName()
                ),
                "https://medibook-v2.vercel.app/patient/appointments.html",
                "Xem lịch hẹn của tôi",
                "#2563eb"
            );
            try {
                AdminController.addLog("Attempting Patient Confirmation email to: " + patientEmail);
                mailService.sendHtml(patientEmail, "MediBook — Lịch hẹn đã được xác nhận", html);
                
                // Notify Admin
                String officialAdminEmail = "tnguyenanh189@gmail.com";
                notifyAdmin(officialAdminEmail, appt, "[Admin Alert v2.4] LỊCH HẸN ĐÃ ĐƯỢC XÁC NHẬN");
            } catch (Exception e) {
                AdminController.addLog("ERROR sending Confirmation email: " + e.getMessage());
                log.error("[NotifDebug] Failed to send confirmation email to patient: {}", e.getMessage());
            }
        }
    }

    private void notifyAdmin(String email, Appointment appt, String subject) {
        String adminHtml = EmailTemplateUtils.getProfessionalTemplate(
            subject,
            "Quản trị viên",
            "Hệ thống vừa ghi nhận một thay đổi quan trọng đối với lịch hẹn sau đây:",
            Map.of(
                "Sự kiện", subject,
                "Bệnh nhân", appt.getPatient().getFullName(),
                "Bác sĩ", appt.getDoctor().getUser().getFullName(),
                "Thời gian", appt.getTimeSlot().getStartAt().toString(),
                "Trạng thái", appt.getStatus().toString(),
                "Ghi chú", appt.getPatientNote() != null ? appt.getPatientNote() : "Không có"
            ),
            "https://medibook-v2.vercel.app/admin/appointments.html",
            "Quản lý hệ thống",
            "#2563eb"
        );
        try {
            AdminController.addLog("Attempting Admin email (" + subject + ") to: " + email);
            mailService.sendHtml(email, subject, adminHtml);
            AdminController.addLog("Admin email sent via Resend for: " + email);
        } catch (Exception e) {
            AdminController.addLog("ERROR sending to Admin " + email + ": " + e.getMessage());
            log.error("[Notif] Error notifying admin {}: {}", email, e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSystemAlert(SystemAlertEvent event) {
        String officialAdminEmail = "tnguyenanh189@gmail.com";
        log.info("[SystemAlert] {}: {}", event.type(), event.title());
        AdminController.addLog("System Alert Event: " + event.title());

        if (mailEnabled) {
            String html = EmailTemplateUtils.getProfessionalTemplate(
                "HỆ THỐNG: " + event.title(),
                "Quản trị viên",
                event.message(),
                event.details(),
                "https://medibook-v2.vercel.app/admin/dashboard.html",
                "Truy cập Dashboard",
                "#4b5563"
            );
            try {
                AdminController.addLog("Attempting System Alert email (" + event.title() + ") to: " + officialAdminEmail);
                mailService.sendHtml(officialAdminEmail, "MediBook Alert — " + event.title(), html);
            } catch (Exception e) {
                AdminController.addLog("ERROR sending System Alert: " + e.getMessage());
                log.error("[SystemAlert] Failed to notify admin: {}", e.getMessage());
            }
        }
    }
}
