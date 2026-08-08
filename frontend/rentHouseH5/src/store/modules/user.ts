import { defineStore } from "pinia";
import type {
  loginQueryInterface,
  UserInfoInterface,
  UserStateInterface
} from "@/api/user/types";
import { getUserInfo, login } from "@/api/user";
import { removeToken, setToken } from "@/utils/token";

export const useUserStore = defineStore({
  id: "app-user",
  state: (): UserStateInterface => ({
    token: null,
    userInfo: null
  }),
  actions: {
    // setToken
    setToken(token: string) {
      this.token = token;
    },
    // login
    async LoginAction(params: loginQueryInterface) {
      const { data } = await login(params);
      setToken(data);
      // 清除旧缓存，强制重新获取
      this.userInfo = null;
      await this.GetInfoAction();
    },
    // setUserInfo
    setUserInfo(userInfo: UserInfoInterface) {
      this.userInfo = userInfo;
    },
    async GetInfoAction() {
      try {
        const { data } = await getUserInfo();
        this.userInfo = data;
      } catch (e) {
        console.error("获取用户信息失败", e);
      }
    },
    async Logout() {
      // await logout()
      this.resetUserStore();
      removeToken();
    },
    resetUserStore() {
      this.token = null;
      this.userInfo = null;
    }
  },
  // 设置为true，缓存state
  persist: true
});
