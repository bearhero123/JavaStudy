package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.*;
import cn.aisino.misstudybackend.system.mapper.*;
import cn.aisino.misstudybackend.system.service.ISysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements ISysUserService {

    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public TableDataInfo<SysUser> queryPageList(SysUser query, PageQuery pageQuery) {
        SysUser effectiveQuery = query == null ? new SysUser() : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        Page<SysUser> page = new Page<>(effectivePage.getPageNum(), effectivePage.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDelFlag, "0")
                .like(StringUtils.hasText(effectiveQuery.getUserName()),
                        SysUser::getUserName, effectiveQuery.getUserName())
                .like(StringUtils.hasText(effectiveQuery.getNickName()),
                        SysUser::getNickName, effectiveQuery.getNickName())
                .like(StringUtils.hasText(effectiveQuery.getPhonenumber()),
                        SysUser::getPhonenumber, effectiveQuery.getPhonenumber())
                .eq(StringUtils.hasText(effectiveQuery.getStatus()),
                        SysUser::getStatus, effectiveQuery.getStatus())
                .eq(effectiveQuery.getDeptId() != null,
                        SysUser::getDeptId, effectiveQuery.getDeptId())
                .orderByDesc(SysUser::getCreateTime)
                .orderByDesc(SysUser::getUserId);
        Page<SysUser> result = page(page, wrapper);
        fillRelations(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public SysUser queryDetail(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserId, userId)
                .eq(SysUser::getDelFlag, "0");
        SysUser user = getOne(wrapper);
        if (user != null) {
            fillRelations(Collections.singletonList(user));
        }
        return user;
    }

    @Override
    public boolean existsByUserName(String userName, Long excludeUserId) {
        return existsByField(SysUser::getUserName, userName, excludeUserId);
    }

    @Override
    public boolean existsByPhone(String phonenumber, Long excludeUserId) {
        return existsByField(SysUser::getPhonenumber, phonenumber, excludeUserId);
    }

    @Override
    public boolean existsByEmail(String email, Long excludeUserId) {
        return existsByField(SysUser::getEmail, email, excludeUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        validateUser(user, false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDelFlag("0");
        user.setCreateBy("admin");
        user.setCreateTime(LocalDateTime.now());
        if (baseMapper.insert(user) <= 0) {
            return false;
        }
        insertUserPosts(user.getUserId(), user.getPostIds());
        insertUserRoles(user.getUserId(), user.getRoleIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        validateUser(user, true);
        user.setPassword(null);
        user.setDelFlag(null);
        user.setCreateBy(null);
        user.setCreateTime(null);
        user.setUpdateBy("admin");
        user.setUpdateTime(LocalDateTime.now());
        if (baseMapper.updateById(user) <= 0) {
            return false;
        }
        LambdaQueryWrapper<SysUserPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(SysUserPost::getUserId, user.getUserId());
        userPostMapper.delete(postWrapper);
        insertUserPosts(user.getUserId(), user.getPostIds());

        LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysUserRole::getUserId, user.getUserId());
        userRoleMapper.delete(roleWrapper);
        insertUserRoles(user.getUserId(), user.getRoleIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (queryDetail(userId) == null) {
            return false;
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDelFlag("2");
        user.setUpdateBy("admin");
        user.setUpdateTime(LocalDateTime.now());
        if (baseMapper.updateById(user) <= 0) {
            return false;
        }

        LambdaQueryWrapper<SysUserPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(SysUserPost::getUserId, userId);
        userPostMapper.delete(postWrapper);
        LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(roleWrapper);
        return true;
    }

    private <T> boolean existsByField(
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<SysUser, T> column,
            T value,
            Long excludeUserId) {
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return false;
        }
        if (value == null) {
            return false;
        }
        Object effectiveValue = value instanceof String text ? text.trim() : value;
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDelFlag, "0")
                .eq(column, effectiveValue)
                .ne(excludeUserId != null, SysUser::getUserId, excludeUserId);
        return count(wrapper) > 0;
    }

    private void validateUser(SysUser user, boolean update) {
        if (user == null) {
            throw new IllegalArgumentException("用户数据不能为空");
        }
        if (update && user.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (update && queryDetail(user.getUserId()) == null) {
            throw new IllegalArgumentException("用户不存在或已删除");
        }
        if (!StringUtils.hasText(user.getUserName())) {
            throw new IllegalArgumentException("用户账号不能为空");
        }
        if (!StringUtils.hasText(user.getNickName())) {
            throw new IllegalArgumentException("用户昵称不能为空");
        }
        if (!update && !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("新增用户密码不能为空");
        }
        if (user.getDeptId() == null) {
            throw new IllegalArgumentException("所属部门不能为空");
        }

        user.setUserName(user.getUserName().trim());
        user.setNickName(user.getNickName().trim());
        if (StringUtils.hasText(user.getPhonenumber())) {
            user.setPhonenumber(user.getPhonenumber().trim());
        }
        if (StringUtils.hasText(user.getEmail())) {
            user.setEmail(user.getEmail().trim());
        }
        user.setStatus(StringUtils.hasText(user.getStatus()) ? user.getStatus() : "0");
        user.setSex(StringUtils.hasText(user.getSex()) ? user.getSex() : "0");
        if (!"0".equals(user.getStatus()) && !"1".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户状态只能为0或1");
        }

        Long excludeId = update ? user.getUserId() : null;
        if (existsByUserName(user.getUserName(), excludeId)) {
            throw new IllegalArgumentException("用户账号已存在");
        }
        if (existsByPhone(user.getPhonenumber(), excludeId)) {
            throw new IllegalArgumentException("手机号码已存在");
        }
        if (existsByEmail(user.getEmail(), excludeId)) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        LambdaQueryWrapper<SysDept> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(SysDept::getDeptId, user.getDeptId())
                .eq(SysDept::getDelFlag, "0")
                .eq(SysDept::getStatus, "0");
        if (deptMapper.selectCount(deptWrapper) == 0) {
            throw new IllegalArgumentException("所属部门不存在、已删除或已停用");
        }

        user.setPostIds(validateIds(
                user.getPostIds(),
                "岗位ID不能为空",
                ids -> {
                    LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SysPost::getPostId, ids)
                            .eq(SysPost::getDelFlag, "0")
                            .eq(SysPost::getStatus, "0");
                    return postMapper.selectCount(wrapper);
                },
                "所选岗位包含不存在、已删除或已停用的数据"));

        user.setRoleIds(validateIds(
                user.getRoleIds(),
                "角色ID不能为空",
                ids -> {
                    LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(SysRole::getRoleId, ids)
                            .eq(SysRole::getDelFlag, "0")
                            .eq(SysRole::getStatus, "0");
                    return roleMapper.selectCount(wrapper);
                },
                "所选角色包含不存在、已删除或已停用的数据"));
    }

    private List<Long> validateIds(
            List<Long> ids,
            String nullMessage,
            java.util.function.Function<Set<Long>, Long> countFunction,
            String invalidMessage) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(nullMessage);
        }
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(ids);
        if (countFunction.apply(distinctIds) != distinctIds.size()) {
            throw new IllegalArgumentException(invalidMessage);
        }
        return new ArrayList<>(distinctIds);
    }

    private void insertUserPosts(Long userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return;
        }
        for (Long postId : postIds) {
            SysUserPost relation = new SysUserPost();
            relation.setUserId(userId);
            relation.setPostId(postId);
            if (userPostMapper.insert(relation) <= 0) {
                throw new IllegalStateException("保存用户岗位关系失败");
            }
        }
    }

    private void insertUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            if (userRoleMapper.insert(relation) <= 0) {
                throw new IllegalStateException("保存用户角色关系失败");
            }
        }
    }

    private void fillRelations(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        Set<Long> deptIds = users.stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> deptNames = new HashMap<>();
        if (!deptIds.isEmpty()) {
            LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysDept::getDeptId, deptIds).eq(SysDept::getDelFlag, "0");
            deptNames = deptMapper.selectList(wrapper).stream()
                    .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));
        }

        List<Long> userIds = users.stream()
                .map(SysUser::getUserId)
                .filter(Objects::nonNull)
                .toList();
        RelationNames postRelations = loadPostRelations(userIds);
        RelationNames roleRelations = loadRoleRelations(userIds);

        for (SysUser user : users) {
            user.setDeptName(deptNames.get(user.getDeptId()));
            List<Long> postIds = new ArrayList<>(postRelations.idsByUser()
                    .getOrDefault(user.getUserId(), Collections.emptyList()));
            user.setPostIds(postIds);
            user.setPostNames(namesForIds(postIds, postRelations.namesById()));

            List<Long> roleIds = new ArrayList<>(roleRelations.idsByUser()
                    .getOrDefault(user.getUserId(), Collections.emptyList()));
            user.setRoleIds(roleIds);
            user.setRoleNames(namesForIds(roleIds, roleRelations.namesById()));
        }
    }

    private RelationNames loadPostRelations(List<Long> userIds) {
        Map<Long, List<Long>> idsByUser = new HashMap<>();
        Set<Long> allIds = new LinkedHashSet<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<SysUserPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysUserPost::getUserId, userIds)
                    .orderByAsc(SysUserPost::getUserId)
                    .orderByAsc(SysUserPost::getPostId);
            for (SysUserPost relation : userPostMapper.selectList(wrapper)) {
                idsByUser.computeIfAbsent(relation.getUserId(), key -> new ArrayList<>())
                        .add(relation.getPostId());
                allIds.add(relation.getPostId());
            }
        }
        Map<Long, String> names = new HashMap<>();
        if (!allIds.isEmpty()) {
            LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysPost::getPostId, allIds).eq(SysPost::getDelFlag, "0");
            names = postMapper.selectList(wrapper).stream()
                    .collect(Collectors.toMap(SysPost::getPostId, SysPost::getPostName));
        }
        return new RelationNames(idsByUser, names);
    }

    private RelationNames loadRoleRelations(List<Long> userIds) {
        Map<Long, List<Long>> idsByUser = new HashMap<>();
        Set<Long> allIds = new LinkedHashSet<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysUserRole::getUserId, userIds)
                    .orderByAsc(SysUserRole::getUserId)
                    .orderByAsc(SysUserRole::getRoleId);
            for (SysUserRole relation : userRoleMapper.selectList(wrapper)) {
                idsByUser.computeIfAbsent(relation.getUserId(), key -> new ArrayList<>())
                        .add(relation.getRoleId());
                allIds.add(relation.getRoleId());
            }
        }
        Map<Long, String> names = new HashMap<>();
        if (!allIds.isEmpty()) {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysRole::getRoleId, allIds).eq(SysRole::getDelFlag, "0");
            names = roleMapper.selectList(wrapper).stream()
                    .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));
        }
        return new RelationNames(idsByUser, names);
    }

    private List<String> namesForIds(List<Long> ids, Map<Long, String> namesById) {
        return new ArrayList<>(ids.stream()
                .map(namesById::get)
                .filter(Objects::nonNull)
                .toList());
    }

    private record RelationNames(
            Map<Long, List<Long>> idsByUser,
            Map<Long, String> namesById) {
    }
}
