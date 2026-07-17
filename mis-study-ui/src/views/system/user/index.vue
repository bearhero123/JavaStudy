<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTreeOptions, type SysDept } from '@/api/system/dept'
import { listPostOptions, type SysPost } from '@/api/system/post'
import { listRoleOptions, type SysRole } from '@/api/system/role'
import {
  addUser,
  checkUserNameExists,
  deleteUser,
  getUser,
  listUser,
  updateUser,
  type SysUser,
  type UserQuery,
} from '@/api/system/user'

defineOptions({ name: 'SystemUserManagement' })

const treeProps = {
  value: 'deptId',
  label: 'deptName',
  children: 'children',
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref<FormInstance>()

const userList = ref<SysUser[]>([])
const total = ref(0)
const deptOptions = ref<SysDept[]>([])
const postOptions = ref<SysPost[]>([])
const roleOptions = ref<SysRole[]>([])
const detailData = ref<SysUser>({})

const queryParams = reactive<UserQuery>({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  nickName: '',
  phonenumber: '',
  status: '',
  deptId: undefined,
})

const form = reactive<SysUser>({
  userId: undefined,
  deptId: undefined,
  userName: '',
  nickName: '',
  email: '',
  phonenumber: '',
  sex: '0',
  password: '',
  status: '0',
  remark: '',
  postIds: [],
  roleIds: [],
})

const rules = reactive<FormRules<SysUser>>({
  userName: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: ['blur', 'change'],
    },
  ],
  status: [{ required: true, message: '请选择用户状态', trigger: 'change' }],
})

function isSuccess(response: { code: number; msg: string }) {
  if (response.code === 200) {
    return true
  }

  ElMessage.error(response.msg || '操作失败')
  return false
}

function displayPostNames(postNames?: string[]) {
  return postNames?.length ? postNames.join('、') : '-'
}

function displayRoleNames(roleNames?: string[]) {
  return roleNames?.length ? roleNames.join('、') : '-'
}

function displaySex(sex?: string) {
  if (sex === '1') {
    return '男'
  }
  if (sex === '2') {
    return '女'
  }
  return '未知'
}

