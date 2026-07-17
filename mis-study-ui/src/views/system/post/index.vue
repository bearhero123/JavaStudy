<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addPost,
  deletePost,
  getPost,
  listPost,
  updatePost,
  type PostQuery,
  type SysPost,
} from '@/api/system/post'

defineOptions({ name: 'SystemPostManagement' })

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const formRef = ref<FormInstance>()

const postList = ref<SysPost[]>([])
const total = ref(0)

const queryParams = reactive<PostQuery>({
  pageNum: 1,
  pageSize: 10,
  postCode: '',
  postName: '',
  status: '',
})

const form = reactive<SysPost>({
  postId: undefined,
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: '',
})

const rules = reactive<FormRules<SysPost>>({
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  postSort: [{ required: true, message: '请输入岗位排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择岗位状态', trigger: 'change' }],
})

function isSuccess(response: { code: number; msg: string }) {
  if (response.code === 200) {
    return true
  }

  ElMessage.error(response.msg || '操作失败')
  return false
}

async function getList() {
  loading.value = true

  try {
    const response = await listPost(queryParams)
    if (isSuccess(response)) {
      postList.value = response.rows || []
      total.value = response.total || 0
    }
  } catch (error) {
    console.error('查询岗位列表失败：', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.postCode = ''
  queryParams.postName = ''
  queryParams.status = ''
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
  form.postId = undefined
  form.postCode = ''
  form.postName = ''
  form.postSort = 0
  form.status = '0'
  form.remark = ''
  formRef.value?.clearValidate()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增岗位'
  dialogVisible.value = true
}

async function handleEdit(row: SysPost) {
  if (!row.postId) {
    ElMessage.error('岗位ID不存在，无法编辑')
    return
  }

  resetForm()

  try {
    const response = await getPost(row.postId)
    if (!isSuccess(response)) {
      return
    }

    Object.assign(form, response.data || row)
    dialogTitle.value = '编辑岗位'
    dialogVisible.value = true
  } catch (error) {
    console.error('查询岗位详情失败：', error)
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    const response = form.postId ? await updatePost(form) : await addPost(form)
    if (!isSuccess(response)) {
      return
    }

    ElMessage.success(form.postId ? '修改成功' : '新增成功')
    dialogVisible.value = false
    await getList()
  } catch (error) {
    console.error('保存岗位失败：', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: SysPost) {
  if (!row.postId) {
    ElMessage.error('岗位ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除岗位【${row.postName}】吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await deletePost(row.postId)
    if (!isSuccess(response)) {
      return
    }

    ElMessage.success('删除成功')
    if (postList.value.length === 1 && queryParams.pageNum > 1) {
      queryParams.pageNum -= 1
    }
    await getList()
  } catch (error) {
    console.log('取消删除或删除请求失败：', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="80px">
        <el-form-item label="岗位编码">
          <el-input
            v-model="queryParams.postCode"
            placeholder="请输入岗位编码"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="岗位名称">
          <el-input
            v-model="queryParams.postName"
            placeholder="请输入岗位名称"
            clearable
            style="width: 200px"
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
          <span>岗位管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="postList" border stripe style="width: 100%">
        <el-table-column prop="postId" label="ID" width="90" align="center" />
        <el-table-column prop="postCode" label="岗位编码" min-width="160" />
        <el-table-column prop="postName" label="岗位名称" min-width="180" />
        <el-table-column prop="postSort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />

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
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="岗位编码" prop="postCode" required>
          <el-input v-model="form.postCode" placeholder="请输入岗位编码" maxlength="64" />
        </el-form-item>

        <el-form-item label="岗位名称" prop="postName" required>
          <el-input v-model="form.postName" placeholder="请输入岗位名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="岗位排序" prop="postSort" required>
          <el-input-number v-model="form.postSort" :min="0" :max="9999" style="width: 100%" />
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
