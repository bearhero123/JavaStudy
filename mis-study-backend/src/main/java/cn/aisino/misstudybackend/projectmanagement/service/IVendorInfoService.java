package cn.aisino.misstudybackend.projectmanagement.service;

import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.VendorInfo;
import com.baomidou.mybatisplus.extension.service.IService;



public interface IVendorInfoService extends IService<VendorInfo> {
    TableDataInfo<VendorInfo> queryPageList(VendorInfo query, PageQuery pageQuery);
    boolean existsByVendorName(String vendorName, Long excludeVendorId);
}
