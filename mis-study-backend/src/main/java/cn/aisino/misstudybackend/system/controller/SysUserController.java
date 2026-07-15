package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysUser;
import cn.aisino.misstudybackend.system.service.ISysUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class SysUserController {

    private final ISysUserService userService;

    public SysUserController(ISysUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser query, PageQuery pageQuery) {
        return userService.queryPageList(query, pageQuery);
    }

    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.queryDetail(userId);
        return user == null ? R.fail("用户不存在或已删除") : R.success(user);
    }

    @GetMapping("/nameExists")
    public R<Boolean> nameExists(
            @RequestParam String userName,
            @RequestParam(required = false) Long excludeUserId) {
        return R.success(userService.existsByUserName(userName, excludeUserId));
    }

    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        try {
            return userService.insertUser(user) ? R.success() : R.fail("新增用户失败");
        } catch (IllegalArgumentException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysUser user) {
        try {
            return userService.updateUser(user) ? R.success() : R.fail("修改用户失败");
        } catch (IllegalArgumentException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public R<Void> remove(@PathVariable Long userId) {
        return userService.deleteUser(userId)
                ? R.success()
                : R.fail("用户不存在、已删除或删除失败");
    }
}
