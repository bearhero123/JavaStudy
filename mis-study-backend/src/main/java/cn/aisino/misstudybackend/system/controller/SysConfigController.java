package cn.aisino.misstudybackend.system.controller;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysConfig;
import cn.aisino.misstudybackend.system.service.ISysConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class SysConfigController {

    private final ISysConfigService configService;

    public SysConfigController(ISysConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/list")
    public TableDataInfo<SysConfig> list(SysConfig query, PageQuery pageQuery) {
        return configService.queryPageList(query, pageQuery);
    }

    @GetMapping("/key/{configKey}")
    public R<String> getValue(@PathVariable String configKey) {
        String value = configService.queryValueByKey(configKey);
        return value == null ? R.fail("参数不存在或已删除") : R.success(value);
    }

    @GetMapping("/{configId}")
    public R<SysConfig> getInfo(@PathVariable Long configId) {
        SysConfig config = configService.queryDetail(configId);
        return config == null ? R.fail("参数不存在或已删除") : R.success(config);
    }

    @PostMapping
    public R<Void> add(@RequestBody SysConfig config) {
        try {
            return configService.insertConfig(config) ? R.success() : R.fail("新增参数失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @PutMapping
    public R<Void> edit(@RequestBody SysConfig config) {
        try {
            return configService.updateConfig(config) ? R.success() : R.fail("修改参数失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }

    @DeleteMapping("/{configId}")
    public R<Void> remove(@PathVariable Long configId) {
        try {
            return configService.deleteConfig(configId) ? R.success() : R.fail("删除参数失败");
        } catch (RuntimeException ex) {
            return R.fail(ex.getMessage());
        }
    }
}
