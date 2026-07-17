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
@TableName("sys_role")
public class SysRole {
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private String status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;

    @TableField(exist = false)
    private List<Long> menuIds = new ArrayList<>();
}
