import { reactive } from "vue";
import http from "../api/http";
import { ElMessage, ElMessageBox } from "element-plus";
export const api = {
  get: (url, params) => http.get("/doctor/workspace" + url, { params }),
  post: (url, data) =>
    http.post("/doctor/workspace" + url, data).then((result) => {
      window.dispatchEvent(new Event("doctor:refresh"));
      return result;
    }),
  put: (url, data) =>
    http.put("/doctor/workspace" + url, data).then((result) => {
      window.dispatchEvent(new Event("doctor:refresh"));
      return result;
    }),
  delete: (url) => http.delete("/doctor/workspace" + url),
};
export async function run(action, success) {
  try {
    const result = await action();
    if (success) ElMessage.success(success);
    return result;
  } catch (e) {
    ElMessage.error(e.message || "请求失败，请检查后端与数据库连接");
    throw e;
  }
}
export async function confirm(text) {
  try {
    await ElMessageBox.confirm(text, "操作确认", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    return true;
  } catch {
    return false;
  }
}
export const avatar = (value) =>
  "/doctor-assets/" + (value || "female") + ".png";
export const maskName = (name = "") =>
  name.length > 1 ? name[0] + "*" + name.slice(-1) : name;
export const maskPhone = (phone = "") =>
  phone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
export const maskCard = (card = "") =>
  card.slice(0, 3) + "********" + card.slice(-4);
export const today = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(
    2,
    "0"
  )}-${String(d.getDate()).padStart(2, "0")}`;
};
export const packages = reactive([
  "中青年体检套餐",
  "高端体检套餐",
  "入职体检套餐",
  "基础体检套餐",
  "老年体检套餐",
  "学生体检套餐",
]);
export function downloadCsv(rows, headers, name) {
  const cell = (v) =>
    '"' +
    String(v ?? "")
      .replace(/^[=+@-]/, "'" + "$&")
      .replace(/"/g, '""') +
    '"';
  const text =
    "\ufeff" +
    [
      headers.map((h) => cell(h[0])).join(","),
      ...rows.map((r) => headers.map((h) => cell(r[h[1]])).join(",")),
    ].join("\r\n");
  const a = document.createElement("a");
  a.href = URL.createObjectURL(
    new Blob([text], { type: "text/csv;charset=utf-8;" })
  );
  a.download = name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}

export const workspaceSettings = reactive({
  profile: {
    doctorName: "张医生",
    department: "健康管理科",
    title: "主治医师",
  },
  preferences: {
    pageSize: 10,
    calendarView: "day",
    landingPage: "dashboard",
    defaultRoom: "体检一室",
  },
});
export async function loadSettings() {
  const s = await api.get("/settings");
  Object.assign(workspaceSettings, s);
  return s;
}
export async function loadCatalog() {
  const d = await api.get("/packages", { enabled: 1, pageSize: 500 });
  packages.splice(0, packages.length, ...d.records.map((p) => p.name));
  return d.records;
}
