package cn.aisino.misstudybackend.projectmanagement.service.impl;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.ClientsInfo;
import cn.aisino.misstudybackend.projectmanagement.mapper.ClientsInfoMapper;
import cn.aisino.misstudybackend.projectmanagement.service.IClientsInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
public class ClientsInfoServiceImpl
        extends ServiceImpl<ClientsInfoMapper, ClientsInfo>
        implements IClientsInfoService {

    //分页查询功能实现
    @Override
    public TableDataInfo<ClientsInfo> queryPageList(ClientsInfo query, PageQuery pageQuery) {
        Page<ClientsInfo> page = new Page<>(
                pageQuery.getPageNum(),   //例如查第2页
                pageQuery.getPageSize()   //例如每页10条
        );  //page对象是MyBatis-Plus提供的分页对象，用于封装分页参数和结果。

        LambdaQueryWrapper <ClientsInfo> queryWrapper = new LambdaQueryWrapper<>();

        // like 表示模糊查询（用户自由输入的文本）；
        //eq表示精确查询（标准化枚举/字典值）
        //ne表示不等于、排除在外	修改时查重/忽略某条记录（排除自己）
        //getXxx  会映射到数据库列名  Xxx -> xxx 进而得到数据库列名
        queryWrapper.eq(ClientsInfo::getDelFlag,"0");


        queryWrapper.like(
                StringUtils.hasText(query.getClientName()), //条件判断
                ClientsInfo::getClientName,  //查询字段
                query.getClientName()   //查询值
        );

        queryWrapper.like(
                StringUtils.hasText(query.getClientAddress()),
                ClientsInfo::getClientAddress,
                query.getClientAddress()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactName()),
                ClientsInfo::getContactName,
                query.getContactName()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactPhone()),
                ClientsInfo::getContactPhone,
                query.getContactPhone()
        );

        queryWrapper.eq(
                StringUtils.hasText(query.getClientLevel()),
                ClientsInfo::getClientLevel,
                query.getClientLevel()
        );
        //Desc 表示降序：数值从大到小，时间从晚到早
        // Asc 表示升序：数值从小到大，时间从早到晚
        //查完数据后，按创建时间倒序排，把最新的客户放在最前面
        queryWrapper.orderByDesc(ClientsInfo::getCreateTime);

        //this.page() 是 MyBatis-Plus 内置的分页方法，可以自动查询总条数；自动截取当前页数据
        Page<ClientsInfo> resultPage = this.page(page, queryWrapper);

        //封装为固定格式{ "total": 100, "rows": [...] }
        return TableDataInfo.build(resultPage);

    }

    @Override
    public boolean existsByClientName(String clientName, Long excludeClientId) {
        if(!StringUtils.hasText(clientName)) {
            return false;
        }
        LambdaQueryWrapper<ClientsInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClientsInfo::getClientName, clientName);
        queryWrapper.eq(ClientsInfo::getDelFlag, "0");
        queryWrapper.ne(excludeClientId != null, ClientsInfo::getClientId, excludeClientId);
        return this.count(queryWrapper) > 0;
    }

}
