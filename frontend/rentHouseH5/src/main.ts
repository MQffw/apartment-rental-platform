import { createApp } from "vue";
import { store } from "./store";
import { useDarkModeStoreHook } from "./store/modules/darkMode";
// normalize.css
import "normalize.css/normalize.css";
// 引入样式
import "./styles/index.ts";
// svg icon
import "virtual:svg-icons-register";

import App from "./App.vue";
import router from "./router";
// 引入指令
import directives from "@/directives";
const app = createApp(App);
app.use(store);
app.use(router);
app.use(directives);

// 启动时同步深色模式偏好到 <html>，保证刷新后全局暗色样式生效
useDarkModeStoreHook().initDarkMode();

app.mount("#app");
