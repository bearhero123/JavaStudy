package cn.aisino.misstudybackend.common;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 表格分页返回结果
 */
@Data
public class TableDataInfo<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 数据列表
     */
    private List<T> rows;

    /**
     * 总记录数
     */
    private Long total;

    public static <T> TableDataInfo<T> success(List<T> rows, Long total) {
        TableDataInfo<T> tableDataInfo = new TableDataInfo<>();
        tableDataInfo.setCode(200);
        tableDataInfo.setMsg("查询成功");
        tableDataInfo.setRows(rows);
        tableDataInfo.setTotal(total);
        return tableDataInfo;
    }
    //把 MyBatis-Plus 的分页结果records + total  转换成前端表格习惯的格式rows + total
    public static <T> TableDataInfo<T> build(IPage<T> page) {
        return success(page.getRecords(), page.getTotal());
    }
}