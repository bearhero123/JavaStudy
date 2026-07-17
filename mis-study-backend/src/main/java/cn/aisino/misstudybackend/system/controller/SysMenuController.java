package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.system.domain.SysMenu;
import cn.aisino.misstudybackend.system.domain.vo.RoleMenuTreeVo;
import cn.aisino.misstudybackend.system.service.ISysMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class SysMenuController {

    private final ISysMenuService menuService;

    public SysMenuController(ISysMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu query) {
        return R.success(menuService.queryTreeList(query));
    }

    @GetMapping("/treeOptions")
    public R<List<SysMenu>> treeOptions(
            @RequestParam(required = false) Long excludeMenuId) {
        return R.success(menuService.queryTreeOptions(excludeMenuId));
    }

    @GetMapping("/roleTree/{roleId}")
    public R<RoleMenuTreeVo> roleTree(@PathVariable Long roleId) {
        try {
            return R.success(menuService.queryRoleMenuTree(roleId));
        } catch (IllegalArgumentException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        SysMenu menu = menuService.queryDetail(menuId);
        return menu == null ? R.fail("菜单不存在或已删除") : R.success(menu);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysMenu menu) {
        try {
            return menuService.insertMenu(menu) ? R.success() : R.fail("新增菜单失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysMenu menu) {
        try {
            return menuService.updateMenu(menu) ? R.success() : R.fail("修改菜单失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        try {
            return menuService.deleteMenu(menuId) ? R.success() : R.fail("删除菜单失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }
}
