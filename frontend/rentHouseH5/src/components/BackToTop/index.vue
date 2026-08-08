<template>
  <Transition name="back-to-top">
    <div
      v-show="visible"
      class="back-to-top"
      :class="{ dark: isDark }"
      @click="scrollToTop"
    >
      <van-icon name="arrow-up" />
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useDarkMode } from "@/hooks/useToggleDarkMode";

const isDark = computed(() => useDarkMode());
const visible = ref(false);
const THRESHOLD = 300;

function onScroll() {
  visible.value = window.scrollY > THRESHOLD;
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}

onMounted(() => {
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();
});

onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
});
</script>

<style scoped lang="less">
.back-to-top {
  position: fixed;
  right: 16px;
  bottom: 96px;
  z-index: 998;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 2px 12px rgba(0, 0, 0, 0.1),
    0 1px 3px rgba(0, 0, 0, 0.06);
  font-size: 20px;
  color: #666;
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.9);
    background: rgba(255, 255, 255, 0.85);
  }
}

.dark .back-to-top {
  background: rgba(40, 40, 40, 0.75);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 2px 12px rgba(0, 0, 0, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.2);
  color: #aaa;

  &:active {
    background: rgba(50, 50, 50, 0.85);
  }
}

.back-to-top-enter-active,
.back-to-top-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.back-to-top-enter-from,
.back-to-top-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
