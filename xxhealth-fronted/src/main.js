import { createApp } from "vue";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import "./doctor/doctor.css";
import "font-awesome/css/font-awesome.min.css";

createApp(App).use(router).use(ElementPlus).mount("#app");

import "./doctor/operations.css";
