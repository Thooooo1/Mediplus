package com.example.medibook.repo;

import com.example.medibook.model.AppUser;
import com.example.medibook.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    Page<AppUser> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
    Page<AppUser> findByRole(Role role, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE AppUser u SET u.passwordHash = :hash WHERE u.email LIKE '%@medibook.vn%'")
    int fixSeedPasswords(@Param("hash") String hash);
}
