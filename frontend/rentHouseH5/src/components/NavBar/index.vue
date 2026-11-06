<template>
  <van-nav-bar
    v-if="isShowNavBar"
    class="liquid-nav"
    :class="{ dark: isDark }"
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
    return "栖巢";
  }
  return (route.meta?.title as string) || "";
});
</script>

<style scoped lang="less">
// 吸顶毛玻璃导航条：占文档流不遮挡内容，滚动时吸附顶部、内容从玻璃后穿过。
// 玻璃视觉（半透明底+blur+分割线）由全局样式统一提供，此处仅处理吸顶与层级
.liquid-nav {
  position: sticky;
  top: 0;
  z-index: 998;
  --van-nav-bar-background: transparent;
  --van-nav-bar-height: 46px;

  :deep(.van-nav-bar__title) {
    color: #1f1f1f;
    font-weight: 600;
  }

  :deep(.van-nav-bar__arrow) {
    color: #277941;
  }

  :deep(.van-nav-bar__text) {
    color: #1f1f1f;
  }
}

// 暗色模式文字/箭头配色
.liquid-nav.dark {
  :deep(.van-nav-bar__title),
  :deep(.van-nav-bar__text) {
    color: #eee;
  }

  :deep(.van-nav-bar__arrow) {
    color: #34c759;
  }
}
</style>
