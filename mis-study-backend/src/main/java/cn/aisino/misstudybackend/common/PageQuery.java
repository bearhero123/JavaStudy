package cn.aisino.misstudybackend.common;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {


    //当前页码
    private Integer pageNum = 1;


     // 每页条数
    private Integer pageSize = 10;

    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return pageSize;
    }
}