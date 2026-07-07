package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.SysUser;
import com.smartedu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/my")
    public Result<List<MenuService.MenuTreeNode>> myMenus() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.ok(menuService.getUserMenuTree(user.getId()));
    }
}
