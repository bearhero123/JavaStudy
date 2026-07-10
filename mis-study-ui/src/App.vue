<script setup lang="ts">
import { ref } from 'vue'
import ClientsView from '@/views/projectManagement/clients/index.vue'
import ProjectInfoView from '@/views/projectManagement/projectInfo/index.vue'

const activeModule = ref('clients')

function handleModuleSelect(index: string) {
  activeModule.value = index
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-aside" width="220px">
      <div class="app-title">项目管理</div>
      <el-menu :default-active="activeModule" class="app-menu" @select="handleModuleSelect">
        <el-menu-item index="clients">客户管理</el-menu-item>
        <el-menu-item index="projectInfo">项目基本信息</el-menu-item>
      </el-menu>
    </el-aside>

    <el-main class="app-main">
      <ClientsView v-if="activeModule === 'clients'" />
      <ProjectInfoView v-else />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f7fa;
}

.app-aside {
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
}

.app-title {
  height: 56px;
  padding: 0 20px;
  font-size: 18px;
  font-weight: 600;
  line-height: 56px;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.app-menu {
  border-right: 0;
}

.app-main {
  padding: 0;
}
</style>
