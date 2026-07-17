package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysMenu;
import cn.aisino.misstudybackend.system.domain.SysRole;
import cn.aisino.misstudybackend.system.domain.SysRoleMenu;
import cn.aisino.misstudybackend.system.domain.SysUserRole;
import cn.aisino.misstudybackend.system.mapper.SysMenuMapper;
import cn.aisino.misstudybackend.system.mapper.SysRoleMapper;
import cn.aisino.misstudybackend.system.mapper.SysRoleMenuMapper;
import cn.aisino.misstudybackend.system.mapper.SysUserRoleMapper;
import cn.aisino.misstudybackend.system.service.ISysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl
        extends ServiceImpl<SysRoleMapper, SysRole>
        implements ISysRoleService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public TableDataInfo<SysRole> queryPageList(SysRole query, PageQuery pageQuery) {
        SysRole effectiveQuery = query == null ? new SysRole() : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        Page<SysRole> page = new Page<>(effectivePage.getPageNum(), effectivePage.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDelFlag, "0")
                .like(StringUtils.hasText(effectiveQuery.getRoleName()),
                        SysRole::getRoleName, effectiveQuery.getRoleName())
                .like(StringUtils.hasText(effectiveQuery.getRoleKey()),
                        SysRole::getRoleKey, effectiveQuery.getRoleKey())
                .eq(StringUtils.hasText(effectiveQuery.getStatus()),
                        SysRole::getStatus, effectiveQuery.getStatus())
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId);
        return TableDataInfo.build(page(page, wrapper));
    }

    @Override
    public List<SysRole> queryOptionList() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDelFlag, "0")
                .eq(SysRole::getStatus, "0")
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId);
        return list(wrapper);
    }

    @Override
    public SysRole queryDetail(Long roleId) {
        if (roleId == null) {
            return null;
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleId, roleId)
                .eq(SysRole::getDelFlag, "0");
        return getOne(wrapper);
    }

    @Override
    public boolean existsByRoleName(String roleName, Long excludeRoleId) {
        return existsByField(SysRole::getRoleName, roleName, excludeRoleId);
    }

    @Override
    public boolean existsByRoleKey(String roleKey, Long excludeRoleId) {
        return existsByField(SysRole::getRoleKey, roleKey, excludeRoleId);
    }

    @Override
    public boolean hasUserReference(Long roleId) {
        if (roleId == null) {
            return false;
        }
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        return userRoleMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRole(SysRole role) {
        validateRole(role, false);
        role.setRoleId(null);
        role.setDelFlag("0");
        role.setCreateBy("admin");
        role.setCreateTime(LocalDateTime.now());
        if (baseMapper.insert(role) <= 0) {
            return false;
        }
        insertRoleMenus(role.getRoleId(), role.getMenuIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        validateRole(role, true);
        role.setDelFlag(null);
        role.setCreateBy(null);
        role.setCreateTime(null);
        role.setUpdateBy("admin");
        role.setUpdateTime(LocalDateTime.now());
        if (baseMapper.updateById(role) <= 0) {
            return false;
        }
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, role.getRoleId());
        roleMenuMapper.delete(wrapper);
        insertRoleMenus(role.getRoleId(), role.getMenuIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        if (queryDetail(roleId) == null) {
            throw new IllegalArgumentException("角色不存在或已删除");
        }
        if (hasUserReference(roleId)) {
            throw new IllegalArgumentException("角色已被用户引用，请先取消用户角色关系");
        }
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setDelFlag("2");
        role.setUpdateBy("admin");
        role.setUpdateTime(LocalDateTime.now());
        if (baseMapper.updateById(role) <= 0) {
            return false;
        }
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(wrapper);
        return true;
    }

    private <T> boolean existsByField(
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<SysRole, T> column,
            T value,
            Long excludeRoleId) {
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return false;
        }
        if (value == null) {
            return false;
        }
        Object effectiveValue = value instanceof String text ? text.trim() : value;
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDelFlag, "0")
                .eq(column, effectiveValue)
                .ne(excludeRoleId != null, SysRole::getRoleId, excludeRoleId);
        return count(wrapper) > 0;
    }

    private void validateRole(SysRole role, boolean update) {
        if (role == null) {
            throw new IllegalArgumentException("角色数据不能为空");
        }
        if (update && role.getRoleId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (update && queryDetail(role.getRoleId()) == null) {
            throw new IllegalArgumentException("角色不存在或已删除");
        }
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (!StringUtils.hasText(role.getRoleKey())) {
            throw new IllegalArgumentException("角色权限字符不能为空");
        }
        if (role.getRoleSort() == null) {
            throw new IllegalArgumentException("角色排序不能为空");
        }
        role.setRoleName(role.getRoleName().trim());
        role.setRoleKey(role.getRoleKey().trim());
        role.setStatus(StringUtils.hasText(role.getStatus()) ? role.getStatus().trim() : "0");
        role.setRemark(StringUtils.hasText(role.getRemark()) ? role.getRemark().trim() : null);
        if (!"0".equals(role.getStatus()) && !"1".equals(role.getStatus())) {
            throw new IllegalArgumentException("角色状态只能为0或1");
        }
        Long excludeId = update ? role.getRoleId() : null;
        if (existsByRoleName(role.getRoleName(), excludeId)) {
            throw new IllegalArgumentException("角色名称已存在");
        }
        if (existsByRoleKey(role.getRoleKey(), excludeId)) {
            throw new IllegalArgumentException("角色权限字符已存在");
        }

        List<Long> menuIds = role.getMenuIds();
        if (menuIds == null || menuIds.isEmpty()) {
            role.setMenuIds(new ArrayList<>());
            return;
        }
        if (menuIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("菜单ID不能为空");
        }
        LinkedHashSet<Long> distinctMenuIds = new LinkedHashSet<>(menuIds);
        LambdaQueryWrapper<SysMenu> menuWrapper = new LambdaQueryWrapper<>();
        menuWrapper.in(SysMenu::getMenuId, distinctMenuIds)
                .eq(SysMenu::getDelFlag, "0");
        if (menuMapper.selectCount(menuWrapper) != distinctMenuIds.size()) {
            throw new IllegalArgumentException("所选菜单包含不存在或已删除的数据");
        }
        role.setMenuIds(new ArrayList<>(distinctMenuIds));
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null || menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            if (roleMenuMapper.insert(relation) <= 0) {
                throw new IllegalStateException("保存角色菜单关系失败");
            }
        }
    }
}
