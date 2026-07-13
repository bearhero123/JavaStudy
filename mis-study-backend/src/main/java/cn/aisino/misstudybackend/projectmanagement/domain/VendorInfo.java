package cn.aisino.misstudybackend.projectmanagement.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("per_vendor_info")

public class VendorInfo {

    @TableId(value = "vendor_id",type = IdType.AUTO)

    private Long vendorId;

    private String vendorName;
    private String contactName;
    private String contactPosition;
    private String contactPhone;
    private String contactEmail;
    private String vendorRatings;
    private String vendorAddr;
    private String remark;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private String delFlag;

}
