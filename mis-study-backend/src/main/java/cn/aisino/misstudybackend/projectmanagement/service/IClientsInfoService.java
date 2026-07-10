package cn.aisino.misstudybackend.projectmanagement.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.ClientsInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IClientsInfoService extends IService<ClientsInfo> {
    //query 负责“筛选数据”（WHERE 条件）；pageQuery 负责“控制数量”（LIMIT 分页）
    TableDataInfo<ClientsInfo> queryPageList(ClientsInfo query, PageQuery pageQuery);

    //检查数据库中是否存在“指定名称”且“未被删除”的客户记录，用于确保客户名称在系统内的唯一性。
    boolean existsByClientName(String clientName, Long excludeClientId);
}
