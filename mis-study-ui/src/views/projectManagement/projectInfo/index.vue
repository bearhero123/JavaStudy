<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProjectInfo,
  deleteProjectInfo,
  listProjectInfo,
  updateProjectInfo,
  type ProjectInfo,
  type ProjectInfoQuery,
} from '@/api/projectManagement/projectInfo'

defineOptions({ name: 'ProjectInfoManagement' })

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')

const projectList = ref<ProjectInfo[]>([])
const total = ref(0)

const queryParams = reactive<ProjectInfoQuery>({
  pageNum: 1,
  pageSize: 10,
  projectNumber: '',
  projectNameCn: '',
  projectType: '',
  clientName: '',
})

const form = reactive<ProjectInfo>({
  projectId: undefined,
  projectNumber: '',
  projectAbbreviation: '',
  projectNameCn: '',
  projectNameEn: '',
  projectType: '',
  projectStatus: '',
  pmName: '',
  clientName: '',
  projectScale: undefined,
  currency: 'CNY',
  businessArea: '',
  contractStatus: '',
  startDate: '',
  endDate: '',
  remark: '',
})

async function getList() {
  loading.value = true

  try {
    const res = await listProjectInfo(queryParams)
    projectList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('查询项目列表失败，请检查后端服务')
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
  queryParams.projectNumber = ''
  queryParams.projectNameCn = ''
  queryParams.projectType = ''
  queryParams.clientName = ''
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
  form.projectId = undefined
  form.projectNumber = ''
  form.projectAbbreviation = ''
  form.projectNameCn = ''
  form.projectNameEn = ''
  form.projectType = ''
  form.projectStatus = ''
  form.pmName = ''
  form.clientName = ''
  form.projectScale = undefined
  form.currency = 'CNY'
  form.businessArea = ''
  form.contractStatus = ''
  form.startDate = ''
  form.endDate = ''
  form.remark = ''
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增项目'
  dialogVisible.value = true
}

function handleEdit(row: ProjectInfo) {
  resetForm()

  Object.assign(form, {
    projectId: row.projectId,
    projectNumber: row.projectNumber,
    projectAbbreviation: row.projectAbbreviation,
    projectNameCn: row.projectNameCn,
    projectNameEn: row.projectNameEn,
    projectType: row.projectType,
    projectStatus: row.projectStatus,
    pmName: row.pmName,
    clientName: row.clientName,
    projectScale: row.projectScale,
    currency: row.currency || 'CNY',
    businessArea: row.businessArea,
    contractStatus: row.contractStatus,
    startDate: row.startDate,
    endDate: row.endDate,
    remark: row.remark,
  })

  dialogTitle.value = '编辑项目'
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.projectNumber) {
    ElMessage.warning('请输入项目编号')
    return
  }

  if (!form.projectNameCn) {
    ElMessage.warning('请输入项目名称')
    return
  }

  if (!form.projectType) {
    ElMessage.warning('请选择项目类型')
    return
  }

  submitLoading.value = true

  try {
    if (form.projectId) {
      await updateProjectInfo(form)
      ElMessage.success('修改成功')
    } else {
      await addProjectInfo(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败，请检查后端接口')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: ProjectInfo) {
  if (!row.projectId) {
    ElMessage.error('项目ID不存在，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认删除项目【${row.projectNameCn || row.projectNumber}】吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await deleteProjectInfo(row.projectId)
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
        <el-form-item label="项目编号">
          <el-input
            v-model="queryParams.projectNumber"
            placeholder="请输入项目编号"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="项目名称">
          <el-input
            v-model="queryParams.projectNameCn"
            placeholder="请输入项目名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="项目类型">
          <el-select
            v-model="queryParams.projectType"
            placeholder="请选择项目类型"
            clearable
            style="width: 180px"
          >
            <el-option label="内部项目" value="内部项目" />
            <el-option label="外部项目" value="外部项目" />
          </el-select>
        </el-form-item>

        <el-form-item label="客户名称">
          <el-input
            v-model="queryParams.clientName"
            placeholder="请输入客户名称"
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
          <span>项目基本信息管理</span>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="projectList" border stripe style="width: 100%">
        <el-table-column prop="projectId" label="ID" width="80" align="center" />
        <el-table-column prop="projectNumber" label="项目编号" width="140" />
        <el-table-column
          prop="projectNameCn"
          label="项目名称"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column prop="projectType" label="项目类型" width="120" />
        <el-table-column prop="projectStatus" label="项目状态" width="120" />
        <el-table-column prop="clientName" label="客户名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="pmName" label="项目经理" width="120" />
        <el-table-column prop="projectScale" label="项目规模" width="120" align="right" />
        <el-table-column prop="currency" label="币种" width="90" align="center" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />

        <el-table-column label="操作" width="160" fixed="right" align="center">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目编号" required>
              <el-input v-model="form.projectNumber" placeholder="请输入项目编号" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目简称">
              <el-input v-model="form.projectAbbreviation" placeholder="请输入项目简称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目名称" required>
              <el-input v-model="form.projectNameCn" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="英文名称">
              <el-input v-model="form.projectNameEn" placeholder="请输入英文名称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目类型" required>
              <el-select
                v-model="form.projectType"
                placeholder="请选择项目类型"
                style="width: 100%"
              >
                <el-option label="内部项目" value="内部项目" />
                <el-option label="外部项目" value="外部项目" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目状态">
              <el-select
                v-model="form.projectStatus"
                placeholder="请选择项目状态"
                style="width: 100%"
              >
                <el-option label="进行中" value="进行中" />
                <el-option label="已验收" value="已验收" />
                <el-option label="暂停" value="暂停" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="客户名称">
              <el-input v-model="form.clientName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目经理">
              <el-input v-model="form.pmName" placeholder="请输入项目经理" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="项目规模">
              <el-input-number v-model="form.projectScale" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="币种">
              <el-select v-model="form.currency" placeholder="请选择币种" style="width: 100%">
                <el-option label="CNY" value="CNY" />
                <el-option label="USD" value="USD" />
                <el-option label="HKD" value="HKD" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="业务领域">
              <el-input v-model="form.businessArea" placeholder="请输入业务领域" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="合同状态">
              <el-select
                v-model="form.contractStatus"
                placeholder="请选择合同状态"
                style="width: 100%"
              >
                <el-option label="已签署" value="已签署" />
                <el-option label="未签署" value="未签署" />
                <el-option label="洽谈中" value="洽谈中" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束日期"
                style="width: 100%"
              />
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
        <el-button type="primary" :loading="submitLoading" @click="submitForm"> 确定 </el-button>
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
