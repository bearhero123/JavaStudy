<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addConfig,
  deleteConfig,
  getConfig,
  listConfig,
  updateConfig,
  type ConfigQuery,
  type SysConfig,
} from '@/api/system/config'

defineOptions({ name: 'SystemConfigManagement' })

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const configList = ref<SysConfig[]>([])
const total = ref(0)
const queryParams = reactive<ConfigQuery>({ pageNum: 1, pageSize: 10, configName: '', configKey: '', configType: '' })
const form = reactive<SysConfig>({ configId: undefined, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' })
const rules = reactive<FormRules<SysConfig>>({
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择参数类型', trigger: 'change' }],
})

function isSuccess(response: { code: number; msg: string }) { if (response.code === 200) return true; ElMessage.error(response.msg || '操作失败'); return false }
function formatDate(value?: string) { return value ? value.replace('T', ' ').slice(0, 19) : '-' }

async function getList() {
  loading.value = true
  try { const response = await listConfig(queryParams); if (isSuccess(response)) { configList.value = response.rows || []; total.value = response.total || 0 } }
  finally { loading.value = false }
}

function resetForm() { Object.assign(form, { configId: undefined, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' }); formRef.value?.clearValidate() }
async function handleAdd() { resetForm(); dialogVisible.value = true }
async function handleEdit(row: SysConfig) { if (!row.configId) return; resetForm(); const response = await getConfig(row.configId); if (!isSuccess(response)) return; Object.assign(form, response.data || row); dialogVisible.value = true }

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try { const response = form.configId ? await updateConfig({ ...form }) : await addConfig({ ...form }); if (!isSuccess(response)) return; ElMessage.success(form.configId ? '修改成功' : '新增成功'); dialogVisible.value = false; await getList() }
  finally { submitLoading.value = false }
}

async function handleDelete(row: SysConfig) {
  if (!row.configId) return
  try { await ElMessageBox.confirm(`确认删除参数【${row.configName}】吗？`, '删除确认', { type: 'warning' }); const response = await deleteConfig(row.configId); if (!isSuccess(response)) return; ElMessage.success('删除成功'); if (configList.value.length === 1 && queryParams.pageNum > 1) queryParams.pageNum -= 1; await getList() }
  catch (error) { console.log('取消删除或删除请求失败：', error) }
}

function resetQuery() { queryParams.pageNum = 1; queryParams.configName = ''; queryParams.configKey = ''; queryParams.configType = ''; getList() }
onMounted(getList)
</script>

<template>
  <div class="page-container">
    <el-card class="query-card"><el-form :model="queryParams" inline label-width="80px"><el-form-item label="参数名称"><el-input v-model="queryParams.configName" clearable /></el-form-item><el-form-item label="参数键名"><el-input v-model="queryParams.configKey" clearable /></el-form-item><el-form-item label="参数类型"><el-select v-model="queryParams.configType" clearable style="width: 130px"><el-option label="系统内置" value="Y" /><el-option label="普通参数" value="N" /></el-select></el-form-item><el-form-item><el-button type="primary" @click="queryParams.pageNum = 1; getList()">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item></el-form></el-card>
    <el-card><template #header><div class="card-header"><span>参数设置</span><el-button type="primary" @click="handleAdd">新增</el-button></div></template>
      <el-table v-loading="loading" :data="configList" border stripe><el-table-column prop="configId" label="ID" width="80" align="center" /><el-table-column prop="configName" label="参数名称" min-width="170" /><el-table-column prop="configKey" label="参数键名" min-width="210" /><el-table-column prop="configValue" label="参数键值" min-width="180" show-overflow-tooltip /><el-table-column label="类型" width="100" align="center"><template #default="{ row }">{{ row.configType === 'Y' ? '系统内置' : '普通参数' }}</template></el-table-column><el-table-column label="创建时间" width="180"><template #default="{ row }">{{ formatDate(row.createTime) }}</template></el-table-column><el-table-column label="操作" width="150" fixed="right" align="center"><template #default="{ row }"><el-button link type="primary" @click="handleEdit(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row)">删除</el-button></template></el-table-column></el-table>
      <div class="pagination-wrapper"><el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[5,10,20,50]" layout="total, sizes, prev, pager, next, jumper" @size-change="queryParams.pageNum = 1; getList()" @current-change="getList" /></div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="form.configId ? '编辑参数' : '新增参数'" width="680px"><el-form ref="formRef" :model="form" :rules="rules" label-width="100px"><el-form-item label="参数名称" prop="configName"><el-input v-model="form.configName" maxlength="100" /></el-form-item><el-form-item label="参数键名" prop="configKey"><el-input v-model="form.configKey" maxlength="100" /></el-form-item><el-form-item label="参数键值" prop="configValue"><el-input v-model="form.configValue" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item><el-form-item label="参数类型" prop="configType"><el-radio-group v-model="form.configType"><el-radio value="Y">系统内置</el-radio><el-radio value="N">普通参数</el-radio></el-radio-group></el-form-item><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" /></el-form-item></el-form><template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button></template></el-dialog>
  </div>
</template>

<style scoped>
.page-container { padding: 20px; }
.query-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
