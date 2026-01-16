package com.asset.module.system.service;

import com.asset.common.exception.BusinessException;
import com.asset.common.service.CaptchaService;
import com.asset.common.service.LoginAttemptService;
import com.asset.module.system.dto.LoginDTO;
import com.asset.module.system.dto.LoginVO;
import com.asset.module.system.entity.SysUser;
import com.asset.module.system.mapper.SysUserMapper;
import com.asset.security.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;
    
    public LoginVO login(LoginDTO dto) {
        log.info("=== 登录请求 - 用户名: {}", dto.getUsername());
        
        // 验证验证码
        if (!captchaService.verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode())) {
            log.warn("验证码错误 - 用户: {}", dto.getUsername());
            throw new BusinessException("验证码错误或已过期");
        }
        
        // 检查账号是否被锁定
        if (loginAttemptService.isBlocked(dto.getUsername())) {
            long remainingMinutes = loginAttemptService.getLockRemainingMinutes(dto.getUsername());
            log.warn("账号已被锁定 - 用户: {}, 剩余时间: {}分钟", dto.getUsername(), remainingMinutes);
            throw new BusinessException("账号已被锁定，请" + remainingMinutes + "分钟后再试");
        }
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            log.warn("用户不存在: {}", dto.getUsername());
            loginAttemptService.loginFailed(dto.getUsername());
            int remaining = loginAttemptService.getRemainingAttempts(dto.getUsername());
            throw new BusinessException("用户名或密码错误，剩余尝试次数: " + remaining);
        }
        
        log.info("找到用户 - ID: {}, 用户名: {}", user.getId(), user.getUsername());
        
        // BCrypt密码验证
        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        log.info("BCrypt 密码匹配结果: {}", matches);
        
        if (!matches) {
            log.warn("密码不匹配 - 用户: {}", dto.getUsername());
            loginAttemptService.loginFailed(dto.getUsername());
            int remaining = loginAttemptService.getRemainingAttempts(dto.getUsername());
            if (remaining > 0) {
                throw new BusinessException("用户名或密码错误，剩余尝试次数: " + remaining);
            } else {
                throw new BusinessException("登录失败次数过多，账号已被锁定15分钟");
            }
        }
        
        if (user.getStatus() == 0) {
            log.warn("账号已禁用 - 用户: {}", dto.getUsername());
            throw new BusinessException("账号已被禁用");
        }
        
        // 登录成功，清除失败记录
        loginAttemptService.loginSucceeded(dto.getUsername());
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("✓✓✓ 登录成功 - 用户: {}, Token已生成 ✓✓✓", dto.getUsername());
        
        // 获取用户权限
        List<String> permissions = permissionService.getUserPermissions(user.getId())
                .stream()
                .map(p -> p.getPermissionCode())
                .collect(java.util.stream.Collectors.toList());
        
        LoginVO loginVO = new LoginVO(token, user.getId(), user.getUsername(), user.getRealName());
        loginVO.setPermissions(permissions);
        return loginVO;
    }
}
