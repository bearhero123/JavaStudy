<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDept,
  deleteDept,
  getDept,
  getDeptTreeOptions,
  listDept,
  updateDept,
  type DeptQuery,
  type SysDept,
} from '@/api/system/dept'

defineOptions({ name: 'SystemDeptManagement' })

interface DeptForm {
  deptId?: number
  parentId?: number
  deptName?: string
  orderNum?: number
  status?: string
  remark?: string
}

const treeProps = {
  value: 'deptId',
  label: 'deptName',
  children: 'children',
}

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref<FormInstance>()
const tableRef = ref()

const deptList = ref<SysDept[]>([])
const parentOptions = ref<SysDept[]>([])

const queryParams = reactive<DeptQuery>({
  deptName: '',
  status: '',
})

const form = reactive<DeptForm>({
  deptId: undefined,
  parentId: 0,
  deptName: '',
  orderNum: 0,
  status: '0',
  remark: '',
})

const rules = reactive<FormRules<DeptForm>>({
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入显示顺序', trigger: 'change' }],
  status: [{ required: true, message: '请选择部门状态', trigger: 'change' }],
})

function isSuccess(response: { code: number; msg: string }) {
  if (response.code === 200) {
    return true
  }

  ElMessage.error(response.msg || '操作失败')
  return false
}

function formatDate(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

async function getList() {
  loading.value = true

  try {
    const response = await listDept(queryParams)
    if (isSuccess(response)) {
      deptList.value = response.data || []
    }
  } catch (error) {
    console.error('查询部门列表失败：', error)
  } finally {
    loading.value = false
  }
}

async function loadParentOptions(excludeDeptId?: number) {
  try {
    const response = await getDeptTreeOptions(excludeDeptId)
    if (!isSuccess(response)) {
      return false
    }

    parentOptions.value = [
      {
        deptId: 0,
        deptName: '无（作为根部门）',
        children: response.data || [],
      },
    ]
    return true
  } catch (error) {
    console.error('查询上级部门选项失败：', error)
    return false
  }
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryParams.deptName = ''
  queryParams.status = ''
  getList()
}

function resetForm() {
  form.deptId = undefined
  form.parentId = 0
  form.deptName = ''
  form.orderNum = 0
  form.status = '0'
  form.remark = ''
  formRef.value?.clearValidate()
}

async function handleAdd(parent?: SysDept) {
  resetForm()
  form.parentId = parent?.deptId || 0

  if (!(await loadParentOptions())) {
    return
  }

  dialogTitle.value = parent ? `新增【${parent.deptName}】的子部门` : '新增根部门'
  dialogVisible.value = true
}

async function handleEdit(row: SysDept) {
  if (!row.deptId) {
    ElMessage.error('部门ID不存在，无法编辑')
    return
  }

  resetForm()

  try {
    const [detailResponse, optionsLoaded] = await Promise.all([
      getDept(row.deptId),
      loadParentOptions(row.deptId),
    ])

    if (!optionsLoaded || !isSuccess(detailResponse)) {
      return
    }

    Object.assign(form, detailResponse.data || row)
    form.parentId = form.parentId || 0
    dialogTitle.value = '编辑部门'
    dialogVisible.value = true
  } catch (error) {
    console.error('查询部门详情失败：', error)
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    const payload: SysDept = {
      ...form,
      parentId: form.parentId || 0,
    }
    const response = form.deptId ? await updateDept(payload) : await addDept(payload)

    if (!isSuccess(response)) {
      return
    }

    ElMessage.success(form.deptId ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await getList()
  } catch (error) {
    console.error('保存部门失败：', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysDept) {
  if (!row.deptId) {
    ElMessage.error('部门ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除部门【${row.deptName}】吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await deleteDept(row.deptId)
    if (!isSuccess(response)) {
      return
    }

    ElMessage.success('删除成功')
    await getList()
  } catch (error) {
    console.log('取消删除或删除请求失败：', error)
  }
}

function setAllExpanded(expanded: boolean) {
  const toggleNodes = (nodes: SysDept[]) => {
    for (const node of nodes) {
      tableRef.value?.toggleRowExpansion(node, expanded)
      if (node.children?.length) {
        toggleNodes(node.children)
      }
    }
  }

  nextTick(() => toggleNodes(deptList.value))
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="部门名称">
          <el-input
            v-model="queryParams.deptName"
            placeholder="请输入部门名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
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
          <span>部门管理</span>
          <div class="header-actions">
            <el-button @click="setAllExpanded(true)">全部展开</el-button>
            <el-button @click="setAllExpanded(false)">全部折叠</el-button>
            <el-button type="primary" @click="handleAdd()">新增根部门</el-button>
          </div>
        </div>
      </template>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="deptList"
        row-key="deptId"
        border
        stripe
        :tree-props="{ children: 'children' }"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="240" />
        <el-table-column prop="orderNum" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">新增子部门</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="treeProps"
            node-key="deptId"
            check-strictly
            default-expand-all
            :render-after-expand="false"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="部门名称" prop="deptName" required>
          <el-input v-model="form.deptName" placeholder="请输入部门名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="显示顺序" prop="orderNum" required>
          <el-input-number v-model="form.orderNum" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status" required>
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
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

.card-header,
.header-actions {
  display: flex;
  align-items: center;
}

.card-header {
  justify-content: space-between;
}

.header-actions {
  gap: 8px;
}
</style>
