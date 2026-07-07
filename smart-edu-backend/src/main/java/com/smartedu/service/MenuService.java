package com.smartedu.service;

import com.smartedu.entity.SysMenu;
import com.smartedu.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务：按角色获取菜单树
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final SysMenuMapper sysMenuMapper;

    /**
     * 构建当前用户的菜单树
     */
    public List<MenuTreeNode> getUserMenuTree(Long userId) {
        List<SysMenu> menus = sysMenuMapper.selectByUserId(userId);
        return buildTree(menus);
    }

    private List<MenuTreeNode> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));

        List<MenuTreeNode> tree = new ArrayList<>();
        List<SysMenu> roots = parentMap.getOrDefault(0L, List.of());
        roots.sort(Comparator.comparing(SysMenu::getSortOrder));

        for (SysMenu root : roots) {
            MenuTreeNode node = convert(root);
            List<SysMenu> children = parentMap.getOrDefault(root.getId(), List.of());
            children.sort(Comparator.comparing(SysMenu::getSortOrder));
            if (!children.isEmpty()) {
                node.setChildren(children.stream().map(this::convert).collect(Collectors.toList()));
            }
            tree.add(node);
        }
        return tree;
    }

    private MenuTreeNode convert(SysMenu menu) {
        return MenuTreeNode.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .menuType(menu.getMenuType())
                .sortOrder(menu.getSortOrder())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MenuTreeNode {
        private Long id;
        private Long parentId;
        private String menuName;
        private String path;
        private String component;
        private String icon;
        private String menuType;
        private Integer sortOrder;
        private List<MenuTreeNode> children;
    }
}
