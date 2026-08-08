<template>
  <div v-show="isShowTabBar" class="tabbar-wrapper" :class="{ dark: isDark }">
    <div class="tabbar-glass">
      <div
        v-for="(item, index) in tabBarData"
        :key="index"
        class="tabbar-item"
        :class="{ active: active === index }"
        @click="handleClick(index, item)"
      >
        <div class="tabbar-item-icon">
          <van-icon :name="item.icon" />
        </div>
        <div class="tabbar-item-title">{{ item.title }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import tabBarRoutes from "@/router/tabBarRoutes";
import { useRoute, useRouter } from "vue-router";
import { useDarkMode } from "@/hooks/useToggleDarkMode";
const route = useRoute();
const router = useRouter();
const active = ref(0);

const isDark = computed(() => useDarkMode());

const tabBarData = computed(() => {
  return tabBarRoutes.map(item => {
    return {
      icon: item.meta?.icon,
      title: item.meta?.title,
      path: item.path
    };
  });
});

const isShowTabBar = computed(() => {
  return tabBarRoutes.some(item => item.path === route.path);
});

// 监听路由变化，同步 tabbar 高亮
watch(
  () => route.path,
  (path) => {
    const index = tabBarRoutes.findIndex(item => item.path === path);
    if (index !== -1) {
      active.value = index;
    }
  },
  { immediate: true }
);

function handleClick(index: number, item: any) {
  active.value = index;
  router.push(item.path);
}
</script>

<style scoped lang="less">
.tabbar-wrapper {
  position: fixed;
  bottom: 12px;
  left: 12px;
  right: 12px;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
}

.tabbar-glass {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 60px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 4px 30px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

// 暗色模式
.dark .tabbar-glass {
  background: rgba(30, 30, 30, 0.75);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 4px 30px rgba(0, 0, 0, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &::before {
    content: "";
    position: absolute;
    top: 4px;
    left: 50%;
    transform: translateX(-50%) scale(0);
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(39, 121, 65, 0.12), rgba(52, 199, 89, 0.08));
    transition: transform 0.3s ease;
  }

  &.active::before {
    transform: translateX(-50%) scale(1);
  }
}

.dark .tabbar-item::before {
  background: linear-gradient(135deg, rgba(39, 121, 65, 0.2), rgba(52, 199, 89, 0.12));
}

.tabbar-item-icon {
  font-size: 22px;
  color: #999;
  transition: all 0.25s ease;
  position: relative;
  z-index: 1;

  .active & {
    color: #277941;
    transform: scale(1.1);
  }
}

.dark .tabbar-item-icon {
  color: #666;

  .active & {
    color: #34c759;
  }
}

.tabbar-item-title {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
  transition: all 0.25s ease;
  position: relative;
  z-index: 1;

  .active & {
    color: #277941;
    font-weight: 600;
  }
}

.dark .tabbar-item-title {
  color: #666;

  .active & {
    color: #34c759;
  }
}
</style>
