package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysDept;
import cn.aisino.misstudybackend.system.domain.SysPost;
import cn.aisino.misstudybackend.system.domain.SysUser;
import cn.aisino.misstudybackend.system.domain.SysUserPost;
import cn.aisino.misstudybackend.system.mapper.SysDeptMapper;
import cn.aisino.misstudybackend.system.mapper.SysPostMapper;
import cn.aisino.misstudybackend.system.mapper.SysUserMapper;
import cn.aisino.misstudybackend.system.mapper.SysUserPostMapper;
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
    private final SysUserPostMapper userPostMapper;
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

        LambdaQueryWrapper<SysUserPost> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SysUserPost::getUserId, user.getUserId());
        userPostMapper.delete(relationWrapper);
        insertUserPosts(user.getUserId(), user.getPostIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (queryDetail(userId) == null) {
            return false;
        }

        LambdaQueryWrapper<SysUserPost> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SysUserPost::getUserId, userId);
        userPostMapper.delete(relationWrapper);

        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDelFlag("2");
        user.setUpdateBy("admin");
        user.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(user) > 0;
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

        List<Long> postIds = user.getPostIds();
        if (postIds == null || postIds.isEmpty()) {
            user.setPostIds(new ArrayList<>());
            return;
        }
        if (postIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("岗位ID不能为空");
        }

        LinkedHashSet<Long> distinctPostIds = new LinkedHashSet<>(postIds);
        LambdaQueryWrapper<SysPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(SysPost::getPostId, distinctPostIds)
                .eq(SysPost::getDelFlag, "0")
                .eq(SysPost::getStatus, "0");
        if (postMapper.selectCount(postWrapper) != distinctPostIds.size()) {
            throw new IllegalArgumentException("所选岗位包含不存在、已删除或已停用的数据");
        }
        user.setPostIds(new ArrayList<>(distinctPostIds));
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
            LambdaQueryWrapper<SysDept> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.in(SysDept::getDeptId, deptIds)
                    .eq(SysDept::getDelFlag, "0");
            deptNames = deptMapper.selectList(deptWrapper).stream()
                    .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));
        }

        List<Long> userIds = users.stream()
                .map(SysUser::getUserId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<Long>> userPostIds = new HashMap<>();
        Set<Long> allPostIds = new LinkedHashSet<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<SysUserPost> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.in(SysUserPost::getUserId, userIds)
                    .orderByAsc(SysUserPost::getUserId)
                    .orderByAsc(SysUserPost::getPostId);
            for (SysUserPost relation : userPostMapper.selectList(relationWrapper)) {
                userPostIds.computeIfAbsent(relation.getUserId(), key -> new ArrayList<>())
                        .add(relation.getPostId());
                allPostIds.add(relation.getPostId());
            }
        }

        Map<Long, String> postNames = new HashMap<>();
        if (!allPostIds.isEmpty()) {
            LambdaQueryWrapper<SysPost> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.in(SysPost::getPostId, allPostIds)
                    .eq(SysPost::getDelFlag, "0");
            postNames = postMapper.selectList(postWrapper).stream()
                    .collect(Collectors.toMap(SysPost::getPostId, SysPost::getPostName));
        }

        for (SysUser user : users) {
            user.setDeptName(deptNames.get(user.getDeptId()));
            List<Long> ids = new ArrayList<>(
                    userPostIds.getOrDefault(user.getUserId(), Collections.emptyList()));
            user.setPostIds(ids);
            List<String> names = ids.stream()
                    .map(postNames::get)
                    .filter(Objects::nonNull)
                    .toList();
            user.setPostNames(new ArrayList<>(names));
        }
    }
}
