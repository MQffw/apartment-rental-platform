<template>
  <div class="user-container" :class="{ dark: isDark }">
    <!-- 顶部用户信息 -->
    <div class="user-header">
      <div class="user-header-bg"></div>
      <div class="user-header-content">
        <van-image
          @click="showImagePreview([userStore.userInfo?.avatarUrl || defaultAvatarUrl])"
          round
          width="72px"
          height="72px"
          :src="userStore.userInfo?.avatarUrl || defaultAvatarUrl"
          class="user-avatar"
        >
          <template v-slot:error>
            <van-icon name="user-o" size="32" color="#fff" />
          </template>
        </van-image>
        <div class="user-nickname">
          {{ userStore.userInfo?.nickname || "未登录" }}
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-card">
      <div
        v-for="(item, index) in navList"
        :key="item.path"
        class="menu-item"
        :class="{ 'menu-item-border': index < navList.length - 1 }"
        @click="router.push(item.path)"
      >
        <div class="menu-item-left">
          <SvgIcon :name="item.icon" size="28" />
          <span class="menu-item-name">{{ item.name }}</span>
        </div>
        <van-icon name="arrow" class="menu-item-arrow" />
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-wrapper">
      <van-button
        round
        block
        plain
        type="danger"
        class="logout-btn"
        @click="logoutHandle"
      >
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts" name="UserCenter">
import { useUserStore } from "@/store/modules/user";
import { showImagePreview } from "vant";
import defaultAvatarUrl from "@/assets/logo_melomini.png";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useDarkMode } from "@/hooks/useToggleDarkMode";
const router = useRouter();
const isDark = computed(() => useDarkMode());
const navList = ref([
  {
    icon: "历史",
    name: "浏览历史",
    path: "/browsingHistory"
  },
  {
    icon: "预约",
    name: "我的预约",
    path: "/myAppointment"
  },
  {
    icon: "合同",
    name: "我的租约",
    path: "/myAgreement"
  },
  {
    icon: "个人资料",
    name: "个人资料",
    path: "/userProfile"
  }
]);
const userStore = useUserStore();
// 退出登录
const logoutHandle = () => {
  userStore.Logout();
  router.replace("/login");
};
onMounted(async () => {
  await userStore.GetInfoAction();
});
</script>

<style scoped lang="less">
.user-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
  transition: background 0.3s ease;
}

.user-container.dark {
  background: #1a1a1a;
}

.user-header {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.user-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 125px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border-radius: 0 0 24px 24px;
}

.user-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 16px;
  position: relative;
  z-index: 1;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.user-nickname {
  margin-top: 6px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}

.menu-card {
  margin: 20px 16px 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.dark .menu-card {
  background: #2c2c2c;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  transition: background 0.15s;

  &:active {
    background: #f5f7fa;
  }
}

.dark .menu-item:active {
  background: #3a3a3a;
}

.menu-item-border {
  border-bottom: 1px solid #f0f0f0;
}

.dark .menu-item-border {
  border-bottom-color: #3a3a3a;
}

.menu-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-item-name {
  font-size: 15px;
  color: #333;
  transition: color 0.3s ease;
}

.dark .menu-item-name {
  color: #e0e0e0;
}

.menu-item-arrow {
  font-size: 14px;
  color: #c0c0c0;
  transition: color 0.3s ease;
}

.dark .menu-item-arrow {
  color: #666;
}

.logout-wrapper {
  margin: 30px 16px 0;
}

.logout-btn {
  height: 44px;
  font-size: 15px;
}
</style>