function formatDate(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

async function getList() {
  loading.value = true

  try {
    const response = await listUser(queryParams)
    if (isSuccess(response)) {
      userList.value = response.rows || []
      total.value = response.total || 0
    }
  } catch (error) {
    console.error('查询用户列表失败：', error)
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [deptResponse, postResponse, roleResponse] = await Promise.all([
      getDeptTreeOptions(),
      listPostOptions(),
      listRoleOptions(),
    ])

    const deptSuccess = isSuccess(deptResponse)
    const postSuccess = isSuccess(postResponse)
    const roleSuccess = isSuccess(roleResponse)
    if (!deptSuccess || !postSuccess || !roleSuccess) {
      return false
    }

    deptOptions.value = deptResponse.data || []
    postOptions.value = postResponse.data || []
    roleOptions.value = roleResponse.data || []
    return true
  } catch (error) {
    console.error('查询部门或岗位选项失败：', error)
    return false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.userName = ''
  queryParams.nickName = ''
  queryParams.phonenumber = ''
  queryParams.status = ''
  queryParams.deptId = undefined
  getList()
}

function handleSizeChange(pageSize: number) {
  queryParams.pageSize = pageSize
  queryParams.pageNum = 1
  getList()
}

function handleCurrentChange(pageNum: number) {
  queryParams.pageNum = pageNum
  getList()
}

function resetForm() {
  form.userId = undefined
  form.deptId = undefined
  form.userName = ''
  form.nickName = ''
  form.email = ''
  form.phonenumber = ''
  form.sex = '0'
  form.password = ''
  form.status = '0'
  form.remark = ''
  form.postIds = []
  form.roleIds = []
  formRef.value?.clearValidate()
}

async function handleAdd() {
  resetForm()
  if (!(await loadOptions())) {
    return
  }

  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

async function handleEdit(row: SysUser) {
  if (!row.userId) {
    ElMessage.error('用户ID不存在，无法编辑')
    return
  }

  resetForm()

  try {
    const [detailResponse, optionsLoaded] = await Promise.all([getUser(row.userId), loadOptions()])
    if (!optionsLoaded || !isSuccess(detailResponse)) {
      return
    }

    Object.assign(form, detailResponse.data || row)
    form.password = ''
    form.postIds = [...(detailResponse.data?.postIds || row.postIds || [])]
    form.roleIds = [...(detailResponse.data?.roleIds || row.roleIds || [])]
    dialogTitle.value = '编辑用户'
    dialogVisible.value = true
  } catch (error) {
    console.error('查询用户详情失败：', error)
  }
}

async function handleDetail(row: SysUser) {
  if (!row.userId) {
    ElMessage.error('用户ID不存在，无法查看详情')
    return
  }

  try {
    const response = await getUser(row.userId)
    if (!isSuccess(response)) {
      return
    }

    detailData.value = response.data || row
    detailVisible.value = true
  } catch (error) {
    console.error('查询用户详情失败：', error)
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    const existsResponse = await checkUserNameExists(form.userName || '', form.userId)
    if (!isSuccess(existsResponse)) {
      return
    }
    if (existsResponse.data) {
      ElMessage.warning('用户账号已存在')
      return
    }

    const payload: SysUser = {
      ...form,
      postIds: [...(form.postIds || [])],
      roleIds: [...(form.roleIds || [])],
    }
    if (form.userId) {
      delete payload.password
    }

    const response = form.userId ? await updateUser(payload) : await addUser(payload)
    if (!isSuccess(response)) {
      return
    }

    ElMessage.success(form.userId ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await getList()
  } catch (error) {
    console.error('保存用户失败：', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysUser) {
  if (!row.userId) {
    ElMessage.error('用户ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除用户【${row.userName}】吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await deleteUser(row.userId)
    if (!isSuccess(response)) {
      return
    }

    ElMessage.success('删除成功')
    if (userList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }
    await getList()
  } catch (error) {
    console.log('取消删除或删除请求失败：', error)
  }
}

onMounted(() => {
  getList()
  loadOptions()
})
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="用户账号">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户账号"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="用户昵称">
          <el-input
            v-model="queryParams.nickName"
            placeholder="请输入用户昵称"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="手机号码">
          <el-input
            v-model="queryParams.phonenumber"
            placeholder="请输入手机号码"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="所属部门">
          <el-tree-select
            v-model="queryParams.deptId"
            :data="deptOptions"
            :props="treeProps"
            node-key="deptId"
            check-strictly
            clearable
            :render-after-expand="false"
            placeholder="请选择部门"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 130px"
          >
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column prop="userId" label="ID" width="80" align="center" />
        <el-table-column prop="userName" label="用户账号" min-width="130" />
        <el-table-column prop="nickName" label="用户昵称" min-width="130" />
        <el-table-column prop="deptName" label="所属部门" min-width="150" show-overflow-tooltip />
        <el-table-column label="岗位" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ displayPostNames(row.postNames) }}</template>
        </el-table-column>
        <el-table-column label="角色" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ displayRoleNames(row.roleNames) }}</template>
        </el-table-column>
        <el-table-column prop="phonenumber" label="手机号码" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户账号" prop="userName" required>
              <el-input v-model="form.userName" placeholder="请输入用户账号" maxlength="64" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName" required>
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="64" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId" required>
              <el-tree-select
                v-model="form.deptId"
                :data="deptOptions"
                :props="treeProps"
                node-key="deptId"
                check-strictly
                default-expand-all
                :render-after-expand="false"
                placeholder="请选择所属部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属岗位">
              <el-select
                v-model="form.postIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择岗位"
                style="width: 100%"
              >
                <el-option
                  v-for="item in postOptions"
                  :key="item.postId"
                  :label="item.postName"
                  :value="item.postId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col v-if="!form.userId" :span="12">
            <el-form-item label="登录密码" prop="password" required>
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入登录密码"
                maxlength="64"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属角色">
              <el-select
                v-model="form.roleIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择角色"
                style="width: 100%"
              >
                <el-option
                  v-for="item in roleOptions"
                  :key="item.roleId"
                  :label="item.roleName"
                  :value="item.roleId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="手机号码">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="32" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.sex" style="width: 100%">
                <el-option label="未知" value="0" />
                <el-option label="男" value="1" />
                <el-option label="女" value="2" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="状态" prop="status" required>
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="用户详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ detailData.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ detailData.status === '0' ? '正常' : '停用' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户账号">{{
          detailData.userName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{
          detailData.nickName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{
          detailData.deptName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ displaySex(detailData.sex) }}</el-descriptions-item>
        <el-descriptions-item label="所属岗位" :span="2">
          {{ displayPostNames(detailData.postNames) }}
        </el-descriptions-item>
        <el-descriptions-item label="所属角色" :span="2">
          {{ displayRoleNames(detailData.roleNames) }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号码">{{
          detailData.phonenumber || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailData.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{
          detailData.remark || '-'
        }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}

.query-card,
.table-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
