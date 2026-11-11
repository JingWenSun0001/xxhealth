# 熙心健康医生端

医生端八个模块均已实现。医生端入口为 `http://localhost:8081/#/doctor/dashboard`，实现工作台、我的预约、体检报告、患者管理、体检套餐、日程管理、消息通知和系统设置。工作台提供新建报告（三步）、编辑报告、模板管理、历史报告和异常提醒。

## 启动

1. 使用 MySQL 8 执行项目内 `sql/doctor_workspace.sql`。脚本创建 `health_check_system` 数据库和 5 张 `doctor_` 前缀表，使用 `CREATE TABLE IF NOT EXISTS` 和 `INSERT IGNORE`，不会清空原有用户端表。
2. 后端使用 `src/main/resources/application.yml`，默认连接本机 MySQL，端口 `8088`、上下文 `/tijiancms`。可通过 `XXHEALTH_DB_URL`、`XXHEALTH_DB_USERNAME`、`XXHEALTH_DB_PASSWORD` 设置连接。数据库凭据以该配置为准。
3. 项目根目录运行 `./mvnw.cmd spring-boot:run`。如果本机 Maven wrapper 下载不可用，可使用已安装的 Maven 执行同一命令。
4. 在 `xxhealth-fronted` 目录运行 `npm install`，然后运行 `npm run dev`（或 `npm run serve`）。
5. 打开入口。开发代理把 `/api` 和用户端兼容的 `/tijian` 前缀转到后端。

本次已在本机导入医生端数据。演示数据采用参考图中的 2025 年 6 月日期，预约日历初始定位该月份；点击日历右上角按钮可回到当前月。今日、本周、本月统计始终使用实际日期，因此历史演示数据不会计入今日统计。患者总数、报告数及分页数量来自数据库，不硬编码设计稿中的 8,230 等示例数值。

## 页面与操作

| 页面 | 路由 | 支持的操作 |
|---|---|---|
| 工作台 | `/doctor/dashboard` | 统计、待处理任务、七日趋势、快捷入口 |
| 我的预约 | `/doctor/appointments` | 月历切换、日期筛选、组合查询、分页、新增、确认、取消、完成、关联报告 |
| 体检报告 | `/doctor/reports` | 报告查询、筛选、分页、详情、编辑、下载 |
| 患者管理 | `/doctor/patients` | 多条件筛选、标签分组、档案增删改查、卡片/表格视图、列设置、选择导出、批量删除、CSV 导入 |
| 新建报告 | `/doctor/reports/new` | 选择患者和模板、录入检查结果、预览、保存草稿、提交审核 |
| 编辑报告 | `/doctor/reports/:id/edit` | 检查结果、数值异常提示、结论建议、健康评分、审核通过/退回 |
| 报告详情 | `/doctor/reports/:id` | 历史报告切换、总览、完整结果、健康评估、建议、异常详情、打印、分享链接 |
| 报告模板 | `/doctor/templates` | 分类、搜索、使用、新建模板及检查项选择 |
| 历史报告 | `/doctor/history` | 列表查询和下载 |
| 异常提醒 | `/doctor/alerts` | 分类、查询、分页、查看报告、标记处理 |

报告流程为 **草稿 → 待审核 → 已生成**，待审核可退回草稿。已生成报告禁止修改。审核通过时，异常项目自动生成提醒并更新患者健康状态和随访标记。患者采用软删除；有待确认或已确认预约时不能删除，防止遗漏未完成服务。

报告下载为含完整结果的独立 HTML 文件；“打印报告”使用浏览器打印，可选择保存 PDF。分享复制当前报告的访问链接。导入使用 UTF-8 CSV，可在“批量操作 → 下载导入模板”取得格式；每批最多 500 条，导入为事务操作，任一条失败整批回滚。导出最多 500 条，页面会提示超出范围。

## 接口

统一前缀：`/tijiancms/doctor/workspace`。返回 `{code,message,data}`；参数错误使用 HTTP 400，数据冲突使用 HTTP 409。日期格式 `yyyy-MM-dd`，月份格式 `yyyy-MM`。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/dashboard` | 工作台统计、3条待处理任务、七日趋势 |
| GET | `/patients` | `name,phone,identityCard,healthStatus,age,sex,startDate,endDate,tab,page,pageSize` |
| GET/PUT/DELETE | `/patients/{id}` | 患者详情、修改、软删除 |
| POST | `/patients` | 新建患者 |
| POST | `/patients/import` | 患者对象数组，最多500条 |
| GET | `/appointments` | `name,phone,status,packageName,date,page,pageSize` |
| POST | `/appointments` | `patientId,examDate,timeSlot,packageName,source`，时段格式 `09:00–10:00` |
| PUT | `/appointments/{id}/status` | `{status:待确认/已确认/已完成/已取消}`，按合法流程校验 |
| GET | `/calendar?month=2025-06` | 月内每日预约数量 |
| GET | `/reports` | `name,patientId,status,packageName,range,page,pageSize` |
| GET | `/reports/{id}` | 完整报告、检查项和患者历史报告 |
| POST | `/reports` | `patientId,templateId,examDate,conclusion` |
| PUT | `/reports/{id}` | `score,conclusion,advice,items` |
| PUT | `/reports/{id}/status` | `{status:待审核/草稿/已生成}` |
| GET/POST | `/templates` | 查询/新增模板 |
| GET | `/alerts` | `name,type,page,pageSize` |
| PUT | `/alerts/{id}` | 标记已处理 |

分页返回 `{records,total,page,pageSize}`，每页上限500。检查项结构包含 `group,name,value,unit,range,min,max,abnormal`。分组使用“基础项目、生化检查、影像检查、其他检查”。

## 兼容与验证

修复了原项目已有合并冲突，合并保留用户端路由、预约/套餐等服务方法及医生端原有报告 CRUD 接口。手机端注册和预约现已在同一数据库事务内同步到医生端患者、预约、日程和通知；取消与归档状态也会同步。历史订单可通过 `POST /tijiancms/doctor/workspace/mobile-sync` 补同步。后四项现为独立功能页面：套餐与预约联动、日程与预约双向同步、消息与预约及报告事件联动、设置持久化并应用到工作流程。

视觉以提供的八张截图为依据，参考基准为1536×1024；品牌、侧栏图标、人物、统计图标、日历插图和X光演示图来自参考图局部提取，已保存到 `public/doctor-assets`，运行时不依赖原始图片路径。X光图片为设计演示素材，不是从患者影像系统读取。

验证命令：项目根目录 `mvn test`；前端目录 `npm run build`。新增集成测试覆盖患者 CRUD、有效性和重复档案校验、SQL 注入输入、预约流转和重复预约限制、报告审核约束、审核后的提醒联动及接口返回字段，测试事务回滚数据。数据库需先执行上述SQL。
