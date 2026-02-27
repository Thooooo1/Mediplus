package com.example.medibook.controller;

import com.example.medibook.dto.AuthDtos;
import com.example.medibook.security.UserPrincipal;
import com.example.medibook.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

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
}
