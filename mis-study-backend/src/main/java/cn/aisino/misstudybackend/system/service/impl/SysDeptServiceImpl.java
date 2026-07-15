package cn.aisino.misstudybackend.system.service.impl;

import cn.aisino.misstudybackend.system.domain.SysDept;
import cn.aisino.misstudybackend.system.domain.SysUser;
import cn.aisino.misstudybackend.system.mapper.SysDeptMapper;
import cn.aisino.misstudybackend.system.mapper.SysUserMapper;
import cn.aisino.misstudybackend.system.service.ISysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor :自动生成一个包含所有 final（或 @NonNull）字段的构造方法
@RequiredArgsConstructor
//ServiceImp<M, T>  mapper,tableinfor  两个参数分别表示操作数据库用的接口 和 实体表信息
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserMapper sysUserMapper;

    //递归查询
    //从数据库中查询所有未删除的部门，把原本“平铺的部门记录”整理成父子嵌套的树形结构，并根据部门名称、状态进行筛选。
    @Override
    //返回所有部门的树形集合
    public List<SysDept> queryTreeList(SysDept query) {
        /* SysDept query是查询对象，接收前端传来的查询条件
           例如：GET /dept/list?deptName=技术部&status=0
           Spring 会把参数封装成：
            query.getDeptName(); // "技术部"
            query.getStatus();   // "0"
            如果前端没有传任何条件，query 可能为空，也可能是一个字段全为空的 SysDept 对象
         */
        // 创建查询对象
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        //查询所有未删除的部门
        wrapper.eq(SysDept::getDelFlag, "0")
                //按部门序号升序排
                .orderByAsc(SysDept::getOrderNum)
                //按部门id升序排
                .orderByAsc(SysDept::getDeptId);  //即排序号相同时再按部门 ID 排序

        //执行数据库查询，baseMapper 是 ServiceImpl 提供的默认 Mapper 实例。即ServiceImpl<SysDeptMapper, SysDept>
        // selectList(wrapper) 会返回所有符合条件的部门列表，查询结果仍然是平铺结构
        List<SysDept> allDepts = baseMapper.selectList(wrapper);
        //没有部门直接返回空集合
        if (allDepts.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 构建两个映射：id -> 部门对象；parentId -> 子部门列表
        //建立部门ID 对应 部门对象，虽然是一一对应的关系，但是为了提高检索效率，通过映射直接查找会快一些
        //把部门集合转换成 Stream 流。对集合进行批量加工的一种写法
        //Map 里面存放键值对
        Map<Long, SysDept> idMap = allDepts.stream()
                //.collect 表示 将 Stream 处理后的结果收集起来
                //Collectors.toMap(...) ：表示把集合转换成 Map。
                //SysDept::getDeptId ：  决定 Map 的键 ，即key = dept.getDeptId();
                //dept -> dept ： 决定 Map 的值，即value = 当前部门对象本身
                //(old, newV) -> old ： 决定当键重复时，保留旧值还是新值，这里选择保留旧值
                .collect(Collectors.toMap(SysDept::getDeptId, dept -> dept, (old, newV) -> old));

        //父部门 ID 对应子部门列表，即找到每一个部门的所有子部门
        //HashMap 不允许重复键的集合（键值对）
        Map<Long, List<SysDept>> parentChildrenMap = new HashMap<>();
        /*
         遍历所有部门
        这是 Java 的增强 for 循环，也叫 for-each 循环
        依次从 allDepts 列表中取出每一个 SysDept 对象，并把当前对象暂时命名为 dept
        例如：allDepts = [集团总部, 研发中心, 运营中心];
        第一次循环：dept = 集团总部
        第二次循环：dept = 研发中心
         第三次循环：dept = 运营中心
        */
        for (SysDept dept : allDepts) {
            //获取当前部门的id，作为父部门ID
            Long parentId = dept.getParentId();
            //computeIfAbsent ：如果当前 key 不存在，就为当前key创建一个 value 即，为key创建一个新集合
            //k -> new ArrayList<>() 表示无论当前键是什么，都创建一个新的空集合。k 是 Lambda 表达式的参数
            parentChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dept);
        }

        // 3. 补充每个部门的 parentName，虽然父部门ID，但为了方便前端显示（但是只有ID没有名字对应），还是需要把父部门名称补充到每个部门对象中
        for (SysDept dept : allDepts) {
            Long parentId = dept.getParentId();
            //根部门没有父部门会返回null
            if (parentId == null || parentId == 0L) {
                dept.setParentName(null);  // 或 ""，根据前端约定
                //普通部门找父部门
            } else {
                //通过映射关系获取父部门名
                SysDept parent = idMap.get(parentId);
                //三元表达式（如果：那么？否则）
                dept.setParentName(parent != null ? parent.getDeptName() : null);
            }
        }

        // 准备递归构建树
        //visited:用于记录哪些部门已经处理过。避免一个部门被重复处理,避免错误数据导致无限递归。
        //HashSet<> 不允许重复元素的集合（单个元素）
        Set<Long> visited = new HashSet<>();
        //roots:用来存放所有根部门,正常情况下可能只有一个,但系统也允许多个根
        List<SysDept> roots = new ArrayList<>();

        // 4.1 处理正常根节点（parentId=0、null 或映射中不存在的）
        for (SysDept dept : allDepts) {
            Long parentId = dept.getParentId();
            //没有设置父部门||顶级部门||父部门不存在
            if (parentId == null || parentId == 0L || !idMap.containsKey(parentId)) {
                // 处理没有被访问的异常节点
                if (!visited.contains(dept.getDeptId())) {
                    //构建当前部门的完整子树
                    //这个方法会递归找到当前部门的所有子部门，
                    buildTree(dept, parentChildrenMap, visited);
                    roots.add(dept);
                }
            }
        }

        // 4.2 额外处理：如果某些节点没有被访问过（可能是环状数据中的孤立环），将其作为额外的根节点
        for (SysDept dept : allDepts) {
            if (!visited.contains(dept.getDeptId())) {
                // 该节点未被任何根包含，单独作为一个“虚拟根”
                buildTree(dept, parentChildrenMap, visited);
                roots.add(dept);
            }
        }

        if (query == null) {
            return roots;
        }

        // trim() 会去掉查询值前后的空格  三元表达式
        String deptName = StringUtils.hasText(query.getDeptName())
                ? query.getDeptName().trim()
                : null;
        String status = StringUtils.hasText(query.getStatus())
                ? query.getStatus().trim()
                : null;

        if (!StringUtils.hasText(deptName) && !StringUtils.hasText(status)) {
            return roots;
        }

        //调用 filterTree 筛选树
        return filterTree(roots, deptName, status);
    }

    /**
     * 递归构建子树，填充 children 字段（安全版，防止循环引用）
     * @param current      当前部门节点
     * @param childrenMap  父ID -> 子部门列表（原始映射，不会被修改）
     * @param visited      已访问过的部门ID集合，用于防止循环
     */
    private void buildTree(SysDept current,
                           Map<Long, List<SysDept>> childrenMap,
                           Set<Long> visited) {
        Long deptId = current.getDeptId();
        // 若当前节点已访问，直接返回（实际上，这个方法被调用时当前节点还未被访问，但防御性保留）
        if (deptId == null || visited.contains(deptId)) {
            return;
        }
        // 将当前节点标记为已访问
        visited.add(deptId);

        // 获取当前节点的直接子列表（从映射中取出，若没有则返回空列表）
        //getOrDefault: 有这个键就返回对应值，没有就返回默认值。
        List<SysDept> rawChildren = childrenMap.getOrDefault(deptId, Collections.emptyList());

        // 新建一个安全列表，只存放未访问过的子节点
        List<SysDept> safeChildren = new ArrayList<>();

        // 遍历原始子列表，过滤已访问节点
        for (SysDept child : rawChildren) {
            Long childId = child.getDeptId();
            // 如果子节点ID为空或已被访问，则跳过（不加入safeChildren，也不递归）
            if (childId == null || visited.contains(childId)) {
                continue;
            }
            // 未访问过的子节点才加入安全列表，并递归处理
            safeChildren.add(child);
            buildTree(child, childrenMap, visited);
        }

        // 对安全子列表排序（按 order_num、dept_id）
        //sort表示总排序规则按升序，Comparator规定了两两对比，comparingInt规定了对比对象是Num，
        // thenComparingLong表示：如果两个部门的 orderNum 相同，再比较 deptId
        safeChildren.sort(Comparator.comparingInt(SysDept::getOrderNum)
                .thenComparingLong(SysDept::getDeptId));

        // 最终将安全列表设置到当前节点的 children 字段
        current.setChildren(safeChildren);
    }

    /**
     * filterTree：当前节点自己符合条件，或者它的任意子孙节点符合条件，就保留当前节点
     */
    private List<SysDept> filterTree(List<SysDept> nodes,
                                     String deptName,
                                     String status) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysDept> filteredNodes = new ArrayList<>();
        for (SysDept node : nodes) {
            //先递归筛选子部门
            List<SysDept> filteredChildren = filterTree(node.getChildren(), deptName, status);

            //判断部门名称是否匹配
            boolean nameMatches = !StringUtils.hasText(deptName) //没有传部门名字
                    || (StringUtils.hasText(node.getDeptName()) //传了部门名字 && 字符包含匹配
                    && node.getDeptName().contains(deptName));

            //判断状态是否匹配
            boolean statusMatches = !StringUtils.hasText(status)
                    || Objects.equals(node.getStatus(), status);

            //决定是否保留当前节点
            //当前节点自己匹配 或者  某个后代节点匹配 满足其一即可
            if ((nameMatches && statusMatches) || !filteredChildren.isEmpty()) {
                node.setChildren(filteredChildren);
                filteredNodes.add(node);
            }
        }
        return filteredNodes;
    }

    /**
     * 过滤上级部门选择器数据：只保留正常部门，并排除指定部门及其整个子树。
     */
    private List<SysDept> filterTreeOptions(List<SysDept> nodes, Long excludeDeptId) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysDept> options = new ArrayList<>();
        for (SysDept node : nodes) {
            if (node == null
                    || Objects.equals(node.getDeptId(), excludeDeptId)
                    || !"0".equals(node.getStatus())) {
                continue;
            }

            node.setChildren(filterTreeOptions(node.getChildren(), excludeDeptId));
            options.add(node);
        }
        return options;
    }

    @Override
    public List<SysDept> queryTreeOptions(Long excludeDeptId) {
        return filterTreeOptions(queryTreeList(null), excludeDeptId);
    }

    @Override
    public boolean existsByDeptName(String deptName, Long parentId, Long excludeDeptId) {
        //如果deptName为空
        if(!StringUtils.hasText(deptName)){
            return false;
        }
        //trim
        String trimmedName = deptName.trim();
        //parentId 为空处理为0L
        Long effectiveParentId = (parentId == null) ? 0L : parentId;
        //构建查询条件
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getDelFlag,"0")
                    .eq(SysDept::getDeptName,trimmedName)
                    .eq(SysDept::getParentId,effectiveParentId);
        //如果excludeDeptId != null ,添加 deptId != excludeDeptId
        if(excludeDeptId != null){
            queryWrapper.ne(SysDept::getDeptId,excludeDeptId);
        }
        return baseMapper.selectCount(queryWrapper)>0;




    }

    @Override
    public boolean hasChild(Long deptId) {
        if(deptId == null){
            return false;
        }
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getDelFlag,"0")
                .eq(SysDept::getParentId,deptId);

        return baseMapper.selectCount(wrapper)>0;

    }

    @Override
    public boolean hasUser(Long deptId) {
        if(deptId == null){
            return false;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDelFlag,"0")
                .eq(SysUser::getDeptId,deptId);
        return  sysUserMapper.selectCount(wrapper)>0;

    }

    //父级循环检查
    @Override
    public boolean isDescendant(Long deptId, Long targetParentId) {
        if (deptId == null || targetParentId == null) {
            return false;
        }

        Set<Long> visited = new HashSet<>();
        Long currentId = targetParentId;

        while (currentId != null && currentId != 0L) {
            if (Objects.equals(currentId, deptId)) {
                return true;
            }
            if (!visited.add(currentId)) {
                return false;
            }

            LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDept::getDeptId, currentId)
                    .eq(SysDept::getDelFlag, "0");
            SysDept currentDept = baseMapper.selectOne(wrapper);
            if (currentDept == null) {
                return false;
            }
            currentId = currentDept.getParentId();
        }
        return false;
    }


}
