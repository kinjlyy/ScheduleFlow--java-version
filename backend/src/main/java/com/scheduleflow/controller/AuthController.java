package com.scheduleflow.controller;

import com.scheduleflow.dto.AuthRequest;
import com.scheduleflow.dto.AuthResponse;
import com.scheduleflow.dto.RegisterRequest;
import com.scheduleflow.model.User;
import com.scheduleflow.repository.UserRepository;
import com.scheduleflow.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints for the Timetable Service.
 *
 * <p><strong>Service Boundary Note:</strong> Authentication (User entity, JWT) is temporarily
 * hosted in this service. In a future phase, this will be extracted into a dedicated Auth Service.
 * At that point, this controller will be removed and token validation will happen at the API Gateway.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/auth/register — create a new account</li>
 *   <li>POST /api/auth/login    — obtain a JWT token</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getName());
        return ResponseEntity.ok(new AuthResponse(token, user.getName(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            String token = jwtUtil.generateToken(user.getEmail(), user.getName());
            return ResponseEntity.ok(new AuthResponse(token, user.getName(), user.getEmail()));
        } else {
            return ResponseEntity.status(401).body("Error: Invalid credentials");
        }
    }
}
