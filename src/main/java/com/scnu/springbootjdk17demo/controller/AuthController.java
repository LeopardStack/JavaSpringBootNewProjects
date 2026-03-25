package com.scnu.springbootjdk17demo.controller;


import com.scnu.springbootjdk17demo.dto.LoginRequest;
import com.scnu.springbootjdk17demo.dto.LoginResponse;
import com.scnu.springbootjdk17demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse resp = userService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.status(resp.getCode() == 200 ? 200 : resp.getCode()).body(resp);
    }
}