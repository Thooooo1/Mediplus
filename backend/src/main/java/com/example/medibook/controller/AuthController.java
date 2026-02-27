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

  @GetMapping("/test-lookup")
  public org.springframework.http.ResponseEntity<?> testLookup(@RequestParam String email, com.example.medibook.repo.AppUserRepository repo) {
    String e = email.trim().toLowerCase();
    Optional<com.example.medibook.model.AppUser> u = repo.findByEmail(e);
    return org.springframework.http.ResponseEntity.ok(u.isPresent() ? "FOUND: " + u.get().getEmail() : "NOT FOUND: " + e);
  }
}
