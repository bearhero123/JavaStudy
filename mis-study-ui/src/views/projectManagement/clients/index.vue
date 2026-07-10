<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addClients,
  checkClientNameExists,
  deleteClients,
  getClients,
  listClients,
  updateClients,
  type ClientsInfo,
  type ClientsQuery,
} from '@/api/projectManagement/clients'

const levelOptions = [
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增客户')
const formRef = ref<FormInstance>()

const clientList = ref<ClientsInfo[]>([])
const total = ref(0)
const detailData = ref<ClientsInfo>({})

const queryParams = reactive<ClientsQuery>({
  pageNum: 1,
  pageSize: 10,
  clientName: '',
  clientAddress: '',
  contactName: '',
  contactPhone: '',
  clientLevel: '',
})

const form = reactive<ClientsInfo>({
  clientId: undefined,
  clientName: '',
  clientAddress: '',
  contactName: '',
  contactPosition: '',
  contactPhone: '',
  contactEmail: '',
  clientLevel: '',
  remark: '',
})

const rules = reactive<FormRules<ClientsInfo>>({
  clientName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  contactEmail: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: ['blur', 'change'],
    },
  ],
})

async function getList() {
  loading.value = true

  try {
    const res = await listClients(queryParams)
    clientList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('查询客户列表失败，请检查后端服务')
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
  queryParams.clientName = ''
  queryParams.clientAddress = ''
  queryParams.contactName = ''
  queryParams.contactPhone = ''
  queryParams.clientLevel = ''
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
  form.clientId = undefined
  form.clientName = ''
  form.clientAddress = ''
  form.contactName = ''
  form.contactPosition = ''
  form.contactPhone = ''
  form.contactEmail = ''
  form.clientLevel = ''
  form.remark = ''
  formRef.value?.clearValidate()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增客户'
  dialogVisible.value = true
}

async function handleEdit(row: ClientsInfo) {
  if (!row.clientId) {
    ElMessage.error('客户ID不存在，无法编辑')
    return
  }

  resetForm()

  try {
    const res = await getClients(row.clientId)
    Object.assign(form, res.data || row)
    dialogTitle.value = '编辑客户'
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('查询客户详情失败，请检查后端接口')
  }
}

async function handleDetail(row: ClientsInfo) {
  if (!row.clientId) {
    ElMessage.error('客户ID不存在，无法查看详情')
    return
  }

  try {
    const res = await getClients(row.clientId)
    detailData.value = res.data || row
    detailVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('查询客户详情失败，请检查后端接口')
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    const existsRes = await checkClientNameExists(form.clientName || '', form.clientId)

    if (existsRes.data) {
      ElMessage.warning('客户名称已存在')
      return
    }

    if (form.clientId) {
      await updateClients(form)
      ElMessage.success('修改成功')
    } else {
      await addClients(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存客户失败，请检查后端接口')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: ClientsInfo) {
  if (!row.clientId) {
    ElMessage.error('客户ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除客户【${row.clientName}】吗？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteClients(row.clientId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.log('取消删除或删除失败：', error)
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
        <el-form-item label="客户名称">
          <el-input
            v-model="queryParams.clientName"
            placeholder="请输入客户名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="客户地址">
          <el-input
            v-model="queryParams.clientAddress"
            placeholder="请输入客户地址"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="联系人">
          <el-input
            v-model="queryParams.contactName"
            placeholder="请输入联系人"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input
            v-model="queryParams.contactPhone"
            placeholder="请输入联系电话"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="客户级别">
          <el-select
            v-model="queryParams.clientLevel"
            placeholder="请选择级别"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
          <span>客户管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="clientList" border stripe style="width: 100%">
        <el-table-column prop="clientId" label="ID" width="80" align="center" />
        <el-table-column prop="clientName" label="客户名称" min-width="220" show-overflow-tooltip />
        <el-table-column
          prop="clientAddress"
          label="客户地址"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPosition" label="职务" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="contactEmail" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="clientLevel" label="客户级别" width="110" align="center" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" width="210" fixed="right" align="center">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="clientName" required>
              <el-input v-model="form.clientName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="客户级别">
              <el-select
                v-model="form.clientLevel"
                placeholder="请选择客户级别"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="item in levelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="客户地址">
              <el-input v-model="form.clientAddress" placeholder="请输入客户地址" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="联系人职务">
              <el-input v-model="form.contactPosition" placeholder="请输入联系人职务" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="客户详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户ID">{{ detailData.clientId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户级别">{{
          detailData.clientLevel || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="客户名称" :span="2">
          {{ detailData.clientName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="客户地址" :span="2">
          {{ detailData.clientAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{
          detailData.contactName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="联系人职务">
          {{ detailData.contactPosition || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{
          detailData.contactPhone || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{
          detailData.contactEmail || '-'
        }}</el-descriptions-item>
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

.query-card {
  margin-bottom: 16px;
}

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
