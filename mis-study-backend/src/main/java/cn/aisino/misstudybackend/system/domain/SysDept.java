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
@TableName("sys_dept")
public class SysDept {
    @TableId(value = "dept_id",type = IdType.AUTO)

    private Long deptId;
    private Long parentId;
    private String deptName;
    private Integer orderNum;
    private String status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;

    // ========== 非数据库字段 ==========

    /**
     * 子部门列表（树形结构用）
     * exist = false 表示：增删改查时，MyBatis-Plus 会完全忽略它
     */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();

    /**
     * 父部门名称（联表查询时用）
     */
    @TableField(exist = false)
    private String parentName;
}
