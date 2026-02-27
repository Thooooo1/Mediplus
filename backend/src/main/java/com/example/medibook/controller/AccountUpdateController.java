package com.example.medibook.controller;

import com.example.medibook.model.AppUser;
import com.example.medibook.model.Role;
import com.example.medibook.repo.AppUserRepository;
import com.example.medibook.utils.StringNormalizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@Slf4j
public class AccountUpdateController {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/sync-emails-gmail")
    public String syncEmails() {
        List<AppUser> users = userRepo.findAll();
        int count = 0;
        String commonPass = encoder.encode("Password@123");

        for (AppUser u : users) {
            String oldEmail = u.getEmail();
            String newEmail = oldEmail;

            if (u.getRole() == Role.ADMIN) {
                newEmail = "admin@gmail.com";
            } else if (u.getRole() == Role.DOCTOR) {
                newEmail = StringNormalizationUtils.toEmailFormat(u.getFullName());
            } else if (u.getRole() == Role.USER && oldEmail.endsWith(".local")) {
                newEmail = oldEmail.replace("@medibook.local", "@gmail.com");
            }

            u.setEmail(newEmail);
            u.setPasswordHash(commonPass);
            userRepo.save(u);
            log.info("Updated {} -> {}", oldEmail, newEmail);
            count++;
        }
        return "Updated " + count + " accounts to @gmail.com and reset passwords to Password@123";
    }
}
