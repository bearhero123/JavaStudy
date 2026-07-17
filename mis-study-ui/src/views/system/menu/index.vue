<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addMenu,
  deleteMenu,
  getMenu,
  getMenuTreeOptions,
  listMenu,
  updateMenu,
  type MenuQuery,
  type SysMenu,
} from '@/api/system/menu'

defineOptions({ name: 'SystemMenuManagement' })

interface MenuForm {
  menuId?: number
  parentId?: number
  menuName: string
  orderNum: number
  path: string
  component?: string
  menuType: string
  visible: string
  status: string
  perms?: string
  icon?: string
  remark?: string
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const tableRef = ref()
const menuList = ref<SysMenu[]>([])
const parentOptions = ref<SysMenu[]>([])
const queryParams = reactive<MenuQuery>({ menuName: '', status: '' })
const form = reactive<MenuForm>({
  menuId: undefined,
  parentId: 0,
  menuName: '',
  orderNum: 0,
  path: '',
  component: '',
  menuType: 'C',
  visible: '0',
  status: '0',
  perms: '',
  icon: '#',
  remark: '',
})
const rules = reactive<FormRules<MenuForm>>({
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入显示顺序', trigger: 'change' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择菜单状态', trigger: 'change' }],
})
const treeProps = { value: 'menuId', label: 'menuName', children: 'children' }

function isSuccess(response: { code: number; msg: string }) {
  if (response.code === 200) return true
  ElMessage.error(response.msg || '操作失败')
  return false
}

async function getList() {
  loading.value = true
  try {
    const response = await listMenu(queryParams)
    if (isSuccess(response)) menuList.value = response.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    menuId: undefined, parentId: 0, menuName: '', orderNum: 0, path: '', component: '',
    menuType: 'C', visible: '0', status: '0', perms: '', icon: '#', remark: '',
  })
  formRef.value?.clearValidate()
}

async function loadParentOptions(excludeMenuId?: number) {
  const response = await getMenuTreeOptions(excludeMenuId)
  if (!isSuccess(response)) return false
  parentOptions.value = [{ menuId: 0, menuName: '无（作为顶级菜单）', children: response.data || [] }]
  return true
}

async function handleAdd(parent?: SysMenu) {
  resetForm()
  form.parentId = parent?.menuId || 0
  if (!(await loadParentOptions())) return
  dialogVisible.value = true
}

async function handleEdit(row: SysMenu) {
  if (!row.menuId) return
  resetForm()
  const [detailResponse, optionsLoaded] = await Promise.all([
    getMenu(row.menuId),
    loadParentOptions(row.menuId),
  ])
  if (!optionsLoaded || !isSuccess(detailResponse)) return
  Object.assign(form, detailResponse.data || row)
  form.parentId = form.parentId || 0
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.menuType === 'C' && (!form.path.trim() || !form.component?.trim())) {
    ElMessage.warning('菜单类型为C时，路由地址和组件路径不能为空')
    return
  }
  if (form.menuType === 'F' && !form.perms?.trim()) {
    ElMessage.warning('按钮类型菜单的权限标识不能为空')
    return
  }
  submitLoading.value = true
  try {
    const response = form.menuId ? await updateMenu({ ...form }) : await addMenu({ ...form })
    if (!isSuccess(response)) return
    ElMessage.success(form.menuId ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysMenu) {
  if (!row.menuId) return
  try {
    await ElMessageBox.confirm(`确认删除菜单【${row.menuName}】吗？`, '删除确认', { type: 'warning' })
    const response = await deleteMenu(row.menuId)
    if (!isSuccess(response)) return
    ElMessage.success('删除成功')
    await getList()
  } catch (error) {
    console.log('取消删除或删除请求失败：', error)
  }
}

function setAllExpanded(expanded: boolean) {
  const toggle = (nodes: SysMenu[]) => {
    for (const node of nodes) {
      tableRef.value?.toggleRowExpansion(node, expanded)
      if (node.children?.length) toggle(node.children)
    }
  }
  nextTick(() => toggle(menuList.value))
}

onMounted(getList)
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="菜单名称"><el-input v-model="queryParams.menuName" clearable @keyup.enter="getList" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="queryParams.status" clearable style="width: 130px"><el-option label="正常" value="0" /><el-option label="停用" value="1" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="getList">查询</el-button><el-button @click="queryParams.menuName = ''; queryParams.status = ''; getList()">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card>
      <template #header><div class="card-header"><span>菜单管理</span><div><el-button @click="setAllExpanded(true)">全部展开</el-button><el-button @click="setAllExpanded(false)">全部折叠</el-button><el-button type="primary" @click="handleAdd()">新增顶级菜单</el-button></div></div></template>
      <el-table ref="tableRef" v-loading="loading" :data="menuList" row-key="menuId" border :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="220" />
        <el-table-column label="类型" width="90" align="center"><template #default="{ row }">{{ row.menuType === 'M' ? '目录' : row.menuType === 'C' ? '菜单' : '按钮' }}</template></el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="path" label="路由地址" min-width="160" />
        <el-table-column prop="perms" label="权限标识" min-width="180" />
        <el-table-column label="状态" width="90" align="center"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="handleAdd(row)">新增子菜单</el-button><el-button link type="primary" @click="handleEdit(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.menuId ? '编辑菜单' : '新增菜单'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="上级菜单"><el-tree-select v-model="form.parentId" :data="parentOptions" :props="treeProps" node-key="menuId" check-strictly default-expand-all style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="菜单类型" prop="menuType"><el-radio-group v-model="form.menuType"><el-radio value="M">目录</el-radio><el-radio value="C">菜单</el-radio><el-radio value="F">按钮</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="菜单名称" prop="menuName"><el-input v-model="form.menuName" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="显示顺序" prop="orderNum"><el-input-number v-model="form.orderNum" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col v-if="form.menuType !== 'F'" :span="12"><el-form-item label="路由地址"><el-input v-model="form.path" maxlength="200" /></el-form-item></el-col>
          <el-col v-if="form.menuType === 'C'" :span="12"><el-form-item label="组件路径"><el-input v-model="form.component" maxlength="255" /></el-form-item></el-col>
          <el-col v-if="form.menuType !== 'M'" :span="12"><el-form-item label="权限标识"><el-input v-model="form.perms" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="图标"><el-input v-model="form.icon" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="显示状态"><el-radio-group v-model="form.visible"><el-radio value="0">显示</el-radio><el-radio value="1">隐藏</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="菜单状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 20px; }
.query-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
