package cn.aisino.misstudybackend.system.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;
    private Long parentId;
    private String menuName;
    private Integer orderNum;
    private String path;
    private String component;
    private String menuType;
    private String visible;
    private String status;
    private String perms;
    private String icon;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;


    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
