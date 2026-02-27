package com.example.medibook;

import com.example.medibook.model.*;
import com.example.medibook.repo.*;
import com.example.medibook.service.AppointmentService;
import com.example.medibook.service.SlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.medibook.exception.ConflictException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingConcurrencyTest extends AbstractIntegrationTest {

    @Autowired private AppointmentService appointmentService;
    @Autowired private TimeSlotRepository slotRepo;
    @Autowired private AppUserRepository userRepo;
    @Autowired private DoctorProfileRepository doctorRepo;
    @Autowired private SpecialtyRepository specialtyRepo;
    @Autowired private SlotService slotService;

    @Test
    void testDoubleBookingRaceCondition() throws InterruptedException {
        // Setup
        Specialty s = specialtyRepo.save(Specialty.builder().name("Test Spec").code("TEST").build());
        AppUser dUser = userRepo.save(AppUser.builder().email("doc@test.com").fullName("Doc").role(Role.DOCTOR).passwordHash("x").build());
        DoctorProfile doc = doctorRepo.save(DoctorProfile.builder().user(dUser).specialty(s).consultFeeVnd(100L).build());
        
        AppUser p1 = userRepo.save(AppUser.builder().email("p1@test.com").fullName("P1").role(Role.USER).passwordHash("x").build());
        AppUser p2 = userRepo.save(AppUser.builder().email("p2@test.com").fullName("P2").role(Role.USER).passwordHash("x").build());

        // Generate 1 slot
        WorkingHour wh = WorkingHour.builder().doctor(doc).dayOfWeek((short)LocalDate.now().getDayOfWeek().getValue())
            .startTime(java.time.LocalTime.of(9,0)).endTime(java.time.LocalTime.of(9,30)).slotMinutes((short)30).build();
        // Manually save slot since we don't assume workingHourRepo usage here
        TimeSlot slot = slotRepo.save(TimeSlot.builder()
            .doctor(doc)
            .startAt(java.time.Instant.now())
            .endAt(java.time.Instant.now().plusSeconds(1800))
            .status(TimeSlotStatus.AVAILABLE)
            .build());

        // Execute concurrent booking
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        java.util.function.Consumer<AppUser> bookTask = (user) -> {
            try {
                latch.await();
                appointmentService.book(slot.getId(), user.getId(), "Race");
                successCount.incrementAndGet();
            } catch (ConflictException | InterruptedException e) {
                failCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        executor.submit(() -> bookTask.accept(p1));
        executor.submit(() -> bookTask.accept(p2));

        latch.countDown(); // Start race
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        // Verify
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);
        
        TimeSlot finalSlot = slotRepo.findById(slot.getId()).orElseThrow();
        assertThat(finalSlot.getStatus()).isEqualTo(TimeSlotStatus.BOOKED);
    }
}
