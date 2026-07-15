package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISysUserService extends IService<SysUser> {

    TableDataInfo<SysUser> queryPageList(SysUser query, PageQuery pageQuery);

    SysUser queryDetail(Long userId);

    boolean existsByUserName(String userName, Long excludeUserId);

    boolean existsByPhone(String phonenumber, Long excludeUserId);

    boolean existsByEmail(String email, Long excludeUserId);

    boolean insertUser(SysUser user);

    boolean updateUser(SysUser user);

    boolean deleteUser(Long userId);
}
