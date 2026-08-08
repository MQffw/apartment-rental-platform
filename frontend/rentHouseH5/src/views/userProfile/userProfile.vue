<template>
  <div class="profile-page" :class="{ dark: isDark }">
    <!-- Header with avatar -->
    <div class="profile-header">
      <div class="profile-header-bg"></div>
      <div class="profile-avatar-wrapper">
        <van-image
          round
          width="72px"
          height="72px"
          :src="userStore.userInfo?.avatarUrl || defaultAvatarUrl"
          class="profile-avatar"
        >
          <template v-slot:error>
            <van-icon name="user-o" size="32" color="#fff" />
          </template>
        </van-image>
        <div class="profile-name">{{ form.nickname || '未设置昵称' }}</div>
      </div>
    </div>

    <!-- Form body -->
    <div class="profile-body">
      <!-- Basic info section -->
      <div>
        <div class="profile-section-title">基本信息</div>
        <div class="profile-card">
          <div class="profile-field">
            <div class="profile-field-icon">
              <van-icon name="user-o" size="18" />
            </div>
            <div class="profile-field-body">
              <div class="profile-field-label">昵称</div>
              <input
                v-model="form.nickname"
                class="profile-field-input"
                placeholder="请输入昵称"
              />
            </div>
          </div>
          <div class="profile-field-divider"></div>
          <div class="profile-field">
            <div class="profile-field-icon">
              <van-icon name="phone-o" size="18" />
            </div>
            <div class="profile-field-body">
              <div class="profile-field-label">手机号</div>
              <input
                v-model="form.phone"
                class="profile-field-input"
                type="tel"
                maxlength="11"
                placeholder="请输入手机号"
              />
            </div>
          </div>
          <div class="profile-field-divider"></div>
          <div class="profile-field">
            <div class="profile-field-icon">
              <van-icon name="envelop-o" size="18" />
            </div>
            <div class="profile-field-body">
              <div class="profile-field-label">邮箱</div>
              <div class="profile-field-value">{{ form.email || '未绑定' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Security section -->
      <div>
        <div class="profile-section-title">账号安全</div>
        <div class="profile-card">
          <div class="profile-field" @click="router.push('/setPassword')">
            <div class="profile-field-icon">
              <van-icon name="lock" size="18" />
            </div>
            <div class="profile-field-body">
              <div class="profile-field-label">登录密码</div>
              <div class="profile-field-value" :class="{ 'is-set': hasPwd }">
                {{ hasPwd ? '已设置' : '未设置' }}
              </div>
            </div>
            <van-icon name="arrow" class="profile-field-arrow" />
          </div>
        </div>
      </div>
    </div>

    <!-- Save button -->
    <div class="profile-save-wrapper">
      <van-button
        type="primary"
        block
        round
        class="profile-save-btn"
        @click="handleSave"
      >
        保存修改
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { getUserInfo, updateProfile, hasPassword } from "@/api/user";
import { useUserStore } from "@/store/modules/user";
import { useDarkMode } from "@/hooks/useToggleDarkMode";
import { showSuccessToast, showFailToast } from "vant";
import { useRouter } from "vue-router";
import defaultAvatarUrl from "@/assets/logo_melomini.png";

const router = useRouter();
const userStore = useUserStore();
const isDark = computed(() => useDarkMode());
const form = ref({
  email: "",
  nickname: "",
  phone: ""
});
const hasPwd = ref(false);

async function fetchUserInfo() {
  const storeEmail = userStore.userInfo?.email;
  if (storeEmail) {
    form.value.email = storeEmail;
  }
  const { data } = await getUserInfo();
  form.value.email = data.email || storeEmail || "";
  form.value.nickname = data.nickname || "";
  form.value.phone = data.phone || "";
}

async function checkHasPassword() {
  try {
    const { data } = await hasPassword();
    hasPwd.value = data;
  } catch (e) {
    // ignore
  }
}

async function handleSave() {
  if (!form.value.nickname.trim()) {
    showFailToast("昵称不能为空");
    return;
  }
  await updateProfile({
    nickname: form.value.nickname,
    phone: form.value.phone
  });
  await userStore.GetInfoAction();
  showSuccessToast("保存成功");
  router.back();
}

onMounted(() => {
  fetchUserInfo();
  checkHasPassword();
});
</script>

<style scoped lang="less">
.profile-page {
  height: calc(100vh - 46px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;

  &.dark {
    background: #1a1a1a;
  }
}

.profile-header {
  position: relative;
  flex-shrink: 0;
  height: 140px;
}

.profile-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 125px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border-radius: 0 0 24px 24px;
}

.profile-avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 16px;
  position: relative;
  z-index: 1;
}

.profile-avatar {
  border: 3px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.profile-name {
  margin-top: 6px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}


.profile-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 12px 16px 0;
  gap: 12px;
}

.profile-section-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
  padding-left: 4px;

  .dark & {
    color: #666;
  }
}

.profile-card {
  background: #fff;
  border-radius: 14px;
  padding: 2px 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);

  .dark & {
    background: #2c2c2c;
    box-shadow: none;
  }
}

.profile-field {
  display: flex;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
}

.profile-field-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(39, 121, 65, 0.08);
  color: #277941;
  margin-right: 10px;
  flex-shrink: 0;

  .dark & {
    background: rgba(52, 199, 89, 0.12);
    color: #34c759;
  }
}

.profile-field-body {
  flex: 1;
  min-width: 0;
}

.profile-field-label {
  font-size: 11px;
  color: #999;
  margin-bottom: 1px;

  .dark & {
    color: #777;
  }
}

.profile-field-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 15px;
  color: #333;
  background: transparent;
  padding: 0;

  &::placeholder {
    color: #ccc;
  }

  .dark & {
    color: #e0e0e0;

    &::placeholder {
      color: #555;
    }
  }
}

.profile-field-value {
  font-size: 15px;
  color: #999;

  &.is-set {
    color: #277941;

    .dark & {
      color: #34c759;
    }
  }

  .dark & {
    color: #666;
  }
}

.profile-field-divider {
  height: 1px;
  background: #f0f0f0;

  .dark & {
    background: #3a3a3a;
  }
}

.profile-field-arrow {
  font-size: 14px;
  color: #c0c0c0;
  margin-left: 8px;

  .dark & {
    color: #555;
  }
}

.profile-save-wrapper {
  flex-shrink: 0;
  padding: 16px 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

.profile-save-btn {
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #277941, #34c759) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(39, 121, 65, 0.3);

  .dark & {
    box-shadow: 0 4px 16px rgba(52, 199, 89, 0.2);
  }

  &:active {
    opacity: 0.9;
  }
}
</style>
