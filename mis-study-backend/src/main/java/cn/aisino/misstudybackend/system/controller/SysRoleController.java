package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysRole;
import cn.aisino.misstudybackend.system.service.ISysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class SysRoleController {

    private final ISysRoleService roleService;

    public SysRoleController(ISysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public TableDataInfo<SysRole> list(SysRole query, PageQuery pageQuery) {
        return roleService.queryPageList(query, pageQuery);
    }

    @GetMapping("/options")
    public R<List<SysRole>> options() {
        return R.success(roleService.queryOptionList());
    }

    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole role = roleService.queryDetail(roleId);
        return role == null ? R.fail("角色不存在或已删除") : R.success(role);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysRole role) {
        try {
            return roleService.insertRole(role) ? R.success() : R.fail("新增角色失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysRole role) {
        try {
            return roleService.updateRole(role) ? R.success() : R.fail("修改角色失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @DeleteMapping("/{roleId}")
    public R<Void> remove(@PathVariable Long roleId) {
        try {
            return roleService.deleteRole(roleId) ? R.success() : R.fail("删除角色失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }
}
