import { defineStore } from "pinia";
import { store } from "@/store";

const darkModeKey = "__dark_mode__";
const isDarkMode = () => {
  const darkMode = window.localStorage.getItem(darkModeKey);
  if (darkMode) {
    return darkMode === "true";
  } else {
    return window.matchMedia("(prefers-color-scheme: dark)").matches;
  }
};

export const useDarkModeStore = defineStore({
  id: "dark-mode",
  state: () => ({
    darkMode: isDarkMode()
  }),
  actions: {
    // 应用启动时调用：把已保存的深色偏好同步到 <html> 上。
    // 否则刷新后 store 虽读到了偏好，但 html.dark 缺失，
    // 全局 html.dark 系列样式（毛玻璃条/Vant 暗色变量）全部失效
    initDarkMode() {
      if (this.darkMode) {
        document.documentElement.classList.add("dark");
      } else {
        document.documentElement.classList.remove("dark");
      }
    },
    toggleDarkMode() {
      this.darkMode = !this.darkMode;
      if (this.darkMode) {
        document.documentElement.classList.add("dark");
        window.localStorage.setItem(darkModeKey, "true");
      } else {
        document.documentElement.classList.remove("dark");
        window.localStorage.setItem(darkModeKey, "false");
      }
    }
  }
});

export function useDarkModeStoreHook() {
  return useDarkModeStore(store);
}
