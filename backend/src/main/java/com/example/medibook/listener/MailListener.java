package com.example.medibook.listener;

import com.example.medibook.events.AppointmentBookedEvent;
import com.example.medibook.model.Appointment;
import com.example.medibook.repo.AppointmentRepository;
import com.example.medibook.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class MailListener {

  private final AppointmentRepository appointmentRepo;
  private final MailService mailService;

  @Value("${app.mail.enabled:false}")
  private boolean mailEnabled;

  private final DateTimeFormatter fmt =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBooked(AppointmentBookedEvent e) {
    if (!mailEnabled) return;

    Appointment a = appointmentRepo.findDetailsById(e.appointmentId()).orElse(null);
    if (a == null) return;

    String when = fmt.format(a.getTimeSlot().getStartAt());
    String html = """
      <div style="font-family:Arial,sans-serif">
        <h2>Xác nhận đặt lịch</h2>
        <p>Chào %s,</p>
        <p>Bạn đã đặt lịch thành công.</p>
        <ul>
          <li><b>Bác sĩ</b>: %s</li>
          <li><b>Thời gian</b>: %s (Asia/Ho_Chi_Minh)</li>
          <li><b>Ghi chú</b>: %s</li>
        </ul>
        <p>Cảm ơn bạn.</p>
      </div>
      """.formatted(
        a.getPatient().getFullName(),
        a.getDoctor().getUser().getFullName(),
        when,
        a.getPatientNote() == null ? "" : a.getPatientNote()
      );

    log.info("Sending booking confirmation for appointment {}", a.getId());
    try {
        mailService.sendHtml(a.getPatient().getEmail(), "Xác nhận đặt lịch", html);
    } catch (Exception ex) {
        log.error("Failed to send booking confirmation for appointment {}: {}", a.getId(), ex.getMessage());
    }
  }
}
