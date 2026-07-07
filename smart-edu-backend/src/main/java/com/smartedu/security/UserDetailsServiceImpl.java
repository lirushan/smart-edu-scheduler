package com.smartedu.security;

import com.smartedu.entity.SysUser;
import com.smartedu.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService 实现 — 从 DB 加载用户 + 角色
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // 角色：格式 "ROLE_STUDENT" 用于 hasRole()
        String role = switch (sysUser.getUserType()) {
            case 1 -> "ROLE_STUDENT";
            case 2 -> "ROLE_TEACHER";
            case 3 -> "ROLE_ACADEMIC";
            case 4 -> "ROLE_ADMIN";
            case 5 -> "ROLE_QB_ADMIN";
            default -> "ROLE_STUDENT";
        };
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(Collections.singleton(() -> role))
                .accountLocked(sysUser.getStatus() == 2)
                .disabled(sysUser.getStatus() == 0)
                .build();
    }
}
