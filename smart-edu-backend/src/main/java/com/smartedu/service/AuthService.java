package com.smartedu.service;

import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.SysUserMapper;
import com.smartedu.model.dto.LoginRequest;
import com.smartedu.model.dto.LoginResponse;
import com.smartedu.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务：登录校验 + Token 管理
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_LOGIN_FAIL = 5;
    private static final int LOCK_MINUTES = 30;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(BizError.USER_NOT_FOUND);
        }

        // 检查锁定状态
        if (user.getStatus() == 0) {
            throw new BusinessException(BizError.ACCOUNT_DISABLED);
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(BizError.ACCOUNT_LOCKED);
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= MAX_LOGIN_FAIL) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                user.setStatus(2);
            }
            sysUserMapper.updateById(user);
            throw new BusinessException(BizError.PASSWORD_ERROR);
        }

        // 登录成功，重置失败计数
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        if (user.getStatus() == 2) user.setStatus(1);
        sysUserMapper.updateById(user);

        // 生成角色列表
        String roleCode = switch (user.getUserType()) {
            case 1 -> "STUDENT";
            case 2 -> "TEACHER";
            case 3 -> "ACADEMIC";
            case 4 -> "ADMIN";
            case 5 -> "QB_ADMIN";
            default -> "STUDENT";
        };
        List<String> roles = Collections.singletonList(roleCode);

        // 签发 Token
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 缓存 refreshToken → userId（7天）
        redisTemplate.opsForValue().set("refresh:" + refreshToken, user.getId().toString(),
                Duration.ofDays(7));

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .userType(user.getUserType())
                        .department(user.getDepartment())
                        .major(user.getMajor())
                        .build())
                .build();
    }

    /**
     * 登出
     */
    public void logout(String refreshToken) {
        redisTemplate.delete("refresh:" + refreshToken);
    }

    /**
     * 刷新 Token
     */
    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(BizError.UNAUTHORIZED);
        }
        String userIdStr = (String) redisTemplate.opsForValue().get("refresh:" + refreshToken);
        if (userIdStr == null) {
            throw new BusinessException(BizError.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(userIdStr);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizError.USER_NOT_FOUND);
        }

        String roleCode = switch (user.getUserType()) {
            case 1 -> "STUDENT"; case 2 -> "TEACHER";
            case 3 -> "ACADEMIC"; case 4 -> "ADMIN";
            case 5 -> "QB_ADMIN"; default -> "STUDENT";
        };

        // 删除旧 refreshToken
        redisTemplate.delete("refresh:" + refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(),
                Collections.singletonList(roleCode));
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        redisTemplate.opsForValue().set("refresh:" + newRefreshToken, user.getId().toString(),
                Duration.ofDays(7));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .userType(user.getUserType())
                        .department(user.getDepartment())
                        .major(user.getMajor())
                        .build())
                .build();
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse.UserInfo getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) throw new BusinessException(BizError.USER_NOT_FOUND);
        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .department(user.getDepartment())
                .major(user.getMajor())
                .build();
    }
}
