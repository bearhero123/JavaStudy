package cn.aisino.misstudybackend.projectmanagement.controller;

import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.projectmanagement.domain.ProjectInfo;
import cn.aisino.misstudybackend.projectmanagement.service.IProInforService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/projectInfo")
public class ProInforController {

    private final IProInforService projectInfoService;

    public ProInforController(IProInforService proProjectInfoService) {
        this.projectInfoService = proProjectInfoService;
    }

    /**
     * 查询项目列表
     */
    @GetMapping("/list")
    public TableDataInfo<ProjectInfo> list(ProjectInfo query, PageQuery pageQuery) {
        return projectInfoService.queryPageList(query, pageQuery);
    }

    /**
     * 根据项目ID查询详情
     */
    @GetMapping("/{projectId}")
    public R<ProjectInfo> getInfo(@PathVariable Long projectId) {
        LambdaQueryWrapper<ProjectInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ProjectInfo::getProjectId, projectId);
        queryWrapper.eq(ProjectInfo::getDelFlag, "0");

        ProjectInfo projectInfo = projectInfoService.getOne(queryWrapper);

        return R.success(projectInfo);
    }

    /**
     * 新增项目
     */
    @PostMapping
    public R<Void> add(@RequestBody ProjectInfo projectInfo) {
        boolean result = projectInfoService.save(projectInfo);

        if (result) {
            return R.success();
        }

        return R.fail("新增失败");
    }

    /**
     * 修改项目
     */
    @PutMapping
    public R<Void> edit(@RequestBody ProjectInfo projectInfo) {
        boolean result = projectInfoService.updateById(projectInfo);

        if (result) {
            return R.success();
        }

        return R.fail("修改失败");
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{projectId}")
    public R<Void> remove(@PathVariable Long projectId) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setProjectId(projectId);
        projectInfo.setDelFlag("2");
        projectInfo.setUpdateBy("admin");
        projectInfo.setUpdateTime(LocalDateTime.now());

        boolean result = projectInfoService.updateById(projectInfo);

        if (result) {
            return R.success();
        }

        return R.fail("删除失败");
    }
}
