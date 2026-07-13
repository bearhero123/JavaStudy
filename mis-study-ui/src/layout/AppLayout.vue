<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menuRoutes = computed(() =>
  router
    .getRoutes()
    .filter((item) => item.meta.menu === true && typeof item.meta.title === 'string')
    .sort((left, right) => Number(left.meta.order || 0) - Number(right.meta.order || 0)),
)
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-aside" width="220px">
      <div class="app-title">项目管理</div>
      <el-menu :default-active="route.path" class="app-menu" router>
        <el-menu-item v-for="item in menuRoutes" :key="item.path" :index="item.path">
          {{ item.meta.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-main class="app-main">
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f7fa;
}

.app-aside {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden auto;
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
  min-width: 0;
  padding: 0;
}
</style>
