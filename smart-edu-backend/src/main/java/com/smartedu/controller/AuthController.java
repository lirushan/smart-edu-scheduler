package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.model.dto.LoginRequest;
import com.smartedu.model.dto.LoginResponse;
import com.smartedu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return Result.ok();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return Result.ok(authService.refresh(body.get("refreshToken")));
    }

    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> me() {
        com.smartedu.entity.SysUser user = (com.smartedu.entity.SysUser)
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
        return Result.ok(authService.getCurrentUser(user.getId()));
    }
}
