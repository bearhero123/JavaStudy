package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.system.domain.SysMenu;
import cn.aisino.misstudybackend.system.domain.SysRole;
import cn.aisino.misstudybackend.system.domain.SysRoleMenu;
import cn.aisino.misstudybackend.system.domain.vo.RoleMenuTreeVo;
import cn.aisino.misstudybackend.system.mapper.SysMenuMapper;
import cn.aisino.misstudybackend.system.mapper.SysRoleMapper;
import cn.aisino.misstudybackend.system.mapper.SysRoleMenuMapper;
import cn.aisino.misstudybackend.system.service.ISysMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl
        extends ServiceImpl<SysMenuMapper, SysMenu>
        implements ISysMenuService {

    private static final Comparator<SysMenu> MENU_COMPARATOR =
            Comparator.comparing(
                            SysMenu::getOrderNum,
                            Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(
                            SysMenu::getMenuId,
                            Comparator.nullsLast(Long::compareTo));

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysMenu> queryTreeList(SysMenu query) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, "0")
                .orderByAsc(SysMenu::getOrderNum)
                .orderByAsc(SysMenu::getMenuId);

        List<SysMenu> allMenus = baseMapper.selectList(wrapper);
        if (allMenus.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SysMenu> idMap = new HashMap<>();
        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();
        for (SysMenu menu : allMenus) {
            if (menu.getMenuId() != null) {
                idMap.putIfAbsent(menu.getMenuId(), menu);
            }
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(menu);
            menu.setChildren(new ArrayList<>());
        }

        Set<Long> visited = new HashSet<>();
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();
            boolean root = Objects.equals(parentId, 0L) || !idMap.containsKey(parentId);
            if (root && !visited.contains(menu.getMenuId())) {
                buildTree(menu, childrenMap, visited);
                roots.add(menu);
            }
        }
        for (SysMenu menu : allMenus) {
            if (!visited.contains(menu.getMenuId())) {
                buildTree(menu, childrenMap, visited);
                roots.add(menu);
            }
        }
        roots.sort(MENU_COMPARATOR);

        if (query == null) {
            return roots;
        }
        String menuName = StringUtils.hasText(query.getMenuName())
                ? query.getMenuName().trim() : null;
        String status = StringUtils.hasText(query.getStatus())
                ? query.getStatus().trim() : null;
        if (!StringUtils.hasText(menuName) && !StringUtils.hasText(status)) {
            return roots;
        }
        return filterTree(roots, menuName, status);
    }

    @Override
    public List<SysMenu> queryTreeOptions(Long excludeMenuId) {
        return filterTreeOptions(queryTreeList(null), excludeMenuId);
    }

    @Override
    public SysMenu queryDetail(Long menuId) {
        if (menuId == null) {
            return null;
        }
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuId, menuId)
                .eq(SysMenu::getDelFlag, "0");
        return getOne(wrapper);
    }

    @Override
    public RoleMenuTreeVo queryRoleMenuTree(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleId, roleId)
                .eq(SysRole::getDelFlag, "0");
        if (roleMapper.selectCount(roleWrapper) == 0) {
            throw new IllegalArgumentException("角色不存在或已删除");
        }

        RoleMenuTreeVo result = new RoleMenuTreeVo();
        result.setMenus(queryTreeList(null));

        LambdaQueryWrapper<SysRoleMenu> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SysRoleMenu::getRoleId, roleId)
                .orderByAsc(SysRoleMenu::getMenuId);
        List<Long> checkedKeys = roleMenuMapper.selectList(relationWrapper).stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .toList();
        result.setCheckedKeys(new ArrayList<>(checkedKeys));
        return result;
    }

    @Override
    public boolean existsByMenuName(Long parentId, String menuName, Long excludeMenuId) {
        if (!StringUtils.hasText(menuName)) {
            return false;
        }
        Long effectiveParentId = parentId == null ? 0L : parentId;
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDelFlag, "0")
                .eq(SysMenu::getParentId, effectiveParentId)
                .eq(SysMenu::getMenuName, menuName.trim())
                .ne(excludeMenuId != null, SysMenu::getMenuId, excludeMenuId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasChild(Long menuId) {
        if (menuId == null) {
            return false;
        }
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, menuId)
                .eq(SysMenu::getDelFlag, "0");
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasRoleReference(Long menuId) {
        if (menuId == null) {
            return false;
        }
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getMenuId, menuId);
        return roleMenuMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isDescendant(Long menuId, Long targetParentId) {
        if (menuId == null || targetParentId == null) {
            return false;
        }
        Set<Long> visited = new HashSet<>();
        Long currentId = targetParentId;
        while (currentId != null && currentId != 0L) {
            if (Objects.equals(currentId, menuId)) {
                return true;
            }
            if (!visited.add(currentId)) {
                return false;
            }
            SysMenu currentMenu = queryDetail(currentId);
            if (currentMenu == null) {
                return false;
            }
            currentId = currentMenu.getParentId();
        }
        return false;
    }

    @Override
    public boolean insertMenu(SysMenu menu) {
        validateMenu(menu, false);
        menu.setMenuId(null);
        menu.setDelFlag("0");
        menu.setCreateBy("admin");
        menu.setCreateTime(LocalDateTime.now());
        return baseMapper.insert(menu) > 0;
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        validateMenu(menu, true);
        menu.setDelFlag(null);
        menu.setCreateBy(null);
        menu.setCreateTime(null);
        menu.setUpdateBy("admin");
        menu.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(menu) > 0;
    }

    @Override
    public boolean deleteMenu(Long menuId) {
        if (queryDetail(menuId) == null) {
            throw new IllegalArgumentException("菜单不存在或已删除");
        }
        if (hasChild(menuId)) {
            throw new IllegalArgumentException("存在子菜单，不能删除当前菜单");
        }
        if (hasRoleReference(menuId)) {
            throw new IllegalArgumentException("菜单已被角色引用，请先取消授权");
        }
        SysMenu menu = new SysMenu();
        menu.setMenuId(menuId);
        menu.setDelFlag("2");
        menu.setUpdateBy("admin");
        menu.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(menu) > 0;
    }

    private void validateMenu(SysMenu menu, boolean update) {
        if (menu == null) {
            throw new IllegalArgumentException("菜单数据不能为空");
        }
        SysMenu existing = null;
        if (update) {
            if (menu.getMenuId() == null) {
                throw new IllegalArgumentException("菜单ID不能为空");
            }
            existing = queryDetail(menu.getMenuId());
            if (existing == null) {
                throw new IllegalArgumentException("菜单不存在或已删除");
            }
        }
        if (!StringUtils.hasText(menu.getMenuName())) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }

        menu.setMenuName(menu.getMenuName().trim());
        menu.setParentId(menu.getParentId() == null ? 0L : menu.getParentId());
        menu.setOrderNum(menu.getOrderNum() == null
                ? existing == null ? 0 : existing.getOrderNum()
                : menu.getOrderNum());
        menu.setMenuType(StringUtils.hasText(menu.getMenuType())
                ? menu.getMenuType().trim().toUpperCase()
                : existing == null ? "C" : existing.getMenuType());
        menu.setVisible(StringUtils.hasText(menu.getVisible())
                ? menu.getVisible().trim()
                : existing == null ? "0" : existing.getVisible());
        menu.setStatus(StringUtils.hasText(menu.getStatus())
                ? menu.getStatus().trim()
                : existing == null ? "0" : existing.getStatus());
        menu.setPath(StringUtils.hasText(menu.getPath()) ? menu.getPath().trim() : "");
        menu.setComponent(trimToNull(menu.getComponent()));
        menu.setPerms(trimToNull(menu.getPerms()));
        menu.setIcon(StringUtils.hasText(menu.getIcon()) ? menu.getIcon().trim() : "#");
        menu.setRemark(trimToNull(menu.getRemark()));

        if (!Set.of("M", "C", "F").contains(menu.getMenuType())) {
            throw new IllegalArgumentException("菜单类型只能为M、C或F");
        }
        if (!Set.of("0", "1").contains(menu.getVisible())) {
            throw new IllegalArgumentException("显示状态只能为0或1");
        }
        if (!Set.of("0", "1").contains(menu.getStatus())) {
            throw new IllegalArgumentException("菜单状态只能为0或1");
        }
        if (menu.getParentId() != 0L) {
            SysMenu parent = queryDetail(menu.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("上级菜单不存在或已删除");
            }
            if (!"0".equals(parent.getStatus())) {
                throw new IllegalArgumentException("上级菜单已停用");
            }
        }
        if (update && isDescendant(menu.getMenuId(), menu.getParentId())) {
            throw new IllegalArgumentException("上级菜单不能是当前菜单自身或其后代");
        }
        if (existsByMenuName(menu.getParentId(), menu.getMenuName(),
                update ? menu.getMenuId() : null)) {
            throw new IllegalArgumentException("同一上级菜单下已存在同名菜单");
        }
        if ("C".equals(menu.getMenuType())) {
            if (!StringUtils.hasText(menu.getPath())) {
                throw new IllegalArgumentException("菜单类型为C时路由地址不能为空");
            }
            if (!StringUtils.hasText(menu.getComponent())) {
                throw new IllegalArgumentException("菜单类型为C时组件路径不能为空");
            }
        }
        if ("F".equals(menu.getMenuType()) && !StringUtils.hasText(menu.getPerms())) {
            throw new IllegalArgumentException("按钮类型菜单的权限标识不能为空");
        }
    }

    private void buildTree(SysMenu current,
                           Map<Long, List<SysMenu>> childrenMap,
                           Set<Long> visited) {
        Long menuId = current.getMenuId();
        if (menuId == null || !visited.add(menuId)) {
            return;
        }
        List<SysMenu> rawChildren = childrenMap.getOrDefault(
                menuId, Collections.emptyList());
        List<SysMenu> safeChildren = new ArrayList<>();
        for (SysMenu child : rawChildren) {
            Long childId = child.getMenuId();
            if (childId == null || visited.contains(childId)) {
                continue;
            }
            safeChildren.add(child);
            buildTree(child, childrenMap, visited);
        }
        safeChildren.sort(MENU_COMPARATOR);
        current.setChildren(safeChildren);
    }

    private List<SysMenu> filterTree(List<SysMenu> nodes,
                                     String menuName,
                                     String status) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> filteredNodes = new ArrayList<>();
        for (SysMenu node : nodes) {
            List<SysMenu> filteredChildren = filterTree(
                    node.getChildren(), menuName, status);
            boolean nameMatches = !StringUtils.hasText(menuName)
                    || (StringUtils.hasText(node.getMenuName())
                    && node.getMenuName().contains(menuName));
            boolean statusMatches = !StringUtils.hasText(status)
                    || Objects.equals(node.getStatus(), status);
            if ((nameMatches && statusMatches) || !filteredChildren.isEmpty()) {
                node.setChildren(filteredChildren);
                filteredNodes.add(node);
            }
        }
        return filteredNodes;
    }

    private List<SysMenu> filterTreeOptions(List<SysMenu> nodes, Long excludeMenuId) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> options = new ArrayList<>();
        for (SysMenu node : nodes) {
            if (node == null
                    || Objects.equals(node.getMenuId(), excludeMenuId)
                    || !"0".equals(node.getStatus())) {
                continue;
            }
            node.setChildren(filterTreeOptions(node.getChildren(), excludeMenuId));
            options.add(node);
        }
        return options;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
