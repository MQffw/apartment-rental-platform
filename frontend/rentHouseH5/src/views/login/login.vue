<template>
  <van-config-provider theme="light">
  <div class="login-page">
    <!-- 顶部装饰背景 -->
    <div class="login-header">
      <div class="login-header-bg"></div>
      <div class="login-header-content">
        <van-image round width="80px" height="80px" :src="defaultAvatarUrl" />
        <div class="login-title">硅谷租房</div>
        <div class="login-subtitle">欢迎回来</div>
      </div>
    </div>

    <!-- 登录表单 -->
    <div class="login-form">
      <!-- 登录模式切换 -->
      <div class="login-mode-switch">
        <div
          class="mode-tab"
          :class="{ active: loginMode === 'code' }"
          @click="loginMode = 'code'"
        >
          验证码登录
        </div>
        <div
          class="mode-tab"
          :class="{ active: loginMode === 'password' }"
          @click="loginMode = 'password'"
        >
          密码登录
        </div>
      </div>

      <van-form ref="formRef">
        <!-- 邮箱输入框（两种模式通用） -->
        <div class="form-group">
          <div class="form-label">邮箱</div>
          <van-field
            v-model.trim="loginInfo.email"
            name="email"
            type="email"
            autocomplete="off"
            placeholder="请输入邮箱地址"
            :border="false"
            class="custom-field"
            :rules="[
              {
                required: true,
                pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message: '请正确填写邮箱'
              }
            ]"
          >
            <template #left-icon>
              <van-icon name="envelop-o" class="field-icon" />
            </template>
          </van-field>
        </div>

        <!-- 验证码模式 -->
        <template v-if="loginMode === 'code'">
          <div class="form-group">
            <div class="form-label">验证码</div>
            <van-field
              v-model.trim="loginInfo.code"
              name="code"
              placeholder="请输入6位验证码"
              clearable
              type="digit"
              maxlength="6"
              autocomplete="off"
              :border="false"
              class="custom-field"
              :rules="[
                {
                  required: true,
                  pattern: /^\d{6}$/,
                  message: '请正确填写验证码'
                }
              ]"
            >
              <template #left-icon>
                <van-icon name="shield-o" class="field-icon" />
              </template>
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  class="code-btn"
                  :disabled="codeSendStatus"
                  @click="getCodeHandle"
                >
                  {{ codeSendStatus ? `${countdownNum}s 后重发` : '发送验证码' }}
                </van-button>
              </template>
            </van-field>
          </div>
        </template>

        <!-- 密码模式 -->
        <template v-else>
          <div class="form-group">
            <div class="form-label">密码</div>
            <van-field
              v-model.trim="loginInfo.password"
              name="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="off"
              :border="false"
              class="custom-field"
              :rules="[{ required: true, message: '请输入密码' }]"
            >
              <template #left-icon>
                <van-icon name="lock" class="field-icon" />
              </template>
            </van-field>
          </div>
        </template>

        <div class="mt-[30px]">
          <loading-button
            round
            block
            type="primary"
            native-type="submit"
            :loadingClick="onSubmitHandle"
            class="login-btn"
          >
            登 录
          </loading-button>
        </div>
      </van-form>
    </div>
  </div>
  </van-config-provider>
</template>
<script setup lang="ts">
import defaultAvatarUrl from "@/assets/logo_melomini.png";
import { ref, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getEmailCode, login, loginByPassword } from "@/api/user";
import type { FormInstance } from "vant";
import { showFailToast } from "vant";
import { useUserStore } from "@/store/modules/user";
import LoadingButton from "@/components/LoadingButton/LoadingButton.vue";
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 登录模式：code=验证码，password=密码
const loginMode = ref<"code" | "password">("code");

// 登录信息
const loginInfo = ref({
  email: "",
  code: "",
  password: ""
});
// 表单实例
const formRef = ref<FormInstance>();

// 验证码倒计时
const codeSendStatus = ref(false);
const countdownNum = ref(60);
let timer: ReturnType<typeof setInterval> | null = null;

function startCountdown() {
  codeSendStatus.value = true;
  countdownNum.value = 60;
  timer = setInterval(() => {
    countdownNum.value--;
    if (countdownNum.value <= 0) {
      codeSendStatus.value = false;
      if (timer) clearInterval(timer);
    }
  }, 1000);
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

// 获取验证码
const getCodeHandle = async () => {
  await formRef.value?.validate("email");
  try {
    await getEmailCode({ email: loginInfo.value.email });
    startCountdown();
  } catch (e) {
    showFailToast("验证码发送失败，请稍后再试");
  }
};
const onSubmitHandle = async () => {
  await formRef.value?.validate();
  let token = "";
  if (loginMode.value === "code") {
    // 验证码登录
    const { data } = await login(loginInfo.value);
    token = data;
  } else {
    // 密码登录
    const { data } = await loginByPassword({
      email: loginInfo.value.email,
      password: loginInfo.value.password
    });
    token = data;
  }
  // 设置 token
  userStore.setToken(token);
  // 跳转到主页（不调 GetInfoAction，由页面自己获取）
  const redirect = route.query?.redirect;
  await router.replace(redirect ? decodeURIComponent(redirect as string) : "/");
};
</script>

<style scoped lang="less">
.login-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.login-header {
  position: relative;
  height: 260px;
  overflow: hidden;
}

.login-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border-radius: 0 0 40px 40px;
}

.login-header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;

  :deep(.van-image) {
    border: 3px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.login-title {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.login-subtitle {
  margin-top: 4px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.login-form {
  margin: -30px 20px 0;
  padding: 24px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 2;
}

.login-mode-switch {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 24px;
}

.mode-tab {
  font-size: 15px;
  color: #999;
  padding-bottom: 8px;
  cursor: pointer;
  transition: all 0.25s;

  &.active {
    color: #277941;
    font-weight: 600;
    border-bottom: 2px solid #277941;
  }
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  padding-left: 4px;
}

.custom-field {
  background: #f5f7fa;
  border-radius: 12px;
  border: 1.5px solid #e8e8e8;
  transition: border-color 0.2s;

  &:focus-within {
    border-color: #277941;
    background: #fff;
  }

  :deep(.van-field__left-icon) {
    margin-right: 8px;
  }

  :deep(.van-field__control) {
    font-size: 15px;
  }
}

.field-icon {
  font-size: 18px;
  color: #999;
}

.code-btn {
  border-radius: 8px;
  font-size: 13px;
  padding: 0 12px;
  height: 32px;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #277941 0%, #34c759 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(39, 121, 65, 0.3);
}
</style>
