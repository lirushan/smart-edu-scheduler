package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.PageResult;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.SysMenu;
import com.smartedu.entity.SysRole;
import com.smartedu.mapper.SysMenuMapper;
import com.smartedu.mapper.SysRoleMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理服务
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    public PageResult<SysRole> listRoles(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getId);

        Page<SysRole> pageResult = sysRoleMapper.selectPage(Page.of(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords());
    }

    public RoleDetail getRoleDetail(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(BizError.NOT_FOUND.getCode(), "角色不存在");
        }
        return new RoleDetail(role, sysRoleMapper.selectMenuIdsByRoleId(id));
    }

    public List<MenuNode> listMenuTree() {
        List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    /**
     * 创建角色
     */
    @Transactional
    public SysRole createRole(String roleName, String roleCode, String description) {
        // 检查角色编码唯一性
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BizError.CONFLICT.getCode(), "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        role.setStatus(1); // 默认启用
        role.setCreateTime(java.time.LocalDateTime.now());
        role.setUpdateTime(java.time.LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role;
    }

    /**
     * 编辑角色基本信息（不包含菜单权限）
     */
    @Transactional
    public SysRole updateRole(Long id, String roleName, String description) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(BizError.NOT_FOUND.getCode(), "角色不存在");
        }
        role.setRoleName(roleName);
        role.setDescription(description);
        sysRoleMapper.updateById(role);
        return role;
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(BizError.NOT_FOUND.getCode(), "角色不存在");
        }
        role.setStatus(status);
        sysRoleMapper.updateById(role);
    }

    @Transactional
    public void updateRoleMenus(Long id, List<Long> menuIds) {
        if (sysRoleMapper.selectById(id) == null) {
            throw new BusinessException(BizError.NOT_FOUND.getCode(), "角色不存在");
        }

        sysRoleMapper.deleteRoleMenus(id);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds.stream().distinct().toList()) {
            if (menuId != null) {
                sysRoleMapper.insertRoleMenu(id, menuId);
            }
        }
    }

    private List<MenuNode> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParentId() != null ? menu.getParentId() : 0L));
        return buildChildren(0L, parentMap);
    }

    private List<MenuNode> buildChildren(Long parentId, Map<Long, List<SysMenu>> parentMap) {
        List<SysMenu> children = new ArrayList<>(parentMap.getOrDefault(parentId, List.of()));
        children.sort(Comparator.comparing(SysMenu::getSortOrder).thenComparing(SysMenu::getId));
        return children.stream()
                .map(menu -> new MenuNode(
                        menu.getId(),
                        menu.getParentId(),
                        menu.getMenuName(),
                        menu.getPath(),
                        menu.getIcon(),
                        menu.getMenuType(),
                        menu.getSortOrder(),
                        buildChildren(menu.getId(), parentMap)))
                .toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDetail implements Serializable {
        private SysRole role;
        private List<Long> menuIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuNode implements Serializable {
        private Long id;
        private Long parentId;
        private String menuName;
        private String path;
        private String icon;
        private String menuType;
        private Integer sortOrder;
        private List<MenuNode> children;
    }
}
