package cn.aisino.misstudybackend.projectmanagement.service.impl;


import cn.aisino.misstudybackend.projectmanagement.domain.ProjectInfo;
import cn.aisino.misstudybackend.projectmanagement.mapper.ProInfoMapper;
import cn.aisino.misstudybackend.projectmanagement.service.IProInforService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

//把这个类交给 Spring 管理，让它成为一个 Spring Bean。
@Service
//继承 MyBatis-Plus 提供的通用 Service 实现类（ServiceImpl） < 使用哪个 Mapper 操作数据库,操作的是哪张表对应的实体类>
public class ProInforServiceImpl extends ServiceImpl<ProInfoMapper, ProjectInfo> implements IProInforService {
    @Override
    public TableDataInfo<ProjectInfo> queryPageList(ProjectInfo query, PageQuery pageQuery) {
        Page<ProjectInfo> page = new Page<>(
                pageQuery.getPageNum(),
                pageQuery.getPageSize()
        );

        LambdaQueryWrapper<ProjectInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ProjectInfo::getDelFlag, "0");

        queryWrapper.like(
                StringUtils.hasText(query.getProjectNumber()),
                ProjectInfo::getProjectNumber,
                query.getProjectNumber()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getProjectNameCn()),
                ProjectInfo::getProjectNameCn,
                query.getProjectNameCn()
        );

        queryWrapper.eq(
                StringUtils.hasText(query.getProjectType()),
                ProjectInfo::getProjectType,
                query.getProjectType()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getClientName()),
                ProjectInfo::getClientName,
                query.getClientName()
        );

        queryWrapper.orderByDesc(ProjectInfo::getCreateTime);

        Page<ProjectInfo> resultPage = this.page(page, queryWrapper);

        return TableDataInfo.build(resultPage);
    }
}
