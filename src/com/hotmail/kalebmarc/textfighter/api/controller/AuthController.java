package com.hotmail.kalebmarc.textfighter.api.controller;

import com.hotmail.kalebmarc.textfighter.api.JwtUtil;
import com.hotmail.kalebmarc.textfighter.api.dto.LoginRequest;
import com.hotmail.kalebmarc.textfighter.api.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (!"1234".equals(req.password())) {
            return ResponseEntity.status(401).body("비밀번호가 틀렸습니다.");
        }
        String token = jwtUtil.generateToken(req.username());
        return ResponseEntity.ok(new LoginResponse(token, req.username()));
    }
}
