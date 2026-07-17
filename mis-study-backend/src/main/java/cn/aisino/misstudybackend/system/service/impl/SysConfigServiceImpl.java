package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysConfig;
import cn.aisino.misstudybackend.system.mapper.SysConfigMapper;
import cn.aisino.misstudybackend.system.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SysConfigServiceImpl
        extends ServiceImpl<SysConfigMapper, SysConfig>
        implements ISysConfigService {

    @Override
    public TableDataInfo<SysConfig> queryPageList(SysConfig query, PageQuery pageQuery) {
        SysConfig effectiveQuery = query == null ? new SysConfig() : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        Page<SysConfig> page = new Page<>(effectivePage.getPageNum(), effectivePage.getPageSize());
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getDelFlag, "0")
                .like(StringUtils.hasText(effectiveQuery.getConfigName()),
                        SysConfig::getConfigName, effectiveQuery.getConfigName())
                .like(StringUtils.hasText(effectiveQuery.getConfigKey()),
                        SysConfig::getConfigKey, effectiveQuery.getConfigKey())
                .eq(StringUtils.hasText(effectiveQuery.getConfigType()),
                        SysConfig::getConfigType, effectiveQuery.getConfigType())
                .orderByDesc(SysConfig::getConfigId);
        return TableDataInfo.build(page(page, wrapper));
    }

    @Override
    public SysConfig queryDetail(Long configId) {
        if (configId == null) {
            return null;
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigId, configId)
                .eq(SysConfig::getDelFlag, "0");
        return getOne(wrapper);
    }

    @Override
    public String queryValueByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey.trim())
                .eq(SysConfig::getDelFlag, "0");
        SysConfig config = getOne(wrapper);
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public boolean existsByConfigKey(String configKey, Long excludeConfigId) {
        if (!StringUtils.hasText(configKey)) {
            return false;
        }
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey.trim())
                .eq(SysConfig::getDelFlag, "0")
                .ne(excludeConfigId != null, SysConfig::getConfigId, excludeConfigId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean insertConfig(SysConfig config) {
        validateConfig(config, false);
        config.setConfigId(null);
        config.setDelFlag("0");
        config.setCreateBy("admin");
        config.setCreateTime(LocalDateTime.now());
        return baseMapper.insert(config) > 0;
    }

    @Override
    public boolean updateConfig(SysConfig config) {
        validateConfig(config, true);
        config.setDelFlag(null);
        config.setCreateBy(null);
        config.setCreateTime(null);
        config.setUpdateBy("admin");
        config.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(config) > 0;
    }

    @Override
    public boolean deleteConfig(Long configId) {
        if (queryDetail(configId) == null) {
            throw new IllegalArgumentException("参数不存在或已删除");
        }
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        config.setDelFlag("2");
        config.setUpdateBy("admin");
        config.setUpdateTime(LocalDateTime.now());
        return baseMapper.updateById(config) > 0;
    }

    private void validateConfig(SysConfig config, boolean update) {
        if (config == null) {
            throw new IllegalArgumentException("参数数据不能为空");
        }
        if (update && config.getConfigId() == null) {
            throw new IllegalArgumentException("参数ID不能为空");
        }
        if (update && queryDetail(config.getConfigId()) == null) {
            throw new IllegalArgumentException("参数不存在或已删除");
        }
        if (!StringUtils.hasText(config.getConfigName())) {
            throw new IllegalArgumentException("参数名称不能为空");
        }
        if (!StringUtils.hasText(config.getConfigKey())) {
            throw new IllegalArgumentException("参数键名不能为空");
        }
        if (!StringUtils.hasText(config.getConfigValue())) {
            throw new IllegalArgumentException("参数键值不能为空");
        }
        config.setConfigName(config.getConfigName().trim());
        config.setConfigKey(config.getConfigKey().trim());
        config.setConfigValue(config.getConfigValue().trim());
        config.setConfigType(StringUtils.hasText(config.getConfigType())
                ? config.getConfigType().trim().toUpperCase() : "N");
        config.setRemark(StringUtils.hasText(config.getRemark())
                ? config.getRemark().trim() : null);
        if (!"Y".equals(config.getConfigType()) && !"N".equals(config.getConfigType())) {
            throw new IllegalArgumentException("参数类型只能为Y或N");
        }
        if (existsByConfigKey(config.getConfigKey(), update ? config.getConfigId() : null)) {
            throw new IllegalArgumentException("参数键名已存在");
        }
    }
}
