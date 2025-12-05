package com.instagram.auth.controller;

import com.instagram.auth.dto.AuthDto;
import com.instagram.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthenticationResponse> register(
            @RequestBody AuthDto.RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthDto.AuthenticationResponse> authenticate(
            @RequestBody AuthDto.AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
