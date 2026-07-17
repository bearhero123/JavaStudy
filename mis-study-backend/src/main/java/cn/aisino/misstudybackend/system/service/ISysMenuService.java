package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.system.domain.SysMenu;
import cn.aisino.misstudybackend.system.domain.vo.RoleMenuTreeVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    //查询并组装菜单树
    List<SysMenu> queryTreeList(SysMenu query);

    //生成父菜单选择树，编辑时排除自身和后代
    List<SysMenu> queryTreeOptions(Long excludeMenuId);

    //查询一条未删除菜单
    SysMenu queryDetail(Long menuId);

    //返回完整菜单树和角色已选菜单。
    RoleMenuTreeVo queryRoleMenuTree(Long roleId);

    //完成重名、子节点和角色引用检查
    boolean existsByMenuName(
            Long parentId,
            String menuName,
            Long excludeMenuId);

    boolean hasChild(Long menuId);

    boolean hasRoleReference(Long menuId);

    //防止菜单形成父级循环
    boolean isDescendant(
            Long menuId,
            Long targetParentId);

    //封装新增、修改和逻辑删除流程
    boolean insertMenu(SysMenu menu);

    boolean updateMenu(SysMenu menu);

    boolean deleteMenu(Long menuId);

}
