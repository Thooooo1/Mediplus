package com.example.medibook.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserPrincipalService userPrincipalService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws ServletException, IOException {

    String path = request.getRequestURI();
    if (path.equals("/api/auth/test-hash") || path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
      chain.doFilter(request, response);
      return;
    }

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = auth.substring(7);
    if (!jwtService.isValid(token)) {
      chain.doFilter(request, response);
      return;
    }

    String userId = jwtService.extractUserId(token);
    try {
      UserPrincipal principal = userPrincipalService.loadUserById(userId);
      var authentication = new UsernamePasswordAuthenticationToken(
        principal, null, principal.getAuthorities()
      );
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception e) {
      // Token references a user that no longer exists (e.g. after DB reset) — skip auth
    }

    chain.doFilter(request, response);
  }
}
