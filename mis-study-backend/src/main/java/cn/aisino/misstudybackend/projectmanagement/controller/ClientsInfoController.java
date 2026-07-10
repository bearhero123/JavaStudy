package cn.aisino.misstudybackend.projectmanagement.controller;


import cn.aisino.misstudybackend.common.PageQuery;
import cn.aisino.misstudybackend.common.R;
import cn.aisino.misstudybackend.common.TableDataInfo;
import cn.aisino.misstudybackend.projectmanagement.domain.ClientsInfo;
import cn.aisino.misstudybackend.projectmanagement.service.IClientsInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

//@RestController 其实是 @Controller + @ResponseBody 这两个注解的组合体
//作用：类里的每个方法的返回值（对象、Map、List）都会自动变成 JSON 格式返回给前端。
@RestController

//将这个 Controller 类中所有的接口请求地址，都统一以 /clients 作为前缀开头
@RequestMapping("/clients")
public class ClientsInfoController {
    //这个 Controller 类，需要依赖一个具备项目服务能力（IProProjectInfoService）的对象，并声明这个依赖是私有的、固定不变的
    //Controller只需要调用接口，因为已经完成了实现类，所以可以写 clientsInfoService.xxx()   （多态）
    //这是声明的一个字段，一个成员变量
    private final IClientsInfoService clientsInfoService;

    public ClientsInfoController(IClientsInfoService clientsInfoService){
        //局部变量（clientsInfoService）赋值给前面的成员变量（clientsInfoService）
        this.clientsInfoService = clientsInfoService;
    }

    //查询客户列表
    @GetMapping("/list")
    public TableDataInfo<ClientsInfo> list(ClientsInfo query, PageQuery pageQuery) {
        return clientsInfoService.queryPageList(query, pageQuery);
    }

    //根据客户ID查询
    //前端请求 /clients/{clientId}
    @GetMapping("/{clientId}")
    //从路径中拿到 clientId
    public R<ClientsInfo> getInfo(@PathVariable Long clientId) {
        //创建一个查询对象，queryWrapper ：装 SQL 查询条件的盒子
        LambdaQueryWrapper<ClientsInfo> queryWrapper = new LambdaQueryWrapper<>();

        //queryWrapper.eq 返回的是自己本身，后面条件组装要用
        //添加条件：客户ID = clientId
        queryWrapper.eq(ClientsInfo::getClientId, clientId);
        //添加条件：删除标志 = 0，也就是未删除
        queryWrapper.eq(ClientsInfo::getDelFlag, "0");
        //调用 clientsInfoService.getOne(queryWrapper) 查询数据库
        //根据前面组装好的查询条件（queryWrapper），去数据库里查一条客户数据。
        //如果数据库里有符合条件的数据，就返回一个 ClientsInfo 对象。如果没有查到就返回null
        ClientsInfo clientsInfo = clientsInfoService.getOne(queryWrapper);
        //把查到的客户对象放进 R.success(...) 返回
        return R.success(clientsInfo);
    }
    //新增客户
    @PostMapping   //因为没有写路径，默认继承类的("/clients")
    //告诉 Spring，前端传来的 HTTP 请求体（Body）里的 JSON 数据，直接转换成 ClientsInfo 对象。
    public R<Void> add(@RequestBody ClientsInfo clientsInfo) {
        //clientsInfo.getClientName() 从 clientsInfo 这个客户对象里，取出客户名称。
        //StringUtils.hasText(...) 判断这个字符串是否有内容。
        if (!StringUtils.hasText(clientsInfo.getClientName())) {
            return R.fail("客户名称不能为空");
        }
        //前端虽然可能已经做了必填校验，但后端必须再次校验，existsByClientName=是否存在+通过+客户名称
        //第二个参数excludeByClientID表示要排除的客户ID，等于null时表示不排除任何客户
        if (clientsInfoService.existsByClientName(clientsInfo.getClientName(), null)) {
            return R.fail("客户名称已存在");
        }

        clientsInfo.setDelFlag("0");  // 标记为“未删除”
        clientsInfo.setCreateBy("admin");  // 记录创建人
        clientsInfo.setCreateTime(LocalDateTime.now());  // 记录创建时间


        //执行数据库插入，返回值是一个布尔值，表示是否插入成功
        boolean result = clientsInfoService.save(clientsInfo);

        if (result) {
            return R.success();   // 成功：返回 {"code":200, "msg":"操作成功"}
        }
        return R.fail("新增客户失败");
    }

    /**
     * 修改客户
     */
    @PutMapping
    public R<Void> edit(@RequestBody ClientsInfo clientsInfo) {
        if (clientsInfo.getClientId() == null) {
            return R.fail("客户ID不能为空");
        }

        if (!StringUtils.hasText(clientsInfo.getClientName())) {
            return R.fail("客户名称不能为空");
        }

        if (clientsInfoService.existsByClientName(
                clientsInfo.getClientName(),
                clientsInfo.getClientId()
        )) {
            return R.fail("客户名称已存在");
        }

        clientsInfo.setUpdateBy("admin");
        clientsInfo.setUpdateTime(LocalDateTime.now());

        boolean result = clientsInfoService.updateById(clientsInfo);

        if (result) {
            return R.success();
        }

        return R.fail("修改客户失败");
    }


     //删除客户：逻辑删除
    @DeleteMapping("/{clientId}")
    public R<Void> remove(@PathVariable Long clientId) {
        ClientsInfo clientsInfo = new ClientsInfo();
        //把路径里的 clientId 设置到 clientsInfo.clientId 里
        clientsInfo.setClientId(clientId);
        //把删除标志设置成 "2"，表示逻辑删除。
        clientsInfo.setDelFlag("2");
        //设置是谁修改的，这里写死为 "admin"
        clientsInfo.setUpdateBy("admin");
        //设置修改时间为当前时间
        clientsInfo.setUpdateTime(LocalDateTime.now());

        boolean result = clientsInfoService.updateById(clientsInfo);

        if (result) {
            return R.success();
        }

        return R.fail("删除客户失败");
    }


     //客户名称是否已存在
    @GetMapping("/nameExists")
    public R<Boolean> nameExists(
            //@RequestParam：从请求地址里取一个参数，把它存进 clientName 里
            @RequestParam String clientName,
            //尝试从URL中获取 excludeClientId 参数，如果能拿到就赋值，如果拿不到（前端没传），就把 null 赋值给这个变量，不会报错。
            //Long 这是一个对象类型，它可以接收 null
            @RequestParam(required = false) Long excludeClientId
    ) {
        //existsByClientName 方法，把前端传过来的两个参数原封不动地传过去。
        return R.success(clientsInfoService.existsByClientName(clientName, excludeClientId));
    }
}


