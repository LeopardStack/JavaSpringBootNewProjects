package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.*;
import com.scnu.springbootjdk17demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse resp = userService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.status(resp.getCode()).body(resp);
    }

    /** 获取当前用户信息（必须带 token） */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal UserInfoResponse user) {
        return ResponseEntity.ok(user);
    }
}