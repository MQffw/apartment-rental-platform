<template>
  <div v-show="isShowTabBar" class="tabbar-wrapper" :class="{ dark: isDark }">
    <div
      ref="barEl"
      class="tabbar-glass"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
    >
      <!-- 液态气泡：框住整个 tab（图标+文字），跟手拖动，松手惯性滑行 -->
      <div class="goo-blob" :class="{ dragging: isDragging }" :style="blobStyle"></div>

      <div
        v-for="(item, index) in tabBarData"
        :key="index"
        :ref="(el: any) => setItemRef(el, index)"
        :data-index="index"
        class="tabbar-item"
        :class="{ active: active === index, squish: squishIndex === index }"
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
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import tabBarRoutes from "@/router/tabBarRoutes";
import { useRoute, useRouter } from "vue-router";
import { useDarkMode } from "@/hooks/useToggleDarkMode";
const route = useRoute();
const router = useRouter();
const active = ref(0);
const isDark = computed(() => useDarkMode());

// ==================== 几何测量 ====================
const barEl = ref<HTMLElement>();
const itemEls: (HTMLElement | null)[] = [];
const setItemRef = (el: any, index: number) => {
  itemEls[index] = el;
};
const itemW = ref(70); // 单个 tab 宽度
const firstCenter = ref(35); // 第一个 tab 中心（相对 bar）
const blobW = ref(62); // 气泡宽度

function measure() {
  const first = itemEls[0];
  if (!barEl.value || !first) return;
  itemW.value = first.offsetWidth;
  blobW.value = first.offsetWidth - 8;
  firstCenter.value = first.offsetLeft + first.offsetWidth / 2;
}

let resizeObserver: ResizeObserver | null = null;

// ==================== 气泡状态 ====================
const blobIdx = ref(0); // 气泡位置（浮点，单位：tab 序号，可表示两 tab 之间）
const isDragging = ref(false);
const dragDir = ref(1); // 拖动方向（决定液滴拉伸方向）

const blobStyle = computed(() => {
  const x = firstCenter.value - blobW.value / 2 + blobIdx.value * itemW.value;
  const scaleX = isDragging.value ? 1.18 : 1;
  return {
    width: `${blobW.value}px`,
    transform: `translateX(${x}px) scaleX(${scaleX})`,
    transformOrigin: isDragging.value
      ? dragDir.value > 0 ? "left center" : "right center"
      : "center"
  };
});

// ==================== 果冻挤压动画 ====================
const squishIndex = ref(-1);
let squishTimer: ReturnType<typeof setTimeout> | null = null;
function squish(index: number) {
  squishIndex.value = index;
  if (squishTimer) clearTimeout(squishTimer);
  squishTimer = setTimeout(() => {
    squishIndex.value = -1;
  }, 500);
}

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

// ==================== 拖动手势（Pointer Events，兼容触摸/鼠标） ====================
let pointerId: number | null = null;
let startX = 0;
let startIdxFloat = 0;
let startT = 0;
let lastX = 0;
let lastT = 0;
let lastV = 0; // 平滑速度 px/ms
let moved = 0;
let downItemIndex = -1;

function onPointerDown(e: PointerEvent) {
  if (!barEl.value) return;
  pointerId = e.pointerId;
  barEl.value.setPointerCapture(e.pointerId);
  startX = lastX = e.clientX;
  startT = lastT = performance.now();
  startIdxFloat = blobIdx.value;
  moved = 0;
  lastV = 0;
  const item = (e.target as HTMLElement).closest(".tabbar-item") as HTMLElement | null;
  downItemIndex = item ? Number(item.dataset.index) : -1;
}

function onPointerMove(e: PointerEvent) {
  if (pointerId === null || e.pointerId !== pointerId) return;
  const now = performance.now();
  const dt = now - lastT;
  if (dt > 0) {
    const v = (e.clientX - lastX) / dt;
    lastV = lastV * 0.75 + v * 0.25; // 指数平滑，抑制抖动
    if (Math.abs(v) > 0.05) dragDir.value = v > 0 ? 1 : -1;
  }
  lastX = e.clientX;
  lastT = now;
  const dx = e.clientX - startX;
  moved = Math.max(moved, Math.abs(dx));
  if (moved > 6) {
    isDragging.value = true;
    // 拖动实时跟手，位置钳制在首尾 tab 内
    blobIdx.value = clamp(
      startIdxFloat + dx / itemW.value,
      0,
      tabBarData.value.length - 1
    );
  }
}

