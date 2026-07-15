package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysPost;
import cn.aisino.misstudybackend.system.service.ISysPostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/post")
public class SysPostController {

    private final ISysPostService postService;

    public SysPostController(ISysPostService postService) {
        this.postService = postService;
    }

    @GetMapping("/list")
    public TableDataInfo<SysPost> list(SysPost query, PageQuery pageQuery) {
        return postService.queryPageList(query, pageQuery);
    }

    @GetMapping("/options")
    public R<List<SysPost>> options() {
        return R.success(postService.queryOptionList());
    }

    @GetMapping("/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        SysPost post = getExistingPost(postId);
        return post == null ? R.fail("岗位不存在或已删除") : R.success(post);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysPost post) {
        String error = validate(post, false);
        if (error != null) {
            return R.fail(error);
        }
        post.setPostId(null);
        post.setDelFlag("0");
        post.setCreateBy("admin");
        post.setCreateTime(LocalDateTime.now());
        return postService.save(post) ? R.success() : R.fail("新增岗位失败");
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysPost post) {
        String error = validate(post, true);
        if (error != null) {
            return R.fail(error);
        }
        if ("1".equals(post.getStatus()) && postService.hasUser(post.getPostId())) {
            return R.fail("岗位已被用户引用，不能停用");
        }
        post.setDelFlag(null);
        post.setCreateBy(null);
        post.setCreateTime(null);
        post.setUpdateBy("admin");
        post.setUpdateTime(LocalDateTime.now());
        return postService.updateById(post) ? R.success() : R.fail("修改岗位失败");
    }

    @DeleteMapping("/{postId}")
    public R<Void> remove(@PathVariable Long postId) {
        if (getExistingPost(postId) == null) {
            return R.fail("岗位不存在或已删除");
        }
        if (postService.hasUser(postId)) {
            return R.fail("岗位已被用户引用，不能删除");
        }

        SysPost post = new SysPost();
        post.setPostId(postId);
        post.setDelFlag("2");
        post.setUpdateBy("admin");
        post.setUpdateTime(LocalDateTime.now());
        return postService.updateById(post) ? R.success() : R.fail("删除岗位失败");
    }

    private String validate(SysPost post, boolean update) {
        if (post == null) {
            return "岗位数据不能为空";
        }
        if (update && post.getPostId() == null) {
            return "岗位ID不能为空";
        }
        if (update && getExistingPost(post.getPostId()) == null) {
            return "岗位不存在或已删除";
        }
        if (!StringUtils.hasText(post.getPostCode())) {
            return "岗位编码不能为空";
        }
        if (!StringUtils.hasText(post.getPostName())) {
            return "岗位名称不能为空";
        }
        if (post.getPostSort() == null) {
            return "岗位排序不能为空";
        }

        post.setPostCode(post.getPostCode().trim());
        post.setPostName(post.getPostName().trim());
        post.setStatus(StringUtils.hasText(post.getStatus()) ? post.getStatus() : "0");
        if (!"0".equals(post.getStatus()) && !"1".equals(post.getStatus())) {
            return "岗位状态只能为0或1";
        }
        Long excludeId = update ? post.getPostId() : null;
        if (postService.existsByPostCode(post.getPostCode(), excludeId)) {
            return "岗位编码已存在";
        }
        if (postService.existsByPostName(post.getPostName(), excludeId)) {
            return "岗位名称已存在";
        }
        return null;
    }

    private SysPost getExistingPost(Long postId) {
        if (postId == null) {
            return null;
        }
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getPostId, postId)
                .eq(SysPost::getDelFlag, "0");
        return postService.getOne(wrapper);
    }
}
