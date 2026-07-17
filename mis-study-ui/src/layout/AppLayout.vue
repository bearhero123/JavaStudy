<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const menuGroups = computed(() => {
  const groups = new Map<
    string,
    {
      title: string
      order: number
      routes: ReturnType<typeof router.getRoutes>
    }
  >()

  for (const item of router.getRoutes()) {
    if (item.meta.menu !== true || typeof item.meta.title !== 'string') {
      continue
    }

    const groupTitle = typeof item.meta.group === 'string' ? item.meta.group : '其他'
    const groupOrder = Number(item.meta.groupOrder || 999)
    const group = groups.get(groupTitle)

    if (group) {
      group.routes.push(item)
    } else {
      groups.set(groupTitle, {
        title: groupTitle,
        order: groupOrder,
        routes: [item],
      })
    }
  }

  return Array.from(groups.values())
    .map((group) => ({
      ...group,
      routes: group.routes.sort(
        (left, right) => Number(left.meta.order || 0) - Number(right.meta.order || 0),
      ),
    }))
    .sort((left, right) => left.order - right.order)
})

const defaultOpenGroups = computed(() => menuGroups.value.map((group) => group.title))
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-aside" width="220px">
      <div class="app-title">管理信息系统</div>
      <el-menu
        :default-active="route.path"
        :default-openeds="defaultOpenGroups"
        class="app-menu"
        router
      >
        <el-sub-menu v-for="group in menuGroups" :key="group.title" :index="group.title">
          <template #title>
            <span>{{ group.title }}</span>
          </template>

          <el-menu-item v-for="item in group.routes" :key="item.path" :index="item.path">
            {{ item.meta.title }}
          </el-menu-item>
        </el-sub-menu>
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

.app-menu :deep(.el-sub-menu__title) {
  font-weight: 600;
  color: #303133;
}

.app-main {
  min-width: 0;
  padding: 0;
}
</style>
