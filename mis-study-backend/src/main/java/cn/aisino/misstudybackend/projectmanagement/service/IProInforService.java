package cn.aisino.misstudybackend.projectmanagement.service;
import cn.aisino.misstudybackend.projectmanagement.domain.ProjectInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;

public interface IProInforService extends IService<ProjectInfo> {
    //以后分页查询由 Service 负责，不放在 Controller 里堆代码
    TableDataInfo<ProjectInfo> queryPageList(ProjectInfo query, PageQuery pageQuery);

}