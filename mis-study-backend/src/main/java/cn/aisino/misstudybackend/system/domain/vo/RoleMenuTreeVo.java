package cn.aisino.misstudybackend.system.domain.vo;


import cn.aisino.misstudybackend.system.domain.SysMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleMenuTreeVo {

    private List<SysMenu> menus = new ArrayList<>();
    private List<Long> checkedKeys = new ArrayList<>();
}
