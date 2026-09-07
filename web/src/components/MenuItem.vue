<template>
  <el-sub-menu v-if="menu.menuType === 1 && menu.children && menu.children.length" :index="menu.id + ''">
    <template #title>
      <span>{{ menu.icon }} {{ menu.menuName }}</span>
    </template>
    <MenuItem v-for="child in menu.children" :key="child.id" :menu="child" />
  </el-sub-menu>

  <el-menu-item v-else-if="menu.menuType === 2 && menu.routePath" :index="menu.routePath">
    <span>{{ menu.icon }} {{ menu.menuName }}</span>
  </el-menu-item>
</template>

<script setup>
defineProps({
  menu: { type: Object, required: true }
})
</script>
