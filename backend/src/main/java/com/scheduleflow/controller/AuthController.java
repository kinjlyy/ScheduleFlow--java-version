package com.scheduleflow.controller;

import com.scheduleflow.dto.AuthRequest;
import com.scheduleflow.dto.AuthResponse;
import com.scheduleflow.dto.RegisterRequest;
import com.scheduleflow.model.User;
import com.scheduleflow.repository.UserRepository;
import com.scheduleflow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
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
