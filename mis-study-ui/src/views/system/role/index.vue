<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleMenuTree, listMenu, type SysMenu } from '@/api/system/menu'
import {
  addRole,
  deleteRole,
  getRole,
  listRole,
  updateRole,
  type RoleQuery,
  type SysRole,
} from '@/api/system/role'

defineOptions({ name: 'SystemRoleManagement' })

interface RoleForm {
  roleId?: number
  roleName: string
  roleKey: string
  roleSort: number
  status: string
  remark: string
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref()
const roleList = ref<SysRole[]>([])
const menuTree = ref<SysMenu[]>([])
const total = ref(0)

const queryParams = reactive<RoleQuery>({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  roleKey: '',
  status: '',
})

const form = reactive<RoleForm>({
  roleId: undefined,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: '',
})

const rules = reactive<FormRules<RoleForm>>({
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
  roleSort: [{ required: true, message: '请输入显示顺序', trigger: 'change' }],
  status: [{ required: true, message: '请选择角色状态', trigger: 'change' }],
})

function isSuccess(response: { code: number; msg: string }) {
  if (response.code === 200) return true
  ElMessage.error(response.msg || '操作失败')
  return false
}

function formatDate(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

async function getList() {
  loading.value = true
  try {
    const response = await listRole(queryParams)
    if (isSuccess(response)) {
      roleList.value = response.rows || []
      total.value = response.total || 0
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.roleId = undefined
  form.roleName = ''
  form.roleKey = ''
  form.roleSort = 0
  form.status = '0'
  form.remark = ''
  menuTree.value = []
  formRef.value?.clearValidate()
}

async function handleAdd() {
  resetForm()
  const response = await listMenu()
  if (!isSuccess(response)) return
  menuTree.value = response.data || []
  dialogVisible.value = true
  await nextTick()
  menuTreeRef.value?.setCheckedKeys([])
}

async function handleEdit(row: SysRole) {
  if (!row.roleId) return
  resetForm()
  const [detailResponse, treeResponse] = await Promise.all([
    getRole(row.roleId),
    getRoleMenuTree(row.roleId),
  ])
  if (!isSuccess(detailResponse) || !isSuccess(treeResponse)) return
  const detail = detailResponse.data || row
  form.roleId = detail.roleId
  form.roleName = detail.roleName || ''
  form.roleKey = detail.roleKey || ''
  form.roleSort = detail.roleSort ?? 0
  form.status = detail.status || '0'
  form.remark = detail.remark || ''
  menuTree.value = treeResponse.data?.menus || []
  dialogVisible.value = true
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(treeResponse.data?.checkedKeys || [])
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload: SysRole = {
      ...form,
      menuIds: (menuTreeRef.value?.getCheckedKeys(false) || []) as number[],
    }
    const response = form.roleId ? await updateRole(payload) : await addRole(payload)
    if (!isSuccess(response)) return
    ElMessage.success(form.roleId ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysRole) {
  if (!row.roleId) return
  try {
    await ElMessageBox.confirm(`确认删除角色【${row.roleName}】吗？`, '删除确认', {
      type: 'warning',
    })
    const response = await deleteRole(row.roleId)
    if (!isSuccess(response)) return
    ElMessage.success('删除成功')
    if (roleList.value.length === 1 && queryParams.pageNum > 1) queryParams.pageNum -= 1
    await getList()
  } catch (error) {
    console.log('取消删除或删除请求失败：', error)
  }
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.roleName = ''
  queryParams.roleKey = ''
  queryParams.status = ''
  getList()
}

onMounted(getList)
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" clearable @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="queryParams.roleKey" clearable @keyup.enter="getList" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable style="width: 130px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="queryParams.pageNum = 1; getList()">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="roleList" border stripe>
        <el-table-column prop="roleId" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleKey" label="权限字符" min-width="180" />
        <el-table-column prop="roleSort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="queryParams.pageNum = 1; getList()"
          @current-change="getList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="权限字符" prop="roleKey"><el-input v-model="form.roleKey" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="显示顺序" prop="roleSort"><el-input-number v-model="form.roleSort" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="菜单授权"><el-tree ref="menuTreeRef" :data="menuTree" node-key="menuId" show-checkbox default-expand-all check-strictly :props="{ label: 'menuName', children: 'children' }" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 20px; }
.query-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
