package cn.aisino.misstudybackend.system.domain;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

//一个用户可以有多个岗位
//一个岗位也可以分配给多个用户
//因此建立关系表，每一行只表示一次岗位分配：
//user_id 可以重复，因为一个用户有多个岗位。
//post_id 可以重复，因为一个岗位属于多个用户。
//但同一个 (user_id, post_id) 组合不能重复。


@Data
@TableName("sys_user_post")
public class SysUserPost {

    private Long userId;
    private Long postId;

}
