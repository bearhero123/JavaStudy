package cn.aisino.misstudybackend.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(value = "user_id",type = IdType.AUTO)
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String email;
    private String phonenumber;
    private String sex;
    //WRITE_ONLY：请求可以传入密码，但接口响应不会输出密码。
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    //NOT_EMPTY：更新时密码为空字符串或 null 就不会覆盖数据库原密码。
    @TableField(updateStrategy = FieldStrategy.NOT_EMPTY)
    private String password;

    private String status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;

    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private List<Long> postIds = new ArrayList<>();
    @TableField(exist = false)
    private List<String> postNames = new ArrayList<>();
    @TableField(exist = false)
    private List<Long> roleIds = new ArrayList<>();
    @TableField(exist = false)
    private List<String> roleNames = new ArrayList<>();
}
