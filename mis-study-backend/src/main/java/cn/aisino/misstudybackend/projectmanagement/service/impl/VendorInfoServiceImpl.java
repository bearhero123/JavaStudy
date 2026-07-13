package cn.aisino.misstudybackend.projectmanagement.service.impl;



import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.VendorInfo;
import cn.aisino.misstudybackend.projectmanagement.mapper.VendorInfoMapper;
import cn.aisino.misstudybackend.projectmanagement.service.IVendorInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VendorInfoServiceImpl extends ServiceImpl<VendorInfoMapper, VendorInfo> implements IVendorInfoService {

    //分页查询功能
    @Override
    public TableDataInfo<VendorInfo> queryPageList(VendorInfo query , PageQuery pageQuery) {
        //page对象是MyBatis-Plus提供的分页对象，用于封装分页参数和结果。
        Page<VendorInfo> page = new Page<>(
                pageQuery.getPageNum(),
                pageQuery.getPageSize()
        );

        //SQL 查询条件的包装器
        LambdaQueryWrapper<VendorInfo> queryWrapper = new LambdaQueryWrapper<>();
        //将 getDelFlag 这个方法本身作为一个对象（函数式接口）传递给了 MyBatis-Plus 框架
        //当 MyBatis-Plus 收到这个方法引用后，截取 getDelFlag 中的 DelFlag 部分，并将其转换为小驼峰命名对应的数据库列名。
        //即 得到数据库字段属性为delFlag == 0 的字段
        queryWrapper.eq(VendorInfo::getDelFlag, "0");

        //like 模糊查询
        queryWrapper.like(
                StringUtils.hasText(query.getVendorName()), //条件判断 判断是否为空，不为空则继续执行后面
                VendorInfo::getVendorName,  //查询字段
                query.getVendorName()  //查询值
        );

        queryWrapper.like(
                StringUtils.hasText(query.getVendorAddr()),
                VendorInfo::getVendorAddr,
                query.getVendorAddr()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactName()),
                VendorInfo::getContactName,
                query.getContactName()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactPhone()),
                VendorInfo::getContactPhone,
                query.getContactPhone()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactEmail()),
                VendorInfo::getContactEmail,
                query.getContactEmail()
        );

        queryWrapper.eq(
                StringUtils.hasText(query.getVendorRatings()),
                VendorInfo::getVendorRatings,
                query.getVendorRatings()
        );

        queryWrapper.like(
                StringUtils.hasText(query.getContactPosition()),
                VendorInfo::getContactPosition,
                query.getContactPosition()
        );

        //按createTime倒序排，把最新的供应商放在最前面
        //orderByDesc框架函数
        queryWrapper.orderByDesc(VendorInfo::getCreateTime);

        //this.page() 是 MyBatis-Plus 内置的分页方法，可以自动查询总条数；自动截取当前页数据
        Page<VendorInfo> resultPage = this.page(page, queryWrapper);

        //将分页结果封装到TableDataInfo中返回
        return TableDataInfo.build(resultPage);
    }

    @Override
    //“重名校验”，判断某个供应商名称是否被其他供应商占用
    public boolean existsByVendorName(String vendorName, Long excludeVendorId){
        //首先确定字段名不为空
        if (!StringUtils.hasText(vendorName)) {
            return false;
        }

        LambdaQueryWrapper<VendorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VendorInfo::getVendorName, vendorName);
        queryWrapper.eq(VendorInfo::getDelFlag, "0");

        //只有当传入的 excludeVendorId（需要排除的供应商ID）不为空时
        //当在做“修改/编辑”操作时传入，在做“新增/添加”操作时不传入。
        if (excludeVendorId != null) {

            //在已有的查询条件（vendor_name = vendorName）基础上，
            // 追加一个条件：vendor_id != excludeVendorId
            //作用：在当前查询中，把“自己”这条记录从查询范围里剔除出去。
            queryWrapper.ne(VendorInfo::getVendorId, excludeVendorId);
        }
        //如果条数 > 0 → 说明除自己之外，确实存在另一个同名的供应商 → 返回 true（表示名称重复，校验不通过）。
        //如果条数 == 0 → 说明除自己之外，不存在同名的供应商 → 返回 false（表示名称可用，校验通过）。
        return this.count(queryWrapper) > 0;
    }
}
