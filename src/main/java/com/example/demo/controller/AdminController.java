package com.example.demo.controller;

import com.example.demo.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${admin.password}")
    private String storedHash;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String inputPassword = req.get("password");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (encoder.matches(inputPassword, storedHash)) {

            String token = JwtUtil.generateToken("admin");

            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body("Invalid password");
    }
}