<script setup lang="ts">
import tabbar from "@/components/Tabbar/index.vue";
import NavBar from "@/components/NavBar/index.vue";
import BackToTop from "@/components/BackToTop/index.vue";
import { useCachedViewStoreHook } from "@/store/modules/cachedView";
import { useDarkMode } from "@/hooks/useToggleDarkMode";
import { computed } from "vue";

const cachedViews = computed(() => {
  return useCachedViewStoreHook().cachedViewList;
});
</script>

<template>
  <div class="app-wrapper">
    <van-config-provider :theme="useDarkMode() ? 'dark' : 'light'">
      <nav-bar />
      <router-view v-slot="{ Component }">
        <!-- 注意：不要加 mode="out-in"！out-in + keep-alive + 懒加载路由
             是 Vue 3 已知白屏 bug（vuejs/core#5970/#7121），表现为跳转详情页
             再返回时页面空白且无报错。改用纯入场动画（离开瞬时结束）。 -->
        <transition name="page-fade">
          <keep-alive :include="cachedViews">
            <component :is="Component" />
          </keep-alive>
        </transition>
      </router-view>
      <BackToTop />
      <tabbar />
    </van-config-provider>
  </div>
</template>

<style lang="less" scoped>
@import "@/styles/mixin.less";

.app-wrapper {
  .clearfix();
  position: relative;
  height: 100%;
  width: 100%;
}
</style>

<style lang="less">
// 全局路由切换动效（非 scoped，transition class 挂在页面根元素上）
// 只做入场动画：leave 时长为 0（旧页面立即移除），等效 out-in 的视觉
// 顺序，但没有 out-in 与 keep-alive 组合的白屏死锁问题
.page-fade-enter-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-active {
  transition: opacity 0s;
}
</style>
