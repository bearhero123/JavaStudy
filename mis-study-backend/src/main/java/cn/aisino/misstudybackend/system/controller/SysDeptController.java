package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.system.domain.SysDept;
import cn.aisino.misstudybackend.system.service.ISysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/dept")
public class SysDeptController {

    private final ISysDeptService deptService;

    public SysDeptController(ISysDeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept query) {
        return R.success(deptService.queryTreeList(query));
    }

    @GetMapping("/treeOptions")
    public R<List<SysDept>> treeOptions(
            @RequestParam(required = false) Long excludeDeptId) {
        return R.success(deptService.queryTreeOptions(excludeDeptId));
    }

    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        SysDept dept = getExistingDept(deptId);
        return dept == null ? R.fail("部门不存在或已删除") : R.success(dept);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysDept dept) {
        if (dept == null || !StringUtils.hasText(dept.getDeptName())) {
            return R.fail("部门名称不能为空");
        }

        dept.setDeptName(dept.getDeptName().trim());
        dept.setParentId(dept.getParentId() == null ? 0L : dept.getParentId());
        dept.setOrderNum(dept.getOrderNum() == null ? 0 : dept.getOrderNum());
        dept.setStatus(StringUtils.hasText(dept.getStatus()) ? dept.getStatus() : "0");
        if (!isValidStatus(dept.getStatus())) {
            return R.fail("部门状态只能为0或1");
        }

        String parentError = validateParent(dept.getParentId());
        if (parentError != null) {
            return R.fail(parentError);
        }
        if (deptService.existsByDeptName(dept.getDeptName(), dept.getParentId(), null)) {
            return R.fail("同一上级部门下已存在同名部门");
        }

        dept.setDeptId(null);
        dept.setDelFlag("0");
        dept.setCreateBy("admin");
        dept.setCreateTime(LocalDateTime.now());
        return deptService.save(dept) ? R.success() : R.fail("新增部门失败");
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysDept dept) {
        if (dept == null || dept.getDeptId() == null) {
            return R.fail("部门ID不能为空");
        }
        SysDept existing = getExistingDept(dept.getDeptId());
        if (existing == null) {
            return R.fail("部门不存在或已删除");
        }
        if (!StringUtils.hasText(dept.getDeptName())) {
            return R.fail("部门名称不能为空");
        }

        dept.setDeptName(dept.getDeptName().trim());
        dept.setParentId(dept.getParentId() == null ? 0L : dept.getParentId());
        dept.setOrderNum(dept.getOrderNum() == null ? existing.getOrderNum() : dept.getOrderNum());
        dept.setStatus(StringUtils.hasText(dept.getStatus()) ? dept.getStatus() : existing.getStatus());
        if (!isValidStatus(dept.getStatus())) {
            return R.fail("部门状态只能为0或1");
        }
        if (deptService.isDescendant(dept.getDeptId(), dept.getParentId())) {
            return R.fail("上级部门不能是当前部门自身或其后代");
        }

        String parentError = validateParent(dept.getParentId());
        if (parentError != null) {
            return R.fail(parentError);
        }
        if (deptService.existsByDeptName(
                dept.getDeptName(), dept.getParentId(), dept.getDeptId())) {
            return R.fail("同一上级部门下已存在同名部门");
        }
        if ("1".equals(dept.getStatus())
                && (deptService.hasChild(dept.getDeptId()) || deptService.hasUser(dept.getDeptId()))) {
            return R.fail("存在子部门或用户引用，不能停用该部门");
        }

        dept.setDelFlag(null);
        dept.setCreateBy(null);
        dept.setCreateTime(null);
        dept.setUpdateBy("admin");
        dept.setUpdateTime(LocalDateTime.now());
        return deptService.updateById(dept) ? R.success() : R.fail("修改部门失败");
    }

    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        if (getExistingDept(deptId) == null) {
            return R.fail("部门不存在或已删除");
        }
        if (deptService.hasChild(deptId)) {
            return R.fail("存在子部门，不能删除该部门");
        }
        if (deptService.hasUser(deptId)) {
            return R.fail("存在用户引用，不能删除该部门");
        }

        SysDept dept = new SysDept();
        dept.setDeptId(deptId);
        dept.setDelFlag("2");
        dept.setUpdateBy("admin");
        dept.setUpdateTime(LocalDateTime.now());
        return deptService.updateById(dept) ? R.success() : R.fail("删除部门失败");
    }

    private SysDept getExistingDept(Long deptId) {
        if (deptId == null) {
            return null;
        }
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getDeptId, deptId)
                .eq(SysDept::getDelFlag, "0");
        return deptService.getOne(wrapper);
    }

    private String validateParent(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }
        SysDept parent = getExistingDept(parentId);
        if (parent == null) {
            return "上级部门不存在或已删除";
        }
        return "0".equals(parent.getStatus()) ? null : "上级部门已停用";
    }

    private boolean isValidStatus(String status) {
        return "0".equals(status) || "1".equals(status);
    }
}
