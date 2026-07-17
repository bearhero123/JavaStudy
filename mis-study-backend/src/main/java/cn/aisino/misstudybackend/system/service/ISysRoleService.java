package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.system.domain.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    TableDataInfo<SysRole> queryPageList(SysRole query, PageQuery pageQuery);
    List<SysRole> queryOptionList();
    SysRole queryDetail(Long roleId);
    boolean existsByRoleName(String roleName, Long excludeRoleId);
    boolean existsByRoleKey(String roleKey, Long excludeRoleId);
    boolean hasUserReference(Long roleId);
    boolean insertRole(SysRole role);
    boolean updateRole(SysRole role);
    boolean deleteRole(Long roleId);
}
