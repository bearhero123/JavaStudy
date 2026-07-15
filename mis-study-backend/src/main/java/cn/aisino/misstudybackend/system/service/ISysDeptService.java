package cn.aisino.misstudybackend.system.service;

import cn.aisino.misstudybackend.system.domain.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    //树形数据查询
    //抽象方法，省略public abstract
    //部门之间通过 parentId 组成树形结构.
    // queryTreeList:查询部门列表，并把普通列表整理成树形结构。
     List<SysDept> queryTreeList(SysDept query);
    // queryTreeOptions:一般用于新增或修改部门时，选择“上级部门”,需要排除正在修改的当前部门
     List<SysDept> queryTreeOptions(Long excludeDeptId);
    //判断某个上级部门下，是否已经存在同名部门
     boolean existsByDeptName(String deptName, Long parentId, Long excludeDeptId);
    //判断某个部门下是否有子部门
     boolean hasChild(Long deptId);
    //判断某个部门下是否有用户
     boolean hasUser(Long deptId);
    //判断某个部门是否是另一个部门的子孙部门
     boolean isDescendant(Long deptId, Long targetParentId);

}
