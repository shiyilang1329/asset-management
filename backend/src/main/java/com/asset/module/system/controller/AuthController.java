package com.asset.module.system.controller;

import com.asset.common.result.Result;
import com.asset.common.service.CaptchaService;
import com.asset.common.service.LoginAttemptService;
import com.asset.module.system.dto.LoginDTO;
import com.asset.module.system.dto.LoginVO;
import com.asset.module.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;
    
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码")
    public Result<Map<String, String>> getCaptcha() {
        String[] captcha = captchaService.generateCaptcha();
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captcha[0]);
        result.put("captchaImage", captcha[1]);
        return Result.success(result);
    }
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        return Result.success();
    }
    
    @PostMapping("/unlock/{username}")
    @Operation(summary = "解锁账号（管理员）")
    public Result<Void> unlockAccount(@PathVariable String username) {
        loginAttemptService.unlock(username);
        return Result.success();
    }
    
    @GetMapping("/check/{username}")
    @Operation(summary = "检查账号状态")
    public Result<String> checkAccountStatus(@PathVariable String username) {
        if (loginAttemptService.isBlocked(username)) {
            long remainingMinutes = loginAttemptService.getLockRemainingMinutes(username);
            return Result.success("账号已被锁定，剩余时间: " + remainingMinutes + "分钟");
        }
        int remaining = loginAttemptService.getRemainingAttempts(username);
        return Result.success("账号正常，剩余尝试次数: " + remaining);
    }
}
