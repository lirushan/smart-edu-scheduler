package com.smartedu.controller;

import com.smartedu.common.PageResult;
import com.smartedu.common.Result;
import com.smartedu.entity.SysRole;
import com.smartedu.service.RoleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.ok(roleService.listRoles(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<RoleService.RoleDetail> detail(@PathVariable Long id) {
        return Result.ok(roleService.getRoleDetail(id));
    }

    @GetMapping("/menus/tree")
    public Result<List<RoleService.MenuNode>> menuTree() {
        return Result.ok(roleService.listMenuTree());
    }

    @PostMapping
    public Result<SysRole> create(@Valid @RequestBody CreateRoleRequest request) {
        return Result.ok(roleService.createRole(request.getRoleName(),
                request.getRoleCode(), request.getDescription()));
    }

    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return Result.ok(roleService.updateRole(id, request.getRoleName(), request.getDescription()));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        roleService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PutMapping("/{id}/menus")
    public Result<Void> updateMenus(@PathVariable Long id, @RequestBody RoleMenusRequest request) {
        roleService.updateRoleMenus(id, request.getMenuIds());
        return Result.ok();
    }

    @Data
    public static class CreateRoleRequest {
        @NotBlank(message = "角色名称不能为空")
        private String roleName;

        @NotBlank(message = "角色编码不能为空")
        private String roleCode;

        private String description;
    }

    @Data
    public static class UpdateRoleRequest {
        @NotBlank(message = "角色名称不能为空")
        private String roleName;

        private String description;
    }

    @Data
    public static class RoleMenusRequest {
        private List<Long> menuIds;
    }
}
