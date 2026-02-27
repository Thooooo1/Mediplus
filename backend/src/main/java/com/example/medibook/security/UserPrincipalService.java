package com.example.medibook.security;

import com.example.medibook.repo.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPrincipalService implements UserDetailsService {

  private final AppUserRepository userRepo;

  @Override
  public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepo.findByEmail(username.trim().toLowerCase())
      .map(UserPrincipal::new)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  public UserPrincipal loadUserById(String id) {
    return userRepo.findById(UUID.fromString(id))
      .map(UserPrincipal::new)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
