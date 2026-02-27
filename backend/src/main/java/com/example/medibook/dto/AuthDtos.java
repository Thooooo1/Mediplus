package com.example.medibook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {
  public record RegisterReq(
    @Email @NotBlank String email,
    @NotBlank @Size(min=2, max=120) String fullName,
    @NotBlank @Size(min=8, max=100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
             message = "Password must contain at least one letter and one digit")
    String password,
    String phone
  ) {}
  public record LoginReq(@Email @NotBlank String email, @NotBlank String password) {}
  public record AuthRes(String token, String role, String email, String fullName) {}
  public record MeRes(String id, String role, String email, String fullName) {}
  public record ForgotPasswordReq(@Email @NotBlank String email) {}
  public record ResetPasswordReq(
    @NotBlank String token,
    @NotBlank @Size(min=8, max=100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
             message = "Password must contain at least one letter and one digit")
    String newPassword
  ) {}
  public record MessageRes(String message) {}
}
