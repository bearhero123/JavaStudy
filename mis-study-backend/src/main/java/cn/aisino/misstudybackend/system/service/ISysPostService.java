package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysPost;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysPostService extends IService<SysPost> {

    //岗位数据是平铺结构，适合分页查询,而部门是树状结构不适合分页
    TableDataInfo<SysPost> queryPageList(SysPost query, PageQuery pageQuery);

    List<SysPost> queryOptionList();

    boolean existsByPostCode(String postCode, Long excludePostId);

    boolean existsByPostName(String postName, Long excludePostId);

    boolean hasUser(Long postId);
}
