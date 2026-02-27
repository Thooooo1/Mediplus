package com.example.medibook.controller;

import com.example.medibook.dto.AuthDtos;
import com.example.medibook.security.UserPrincipal;
import com.example.medibook.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final com.example.medibook.repo.AppUserRepository appUserRepository;

  @PostMapping("/register")
  public AuthDtos.AuthRes register(@Valid @RequestBody AuthDtos.RegisterReq req) {
    return authService.register(req);
  }

  @PostMapping("/login")
  public AuthDtos.AuthRes login(@Valid @RequestBody AuthDtos.LoginReq req) {
    return authService.login(req);
  }

  @GetMapping("/me")
  public AuthDtos.MeRes me(@AuthenticationPrincipal UserPrincipal p) {
    return new AuthDtos.MeRes(p.getId().toString(), p.getRole(), p.getEmail(), p.getUsername());
  }

  @PostMapping("/forgot-password")
  public AuthDtos.MessageRes forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordReq req) {
    return authService.forgotPassword(req);
  }

  @PostMapping("/reset-password")
  public AuthDtos.MessageRes resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordReq req) {
    return authService.resetPassword(req);
  }

  @GetMapping("/hotfix-passwords")
  public org.springframework.http.ResponseEntity<?> hotfix() {
    String p = "$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W";
    int u = appUserRepository.fixSeedPasswords(p);
    return org.springframework.http.ResponseEntity.ok("Hotfix applied via JPA. Updated " + u + " users.");
  }
}
