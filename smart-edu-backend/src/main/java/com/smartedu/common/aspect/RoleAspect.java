package com.smartedu.common.aspect;

import com.smartedu.common.annotation.RequireRole;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysUser;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 角色校验切面 — 配合 @RequireRole 使用
 */
@Aspect
@Component
public class RoleAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof SysUser user)) {
            throw new BusinessException(BizError.UNAUTHORIZED);
        }

        String userTypeName = switch (user.getUserType()) {
            case 1 -> "STUDENT";
            case 2 -> "TEACHER";
            case 3 -> "ACADEMIC";
            case 4 -> "ADMIN";
            case 5 -> "QB_ADMIN";
            default -> "";
        };

        String[] required = requireRole.value();
        if (required.length > 0 && Arrays.stream(required).noneMatch(r -> r.equalsIgnoreCase(userTypeName))) {
            throw new BusinessException(BizError.FORBIDDEN);
        }
    }
}
