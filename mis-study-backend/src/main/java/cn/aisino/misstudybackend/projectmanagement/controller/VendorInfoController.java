package cn.aisino.misstudybackend.projectmanagement.controller;


import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.VendorInfo;
import cn.aisino.misstudybackend.projectmanagement.service.IVendorInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/vendor")
public class VendorInfoController {
    private final IVendorInfoService vendorInfoService;

    public VendorInfoController(IVendorInfoService vendorInfoService) {
        this.vendorInfoService = vendorInfoService;
    }

    //查询供应商列表
    @GetMapping("/list")
    public TableDataInfo<VendorInfo> list(VendorInfo query, PageQuery pageQuery) {
        //将接收到的查询条件和分页参数，直接交给业务层（IVendorInfoService）的 queryPageList 方法去处理
        //最终返回一个 TableDataInfo<VendorInfo> 对象
        return vendorInfoService.queryPageList(query, pageQuery);
    }

    //根据供应商ID查询供应商信息
    @GetMapping("/{vendorId}")
    //接收 @PathVariable Long vendorId  ，即从路径中拿到 vendorId
    //这里一定是R类型，R会返回状态码、消息、数据。如果是实体类的话只会返回数据
    public R<VendorInfo> getInfo(@PathVariable Long vendorId) {
        //创建查询对象
        LambdaQueryWrapper<VendorInfo> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件：供应商ID = vendorId
        queryWrapper.eq(VendorInfo::getVendorId, vendorId);
        //添加条件：删除标志 = 0，也就是未删除
        queryWrapper.eq(VendorInfo::getDelFlag, "0");
        //调用 vendorInfoService.getOne(queryWrapper) 查询数据库
        //根据前面组装好的查询条件（queryWrapper），去数据库里查一条供应商数据。
        //如果数据库里有符合条件的数据，就返回一个 VendorInfo 对象。如果没有查到就返回null
        VendorInfo vendorInfo = vendorInfoService.getOne(queryWrapper);
        //把查到的供应商对象放进 R.success(...) 返回
        return R.success(vendorInfo);
    }

    //新增供应商
    @PostMapping
    //@RequestBody: 告诉 Spring，前端传来的 HTTP 请求体（Body）里的 JSON 数据，直接转换成 ClientsInfo 对象。
    public R<Void> add(@RequestBody VendorInfo vendorInfo) {

        if (!StringUtils.hasText(vendorInfo.getVendorName())) {
            return R.fail("供应商名称不能为空");
        }

        if (!StringUtils.hasText(vendorInfo.getContactName())) {
            return R.fail("联系人不能为空");
        }

        if (vendorInfoService.existsByVendorName(vendorInfo.getVendorName(), null)) {
            return R.fail("供应商名称已存在");
        }

        vendorInfo.setDelFlag("0");
        vendorInfo.setCreateBy("admin");
        vendorInfo.setCreateTime(LocalDateTime.now());

        //saveOrUpdate自动判断是修改还是新增
        boolean result = vendorInfoService.save(vendorInfo);

        if (result) {
            return R.success();
        }

        return R.fail("新建供应商失败");
    }

    //修改供应商信息
    @PutMapping
    public R<Void> edit(@RequestBody VendorInfo vendorInfo) {

        if (vendorInfo.getVendorId() == null) {
            return R.fail("供应商ID不能为空");
        }

        if (!StringUtils.hasText(vendorInfo.getVendorName())) {
            return R.fail("供应商名称不能为空");
        }

        if (!StringUtils.hasText(vendorInfo.getContactName())) {
            return R.fail("联系人不能为空");
        }

        if (vendorInfoService.existsByVendorName(vendorInfo.getVendorName(), vendorInfo.getVendorId())) {
            return R.fail("供应商名称已存在");
        }

        vendorInfo.setUpdateBy("admin");
        vendorInfo.setUpdateTime(LocalDateTime.now());

        boolean result = vendorInfoService.updateById(vendorInfo);

        if (result) {
            return R.success();
        }

        return R.fail("修改供应商失败");
    }


    //删除接口
    @DeleteMapping("/{vendorId}")
    public R<Void> remove(@PathVariable Long vendorId) {
        //逻辑删除：把 delFlag 改为 2
        VendorInfo vendorInfo = new VendorInfo();
        vendorInfo.setVendorId(vendorId);
        vendorInfo.setDelFlag("2");
        vendorInfo.setUpdateBy("admin");
        vendorInfo.setUpdateTime(LocalDateTime.now());

        boolean result = vendorInfoService.updateById(vendorInfo);

        if (result) {
            return R.success();
        }

        return R.fail("删除供应商失败");
    }

    //判断存在接口
    @GetMapping("/nameExists")
    public R<Boolean> nameExists(
            //@RequestParam：从请求地址里取 clientName 参数
            @RequestParam String vendorName,
            //尝试从URL中获取 excludeClientId 参数，如果能拿到就赋值，如果拿不到（前端没传），就把 null 赋值给这个变量，不会报错。
            //Long 这是一个对象类型，它可以接收 null
            @RequestParam(required = false) Long excludeVendorId) {
        boolean exists = vendorInfoService.existsByVendorName(vendorName, excludeVendorId);
        return R.success(exists);
    }
}
