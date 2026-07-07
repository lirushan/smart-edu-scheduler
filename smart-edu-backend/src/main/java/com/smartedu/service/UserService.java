package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.PageResult;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户管理服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     */
    public PageResult<SysUser> listUsers(Integer page, Integer size, String keyword, Integer userType) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getDepartment, keyword));
        }
        if (userType != null) {
            wrapper.eq(SysUser::getUserType, userType);
        }
        wrapper.orderByAsc(SysUser::getCreateTime);

        Page<SysUser> pageResult = sysUserMapper.selectPage(Page.of(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 新增用户
     */
    public SysUser createUser(SysUser user) {
        SysUser existing = sysUserMapper.selectByUsername(user.getUsername());
        if (existing != null) {
            throw new BusinessException(BizError.CONFLICT.getCode(), "用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "password123"));
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user;
    }

    /**
     * 编辑用户
     */
    public SysUser updateUser(Long id, SysUser user) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) throw new BusinessException(BizError.USER_NOT_FOUND);

        if (StringUtils.hasText(user.getRealName())) existing.setRealName(user.getRealName());
        if (user.getUserType() != null) existing.setUserType(user.getUserType());
        if (user.getDepartment() != null) existing.setDepartment(user.getDepartment());
        if (user.getMajor() != null) existing.setMajor(user.getMajor());
        if (user.getGrade() != null) existing.setGrade(user.getGrade());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (StringUtils.hasText(user.getPassword())) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        sysUserMapper.updateById(existing);
        return existing;
    }

    /**
     * 切换用户状态
     */
    public void toggleStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) throw new BusinessException(BizError.USER_NOT_FOUND);
        user.setStatus(status);
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        sysUserMapper.updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) throw new BusinessException(BizError.USER_NOT_FOUND);
        sysUserMapper.deleteById(id);
    }
}
