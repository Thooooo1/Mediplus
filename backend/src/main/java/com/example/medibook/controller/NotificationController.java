package com.example.medibook.controller;

import com.example.medibook.model.Notification;
import com.example.medibook.repo.NotificationRepository;
import com.example.medibook.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo;

    @GetMapping
    public Page<Notification> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(principal.getId(), pageable);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationRepo.countByUserIdAndReadFalse(principal.getId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        Notification n = notificationRepo.findById(id).orElse(null);
        if (n != null && n.getUser().getId().equals(principal.getId())) {
            n.setRead(true);
            notificationRepo.save(n);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationRepo.markAllReadByUserId(principal.getId());
        return ResponseEntity.ok().build();
    }
}
