# 医生端后四个模块

升级前先保留现有数据库，依次执行 `sql/doctor_workspace.sql`、`sql/doctor_operations.sql`。两个脚本均可重复执行，不清空已有业务数据。第二个脚本新增套餐、日程、站内消息、设置和操作记录，并给已有预约补齐关联日程。本次已在本机执行。

| 模块 | 页面 | 功能 |
|---|---|---|
| 体检套餐 | `/#/doctor/packages` | 分类、性别/价格/特色筛选、关键词搜索、排序、分页、详情、选择并预约；管理模式支持新增、编辑、上下架 |
| 日程管理 | `/#/doctor/schedule` | 日/周/月视图、月份与日期切换、上午/下午筛选、单条调整、状态管理、批量改期/平移、检查提醒、异常提醒、排班规则 |
| 消息通知 | `/#/doctor/notifications` | 分类统计、关键词/状态/时间筛选、分页、详情、已读/未读、全部已读、跳转关联预约/报告/异常 |
| 系统设置 | `/#/doctor/settings` | 医生档案、默认入口、患者列表条数、日程默认视图、默认诊室、站内消息偏好、体检排班规则、操作记录 |

套餐和日程、消息按此次三张参考图布局实现；系统设置沿用相同的侧栏、浅蓝背景和卡片样式。横幅、人物及部分图标从参考图提取到 `public/doctor-assets`，运行不依赖项目外图片路径。

## 联动和规则

- 新增预约会校验套餐上架状态与适用性别、工作日、工作时间、时段长度、每日容量及默认诊室冲突；随后自动生成同一预约的日程和站内通知。预约时段选择由排班规则生成。
- 预约确认、取消、完成同步到日程。日程调整同步预约日期、时段和状态；进行中的体检不能改期，完成后不可重复完成，避免患者就诊次数重复累加。
- 单条和批量日程均校验诊室重叠、患者重复预约、工作时间和每日总容量。批量操作以事务保存，任一冲突整批失败。
- 报告审核通过生成报告通知；异常指标生成异常提醒消息。消息已读与临床异常“已处理”是独立状态，阅读消息不会自动关闭异常。
- 顶部和侧栏消息角标读取真实未读数。修改为已读、生成业务通知后刷新角标。
- 设置保存在 MySQL，刷新后仍有效。医生姓名用于之后新建报告的署名；已生成报告保留原署名。通知开关控制以后是否生成相应类别的站内消息，已有历史保留。
- 套餐、日程和设置写入携带版本号，避免旧页面覆盖新修改；发生版本冲突时请刷新后重新操作。
- 套餐、日程和设置操作记录在“系统设置 → 操作记录”查看。套餐下架保留历史预约中的套餐名称快照。

演示数据包含9款套餐、24条消息（初始3条未读）和执行SQL当天5条日程。统计及分类数量均实时查询；不会硬编码参考图中的28款、56人等展示数据。日程初始显示当前日期，可用日历切换至已有历史预约日期。

## 新增接口

前缀 `/tijiancms/doctor/workspace`，开发前端通过 `/api/doctor/workspace` 访问，返回 `{code,message,data}`。

| 方法 | 路径 | 参数或说明 |
|---|---|---|
| GET | `/packages` | `category,gender,price(low/middle/high),feature,keyword,sort(priceAsc/priceDesc/newest),enabled,page,pageSize` |
| GET | `/packages/{id}` | 含特色标签、检查项目、版本号的套餐详情 |
| POST | `/packages` | 新建套餐，`name,category,audience,gender,price,originalPrice,features[],items[],picture,enabled,sortOrder` |
| PUT | `/packages/{id}` | 同新增，另需 `version` |
| PUT | `/packages/{id}/status` | `{enabled:0或1,version}` |
| GET | `/schedule` | `date(yyyy-MM-dd),view(day/week/month)`；返回events、月标记、选中日统计、昨日统计、未处理异常和排班规则 |
| PUT | `/schedule/{id}` | `version,scheduleDate,startTime,endTime,status,room,notes` |
| PUT | `/schedule/batch` | `events:[{id,version}],targetDate,offsetMinutes`；最多100条 |
| GET | `/notifications` | `category,keyword,isRead(0/1),range(month/3months/空),page,pageSize` |
| GET | `/notifications/summary` | 总数、已读、未读、分类统计 |
| GET | `/notifications/{id}` | 消息详情 |
| PUT | `/notifications/{id}/read` | `{read:true或false}` |
| PUT | `/notifications/read-all` | 全部标记已读 |
| GET | `/settings` | 四个设置分组、version、updatedAt |
| PUT | `/settings/{section}` | section为profile/preferences/notifications/rules；请求 `{version,values:{...}}` |
| GET | `/operation-logs` | `page,pageSize` |
| POST | `/mobile-sync` | 将历史 `users/orders` 数据幂等补同步到医生端患者、预约、日程和通知 |

新增表：`doctor_package`、`doctor_schedule`、`doctor_schedule_day`（排期并发锁）、`doctor_notification`、`doctor_settings`、`doctor_operation_log`。继续复用前四模块的患者、预约、报告、异常表；手机端注册、预约、取消和归档通过共享后端实时同步，历史订单可幂等补同步。

验证：根目录 `./mvnw.cmd test`，前端目录 `npm run build`。新增16项集成测试覆盖套餐价格与适用人群、上下架、排期联动与冲突、批量调整回滚及容量、通知已读与偏好、设置版本校验、操作记录和HTTP接口契约，测试数据使用事务回滚。
