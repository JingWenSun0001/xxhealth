# 手机端与医生端联调

手机端源码已克隆到 `mobile-client`。两套前端共用项目根目录的 Spring Boot 后端和同一个 `health_check_system` 数据库；不要再单独启动 `mobile-client` 里的旧后端，否则会产生端口和数据源分叉。

## 端口

| 服务 | 地址 |
|---|---|
| 手机端 | `http://localhost:8080` |
| 医生端 | `http://localhost:8081/#/doctor/dashboard` |
| 共享后端 | `http://localhost:8088/tijiancms` |

## 启动

1. 在项目根目录运行 `.\mvnw.cmd spring-boot:run`。
2. 在 `mobile-client\xxhealth-fronted` 运行 `npm install`，再运行 `npm run serve`。
3. 在根目录的 `xxhealth-fronted` 运行 `npm install`，再运行 `npm run dev`。

两个前端的开发代理都指向 8088。手机端继续使用原来的 `/tijian` 请求前缀，代理会改写到后端的 `/tijiancms`，因此不需要改动各页面接口。

## 数据联动

- 手机端注册成功后，同一事务创建或更新 `doctor_patient` 患者档案。
- 手机端预约成功后，同一事务创建 `doctor_appointment`、`doctor_schedule` 和医生站内预约通知。
- 手机端取消预约时，医生端预约和日程同步为“已取消”。
- 用户端订单归档后，医生端预约和日程同步为“已完成”。
- 医生端预约号使用 `MOB` 加十位用户端订单号，重复同步不会产生重复预约。
- 历史手机端订单可调用 `POST /tijiancms/doctor/workspace/mobile-sync` 批量补同步。

## 双向适配

- 手机端“我的预约”直接读取医生工作台状态，可展示待确认、已确认、已完成、已取消，以及医生安排的时段、诊室和备注。
- 医生端确认、完成、取消或调整手机来源的预约后，手机端刷新即可看到最新日期、时段和状态。
- 医生端将报告状态更新为“已生成”后，手机端“体检报告”可以查看健康评分、医生结论、健康建议、检查指标和异常提醒；草稿及待审核报告不会暴露给患者。
- 手机端新增“健康消息”和首页下一次体检卡片，消息由预约状态与已发布报告生成。
- 医生完成预约或发布与手机订单匹配的报告时，旧订单会同步归档，保持历史接口兼容。

手机适配接口统一使用 `POST /tijiancms/mobile/workspace/*`：

| 接口 | 用途 |
|---|---|
| `/appointments` | 当前用户预约与医生状态 |
| `/reports` | 已正式发布的报告列表 |
| `/reports/detail` | 报告详情（校验手机号归属） |
| `/summary` | 首页预约、报告和风险摘要 |
| `/messages` | 预约与报告消息 |

手机仓库现有功能提交的是注册资料、预约订单和查询请求，并没有文件选择或文件上传页面。本次联调覆盖仓库当前所有会写入业务数据的手机端操作，并补齐医生状态、排班、报告与消息的患者端读取能力。

## 验证

运行 `.\mvnw.cmd "-Dtest=MobileDoctorSyncIntegrationTests,MobileWorkspaceIntegrationTests" test`，测试会完整验证“注册 → 预约 → 医生患者/预约/日程/通知 → 医生确认/完成 → 手机状态与报告”的双向链路。两个前端分别运行 `npm run build` 验证构建。