function onPointerUp(e: PointerEvent) {
  if (pointerId === null || e.pointerId !== pointerId) return;
  pointerId = null;
  try {
    barEl.value?.releasePointerCapture(e.pointerId);
  } catch {
    /* 已释放 */
  }
  const duration = performance.now() - startT;

  // 轻点（几乎没移动）：等同点击该 tab
  if (moved < 10 && duration < 350 && downItemIndex >= 0) {
    isDragging.value = false;
    blobIdx.value = downItemIndex;
    squish(downItemIndex);
    if (downItemIndex !== active.value) {
      router.push(tabBarData.value[downItemIndex].path);
    }
    return;
  }
  releaseWithFling();
}

/** 松手：按惯性估算最终停留 tab，动画滑过去并跳转 */
function releaseWithFling() {
  isDragging.value = false;
  const n = tabBarData.value.length;
  const current = blobIdx.value;
  const v = lastV; // px/ms
  let target = current;
  if (Math.abs(v) > 0.15) {
    // fling 距离 = v²/(2a)，a=0.004 px/ms²；限幅 3.5 个 tab 防甩飞
    const distPx =
      Math.min((v * v) / (2 * 0.004), 3.5 * itemW.value) * Math.sign(v);
    target = current + distPx / itemW.value;
  }
  target = Math.round(clamp(target, 0, n - 1));
  // 惯性滑行到目标（transition 接管），落地后果冻挤压
  blobIdx.value = target;
  squish(target);
  if (target !== active.value) {
    router.push(tabBarData.value[target].path);
  }
}

function clamp(val: number, min: number, max: number) {
  return Math.min(max, Math.max(min, val));
}

// 监听路由变化（系统返回键/编程跳转）：气泡跟随，拖动中不打断
watch(
  () => route.path,
  (path) => {
    const index = tabBarRoutes.findIndex(item => item.path === path);
    if (index !== -1) {
      active.value = index;
      if (!isDragging.value) {
        blobIdx.value = index;
      }
    }
  },
  { immediate: true }
);

onMounted(async () => {
  await nextTick();
  measure();
  resizeObserver = new ResizeObserver(measure);
  if (barEl.value) resizeObserver.observe(barEl.value);
});

onUnmounted(() => {
  resizeObserver?.disconnect();
});
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
  position: relative;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 62px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow:
    0 4px 30px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  touch-action: none; // 拖动气泡时阻止页面滚动
  user-select: none;
  -webkit-user-select: none;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
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

// ==================== 液态气泡 ====================
.goo-blob {
  position: absolute;
  left: 0;
  top: 4px;
  height: 54px;
  border-radius: 18px;
  background: linear-gradient(135deg, #2e8b57, #277941);
  box-shadow: 0 4px 14px rgba(39, 121, 65, 0.4);
  pointer-events: none;
  z-index: 0;
  // 松手后惯性滑行：过冲回弹曲线（液滴感）
  transition: transform 0.45s cubic-bezier(0.3, 1.35, 0.45, 1);
  will-change: transform;

  // 拖动中：去掉过渡实时跟手
  &.dragging {
    transition: none;
  }
}

.dark .goo-blob {
  background: linear-gradient(135deg, #3ecf6e, #34c759);
  box-shadow: 0 4px 14px rgba(52, 199, 89, 0.3);
}

// ==================== 内容层 ====================
.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  position: relative;
  z-index: 1;
}

.tabbar-item-icon {
  font-size: 20px;
  color: #999;
  transition: color 0.25s ease;
  line-height: 24px;

  .active & {
    color: #fff; // 气泡实心，图标文字一起反白
  }
}

.dark .tabbar-item-icon {
  color: #666;

  .active & {
    color: #fff;
  }
}

// 果冻挤压动画：点击/落地时图标横向压扁再回弹
.tabbar-item.squish .tabbar-item-icon {
  animation: squish 0.45s cubic-bezier(0.3, 1.2, 0.4, 1);
}

@keyframes squish {
  0% {
    transform: scale(1, 1);
  }
  30% {
    transform: scale(1.35, 0.7);
  }
  60% {
    transform: scale(0.85, 1.15);
  }
  100% {
    transform: scale(1, 1);
  }
}

.tabbar-item-title {
  font-size: 10px;
  color: #999;
  margin-top: 1px;
  transition: color 0.25s ease;
  white-space: nowrap;

  .active & {
    color: #fff;
    font-weight: 600;
  }
}

.dark .tabbar-item-title {
  color: #666;

  .active & {
    color: #fff;
  }
}
</style>
