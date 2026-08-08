<template>
  <van-nav-bar
    v-if="isShowNavBar"
    fixed
    placeholder
    :title="title"
    :left-arrow="showBack"
    @click-left="onClickLeft"
    @click-right="onClickRight"
  >
    <template #right>
      <svg-icon class="text-[18px]" :name="useDarkMode() ? 'light' : 'dark'" />
    </template>
  </van-nav-bar>
</template>
<script setup lang="ts">
import { useDarkMode, useToggleDarkMode } from "@/hooks/useToggleDarkMode";
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import tabBarRoutes from "@/router/tabBarRoutes";
import otherRoutes from "@/router/otherRoutes";
const route = useRoute();
const router = useRouter();
const onClickRight = () => {
  useToggleDarkMode();
};
const onClickLeft = () => {
  const state = history.state;
  if (state && state.position > 0) {
    router.back();
  } else {
    router.replace("/");
  }
};
const isShowNavBar = computed(() => {
  const showNavBarRoutePathList = [...tabBarRoutes, ...otherRoutes]
    .filter(item => item?.meta?.isShowNavBar)
    .map(item => item.path);
  return showNavBarRoutePathList.includes(route.path);
});
const showBack = computed(() => {
  const tabBarPaths = tabBarRoutes.map(item => item.path);
  return !tabBarPaths.includes(route.path);
});
const title = computed(() => {
  const tabBarPaths = tabBarRoutes.map(item => item.path);
  if (tabBarPaths.includes(route.path)) {
    return "桔巢";
  }
  return (route.meta?.title as string) || "";
});
</script>

<style scoped></style>
