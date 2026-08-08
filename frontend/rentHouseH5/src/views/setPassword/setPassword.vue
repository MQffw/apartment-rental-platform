<template>
  <div class="pwd-page" :class="{ dark: isDark }">
    <!-- Header -->
    <div class="pwd-header">
      <div class="pwd-header-bg"></div>
      <div class="pwd-header-content">
        <div class="pwd-header-icon-wrap">
          <van-icon name="lock" size="36" color="#fff" />
        </div>
        <div class="pwd-header-title">设置密码</div>
        <div class="pwd-header-desc">设置后可使用密码登录，无需验证码</div>
      </div>
    </div>

    <!-- Form body -->
    <div class="pwd-body">
      <div class="pwd-section-title">密码信息</div>
      <div class="pwd-card">
        <div class="pwd-field">
          <div class="pwd-field-icon">
            <van-icon name="lock" size="18" />
          </div>
          <div class="pwd-field-body">
            <div class="pwd-field-label">新密码</div>
            <input
              v-model="form.password"
              class="pwd-field-input"
              type="password"
              placeholder="请输入密码（至少6位）"
            />
          </div>
        </div>
        <div class="pwd-field-divider"></div>
        <div class="pwd-field">
          <div class="pwd-field-icon">
            <van-icon name="shield-o" size="18" />
          </div>
          <div class="pwd-field-body">
            <div class="pwd-field-label">确认密码</div>
            <input
              v-model="form.confirmPassword"
              class="pwd-field-input"
              type="password"
              placeholder="请再次输入密码"
            />
          </div>
        </div>
      </div>

      <!-- Password strength indicator -->
      <div class="pwd-strength" v-if="form.password">
        <div class="pwd-strength-label">密码强度</div>
        <div class="pwd-strength-bars">
          <div
            v-for="i in 3"
            :key="i"
            class="pwd-strength-bar"
            :class="{ active: strengthLevel >= i }"
            :style="{ background: strengthLevel >= i ? strengthColor : undefined }"
          ></div>
        </div>
        <div class="pwd-strength-text" :style="{ color: strengthColor }">{{ strengthText }}</div>
      </div>
    </div>

    <!-- Submit button -->
    <div class="pwd-save-wrapper">
      <van-button
        type="primary"
        block
        round
        class="pwd-save-btn"
        @click="handleSave"
      >
        确认设置
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { setPassword } from "@/api/user";
import { showSuccessToast, showFailToast } from "vant";
import { useRouter } from "vue-router";
import { useDarkMode } from "@/hooks/useToggleDarkMode";

const router = useRouter();
const isDark = computed(() => useDarkMode());
const form = ref({
  password: "",
  confirmPassword: ""
});

const strengthLevel = computed(() => {
  const pwd = form.value.password;
  if (!pwd) return 0;
  let level = 0;
  if (pwd.length >= 6) level++;
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) level++;
  if (/\d/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) level++;
  return level;
});

const strengthColor = computed(() => {
  const colors = ["#e74c3c", "#f39c12", "#27ae60"];
  return colors[strengthLevel.value - 1] || "#e74c3c";
});

const strengthText = computed(() => {
  const texts = ["弱", "中", "强"];
  return texts[strengthLevel.value - 1] || "弱";
});

async function handleSave() {
  if (form.value.password.length < 6) {
    showFailToast("密码长度不能少于6位");
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    showFailToast("两次密码不一致");
    return;
  }
  await setPassword(form.value.password);
  showSuccessToast("密码设置成功");
  router.back();
}
</script>

<style scoped lang="less">
.pwd-page {
  height: calc(100vh - 46px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;

  &.dark {
    background: #1a1a1a;
  }
}

.pwd-header {
  position: relative;
  flex-shrink: 0;
  height: 150px;
}

.pwd-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 130px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border-radius: 0 0 24px 24px;
}

.pwd-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 24px;
  position: relative;
  z-index: 1;
}

.pwd-header-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.pwd-header-title {
  margin-top: 8px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}

.pwd-header-desc {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.pwd-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px 16px 0;
  gap: 12px;
}

.pwd-section-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
  padding-left: 4px;

  .dark & {
    color: #666;
  }
}

.pwd-card {
  background: #fff;
  border-radius: 14px;
  padding: 2px 16px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);

  .dark & {
    background: #2c2c2c;
    box-shadow: none;
  }
}

.pwd-field {
  display: flex;
  align-items: center;
  padding: 12px 0;
}

.pwd-field-icon {
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

.pwd-field-body {
  flex: 1;
  min-width: 0;
}

.pwd-field-label {
  font-size: 11px;
  color: #999;
  margin-bottom: 1px;

  .dark & {
    color: #777;
  }
}

.pwd-field-input {
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

.pwd-field-divider {
  height: 1px;
  background: #f0f0f0;

  .dark & {
    background: #3a3a3a;
  }
}

.pwd-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px 0;
}

.pwd-strength-label {
  font-size: 11px;
  color: #999;

  .dark & {
    color: #666;
  }
}

.pwd-strength-bars {
  display: flex;
  gap: 4px;
  flex: 1;
}

.pwd-strength-bar {
  height: 4px;
  flex: 1;
  border-radius: 2px;
  background: #e8e8e8;
  transition: background 0.3s ease;

  .dark & {
    background: #3a3a3a;
  }
}

.pwd-strength-text {
  font-size: 11px;
  font-weight: 600;
  min-width: 16px;
}

.pwd-save-wrapper {
  flex-shrink: 0;
  padding: 16px 16px;
  padding-bottom: calc(16px + env(safe-area-inset-bottom));
}

.pwd-save-btn {
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
