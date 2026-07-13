<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, TagProps } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addVendor,
  checkVendorNameExists,
  deleteVendor,
  getVendor,
  listVendor,
  updateVendor,
  type VendorInfo,
  type VendorQuery,
} from '@/api/projectManagement/vendor'

defineOptions({ name: 'VendorManagement' })

const ratingOptions = [
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增供应商')
const formRef = ref<FormInstance>()

const vendorList = ref<VendorInfo[]>([])
const total = ref(0)
const detailData = ref<VendorInfo>({})

const queryParams = reactive<VendorQuery>({
  pageNum: 1,
  pageSize: 10,
  vendorName: '',
  contactName: '',
  contactPhone: '',
  vendorRatings: '',
  vendorAddr: '',
})

const form = reactive<VendorInfo>({
  vendorId: undefined,
  vendorName: '',
  contactName: '',
  contactPosition: '',
  contactPhone: '',
  contactEmail: '',
  vendorRatings: '',
  vendorAddr: '',
  remark: '',
})

const rules = reactive<FormRules<VendorInfo>>({
  vendorName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactEmail: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: ['blur', 'change'],
    },
  ],
})

function ensureSuccess(result: { code: number; msg: string }, fallbackMessage: string) {
  if (result.code !== 200) {
    throw new Error(result.msg || fallbackMessage)
  }
}

function errorMessage(error: unknown, fallbackMessage: string) {
  return error instanceof Error && error.message ? error.message : fallbackMessage
}

function ratingTagType(rating?: string): TagProps['type'] {
  if (rating === 'A') {
    return 'success'
  }

  if (rating === 'B') {
    return 'warning'
  }

  return 'info'
}

async function getList() {
  loading.value = true

  try {
    const res = await listVendor(queryParams)
    ensureSuccess(res, '查询供应商列表失败')
    vendorList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error(errorMessage(error, '查询供应商列表失败，请检查后端服务'))
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
  queryParams.vendorName = ''
  queryParams.contactName = ''
  queryParams.contactPhone = ''
  queryParams.vendorRatings = ''
  queryParams.vendorAddr = ''
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
  form.vendorId = undefined
  form.vendorName = ''
  form.contactName = ''
  form.contactPosition = ''
  form.contactPhone = ''
  form.contactEmail = ''
  form.vendorRatings = ''
  form.vendorAddr = ''
  form.remark = ''
  nextTick(() => formRef.value?.clearValidate())
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增供应商'
  dialogVisible.value = true
}

async function handleEdit(row: VendorInfo) {
  if (row.vendorId == null) {
    ElMessage.error('供应商ID不存在，无法编辑')
    return
  }

  resetForm()

  try {
    const res = await getVendor(row.vendorId)
    ensureSuccess(res, '查询供应商详情失败')
    Object.assign(form, res.data || row)
    dialogTitle.value = '编辑供应商'
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error(errorMessage(error, '查询供应商详情失败，请检查后端接口'))
  }
}

async function handleDetail(row: VendorInfo) {
  if (row.vendorId == null) {
    ElMessage.error('供应商ID不存在，无法查看详情')
    return
  }

  try {
    const res = await getVendor(row.vendorId)
    ensureSuccess(res, '查询供应商详情失败')
    detailData.value = res.data || row
    detailVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error(errorMessage(error, '查询供应商详情失败，请检查后端接口'))
  }
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    const existsRes = await checkVendorNameExists(form.vendorName || '', form.vendorId)
    ensureSuccess(existsRes, '校验供应商名称失败')

    if (existsRes.data) {
      ElMessage.warning('供应商名称已存在')
      return
    }

    if (form.vendorId != null) {
      const res = await updateVendor(form)
      ensureSuccess(res, '修改供应商失败')
      ElMessage.success('修改成功')
    } else {
      const res = await addVendor(form)
      ensureSuccess(res, '新增供应商失败')
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await getList()
  } catch (error) {
    console.error(error)
    ElMessage.error(errorMessage(error, '保存供应商失败，请检查后端接口'))
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: VendorInfo) {
  if (row.vendorId == null) {
    ElMessage.error('供应商ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除供应商【${row.vendorName || row.vendorId}】吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const res = await deleteVendor(row.vendorId)
    ensureSuccess(res, '删除供应商失败')
    ElMessage.success('删除成功')
    await getList()

    if (queryParams.pageNum > 1 && vendorList.value.length === 0) {
      queryParams.pageNum -= 1
      await getList()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
      ElMessage.error(errorMessage(error, '删除供应商失败，请检查后端接口'))
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-container">
    <el-card class="query-card">
      <el-form :model="queryParams" inline label-width="90px">
        <el-form-item label="供应商名称">
          <el-input
            v-model="queryParams.vendorName"
            placeholder="请输入供应商名称"
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

        <el-form-item label="供应商评级">
          <el-select
            v-model="queryParams.vendorRatings"
            placeholder="请选择评级"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in ratingOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="供应商地址">
          <el-input
            v-model="queryParams.vendorAddr"
            placeholder="请输入供应商地址"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
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
          <span>供应商管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="vendorList" border stripe style="width: 100%">
        <el-table-column prop="vendorId" label="ID" width="80" align="center" />
        <el-table-column
          prop="vendorName"
          label="供应商名称"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPosition" label="职务" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="contactEmail" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="vendorRatings" label="评级" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="row.vendorRatings"
              :type="ratingTagType(row.vendorRatings)"
              effect="plain"
            >
              {{ row.vendorRatings }}级
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="vendorAddr"
          label="供应商地址"
          min-width="240"
          show-overflow-tooltip
        />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="供应商名称" prop="vendorName" required>
              <el-input v-model="form.vendorName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="供应商评级">
              <el-select
                v-model="form.vendorRatings"
                placeholder="请选择供应商评级"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="item in ratingOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="联系人" prop="contactName" required>
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="联系人职务">
              <el-input v-model="form.contactPosition" placeholder="请输入联系人职务" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="供应商地址">
              <el-input v-model="form.vendorAddr" placeholder="请输入供应商地址" />
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

    <el-dialog v-model="detailVisible" title="供应商详情" width="760px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商ID">{{
          detailData.vendorId ?? '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="供应商评级">
          <el-tag
            v-if="detailData.vendorRatings"
            :type="ratingTagType(detailData.vendorRatings)"
            effect="plain"
          >
            {{ detailData.vendorRatings }}级
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="供应商名称" :span="2">
          {{ detailData.vendorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{
          detailData.contactName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="联系人职务">
          {{ detailData.contactPosition || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ detailData.contactPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">{{
          detailData.contactEmail || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="供应商地址" :span="2">
          {{ detailData.vendorAddr || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{
          detailData.remark || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          detailData.createTime || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="更新人">{{ detailData.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
          detailData.updateTime || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="记录状态" :span="2">
          {{ detailData.delFlag === '2' ? '已删除' : '正常' }}
        </el-descriptions-item>
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
