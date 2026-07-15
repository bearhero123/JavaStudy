package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysPost;
import cn.aisino.misstudybackend.system.domain.SysUserPost;
import cn.aisino.misstudybackend.system.mapper.SysPostMapper;
import cn.aisino.misstudybackend.system.mapper.SysUserPostMapper;
import cn.aisino.misstudybackend.system.service.ISysPostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysPostServiceImpl
        extends ServiceImpl<SysPostMapper, SysPost>
        implements ISysPostService {

    private final SysUserPostMapper userPostMapper;

    @Override
    public TableDataInfo<SysPost> queryPageList(SysPost query, PageQuery pageQuery) {
        SysPost effectiveQuery = query == null ? new SysPost() : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        Page<SysPost> page = new Page<>(effectivePage.getPageNum(), effectivePage.getPageSize());

        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getDelFlag, "0")
                .like(StringUtils.hasText(effectiveQuery.getPostCode()),
                        SysPost::getPostCode, effectiveQuery.getPostCode())
                .like(StringUtils.hasText(effectiveQuery.getPostName()),
                        SysPost::getPostName, effectiveQuery.getPostName())
                .eq(StringUtils.hasText(effectiveQuery.getStatus()),
                        SysPost::getStatus, effectiveQuery.getStatus())
                .orderByAsc(SysPost::getPostSort)
                .orderByAsc(SysPost::getPostId);

        return TableDataInfo.build(page(page, wrapper));
    }

    @Override
    public List<SysPost> queryOptionList() {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getDelFlag, "0")
                .eq(SysPost::getStatus, "0")
                .orderByAsc(SysPost::getPostSort)
                .orderByAsc(SysPost::getPostId);
        return list(wrapper);
    }

    @Override
    public boolean existsByPostCode(String postCode, Long excludePostId) {
        if (!StringUtils.hasText(postCode)) {
            return false;
        }
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getDelFlag, "0")
                .eq(SysPost::getPostCode, postCode.trim())
                .ne(excludePostId != null, SysPost::getPostId, excludePostId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean existsByPostName(String postName, Long excludePostId) {
        if (!StringUtils.hasText(postName)) {
            return false;
        }
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getDelFlag, "0")
                .eq(SysPost::getPostName, postName.trim())
                .ne(excludePostId != null, SysPost::getPostId, excludePostId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasUser(Long postId) {
        if (postId == null) {
            return false;
        }
        LambdaQueryWrapper<SysUserPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPost::getPostId, postId);
        return userPostMapper.selectCount(wrapper) > 0;
    }
}
