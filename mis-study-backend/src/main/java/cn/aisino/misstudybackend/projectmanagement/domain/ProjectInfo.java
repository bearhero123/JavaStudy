package cn.aisino.misstudybackend.projectmanagement.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pro_project_info")

public class ProjectInfo {

    @TableId(value = "project_id", type = IdType.AUTO)
    private Long projectId;
    private String projectNumber;
    private String projectAbbreviation;
    private String projectNameCn;
    private String projectNameEn;
    private String projectType;
    private String projectStatus;
    private Long pmPersonId;
    private String pmName;
    private Long clientId;
    private String clientName;
    private BigDecimal projectScale;
    private String currency;
    private String businessArea;
    private String contractStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;

}
