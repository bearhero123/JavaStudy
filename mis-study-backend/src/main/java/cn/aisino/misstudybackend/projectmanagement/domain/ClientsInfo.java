package cn.aisino.misstudybackend.projectmanagement.domain;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("per_clients_info")
public class ClientsInfo {

    @TableId(value = "client_id",type = IdType.AUTO)
    private Long clientId;
    private String clientName;
    private String clientAddress;
    private String contactName;
    private String contactPosition;
    private String contactPhone;
    private String contactEmail;
    private String clientLevel;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String delFlag;

}
