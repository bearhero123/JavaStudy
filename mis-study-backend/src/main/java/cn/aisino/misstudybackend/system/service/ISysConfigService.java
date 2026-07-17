package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISysConfigService extends IService<SysConfig> {
    TableDataInfo<SysConfig> queryPageList(SysConfig query, PageQuery pageQuery);
    SysConfig queryDetail(Long configId);
    String queryValueByKey(String configKey);
    boolean existsByConfigKey(String configKey, Long excludeConfigId);
    boolean insertConfig(SysConfig config);
    boolean updateConfig(SysConfig config);
    boolean deleteConfig(Long configId);
}
