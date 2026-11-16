# 熙心健康体检 · 用户端接口文档 v2（按文档口径）

> 变更说明：从 `/api` + `Result` 改为文档的 `/tijian` 前缀 + **裸返回**。
> 负责人：曲奇饼（账号/医院/套餐）、怿安（预约/报告/日历）

## 通用约定

| 项 | 约定 |
|---|---|
| Base URL | `http://localhost:8080/tijian` |
| 请求方式 | 全部 `POST`，`Content-Type: application/json` |
| 返回 | **裸对象**（实体 / int / List），不再包 `Result` |
| 失败表示 | 返回 `0`、`null` 或 `[]`；参数错误不抛异常 |
| 日期 | `yyyy-MM-dd` |
| 字段 | 小写（`userid`、`smid`、`hpid`、`orderdate`） |
| 前端代理 | `vue.config.js` 把 `/tijian` 代理到 `localhost:8080` |

---

## 1. users（曲奇饼）

### 1.1 登录

`POST /tijian/users/getUsersByUserIdByPass`

```json
{ "userid": "13800000001", "password": "123456" }
```

返回 `Users` 对象；账号或密码错误返回 `null`。

```json
{
  "userid": "13800000001",
  "password": "123456",
  "realname": "张伟",
  "sex": 1,
  "identitycard": "210102199001011234",
  "birthday": "1990-01-01",
  "usertype": 1
}
```

### 1.2 手机号查重

`POST /tijian/users/getUsersById`

请求 `{ "userid": "13800000001" }`，返回 `Users` 或 `null`。

### 1.3 注册

`POST /tijian/users/saveUsers`

```json
{
  "userid": "13800000003",
  "password": "123456",
  "realname": "王强",
  "sex": 1,
  "identitycard": "210102199801011234",
  "birthday": "1998-01-01",
  "usertype": 1
}
```

返回 `int`：`1` 成功，`0` 手机号已存在。

---

## 2. setmeal（曲奇饼）

### 2.1 按类型查套餐

`POST /tijian/setmeal/listSetmealByType`

请求 `{ "type": 1 }`（1 男 / 0 女），返回 `List<Setmeal>`：

```json
[ { "smid": 2, "name": "中青年体检套餐", "type": 1, "price": 599 } ]
```

### 2.2 套餐详情

`POST /tijian/setmeal/getSetmealById`

请求 `{ "smid": 2 }`，返回 `Setmeal`，**额外带 items 检查项列表**：

```json
{
  "smid": 2,
  "name": "中青年体检套餐",
  "type": 1,
  "price": 599,
  "items": [
    { "ciid": 1, "ciname": "一般检查", "cicontent": "身高、体重、血压、心率", "meaning": "了解身体基本指标" }
  ]
}
```

---

## 3. hospital（曲奇饼）

### 3.1 医院列表

`POST /tijian/hospital/listHospital`

请求 `{ "state": 1 }`，返回 `List<Hospital>`。

### 3.2 医院详情

`POST /tijian/hospital/getHospitalById`

请求 `{ "hpid": 1 }`，返回 `Hospital`。

---

## 4. orders（怿安）

### 4.1 是否可预约

`POST /tijian/orders/getOrdersByUserId`

请求 `{ "userid": "13800000001" }`，返回未归档订单数量 `int`（`0` 表示可预约）。

### 4.2 创建预约

`POST /tijian/orders/saveOrders`

```json
{ "userid": "13800000001", "hpid": 1, "smid": 2, "orderdate": "2026-11-10" }
```

返回 `int`：

| 值 | 含义 |
|---|---|
| `1` | 成功 |
| `0` | 已有未归档预约，不能重复预约 |
| `-2` | 预约日期早于今天 |

### 4.3 我的预约

`POST /tijian/orders/listOrdersByUserId`

请求 `{ "userid": "13800000001", "state": null }`（1 未归档 / 2 已归档 / null 全部）

返回 `List<Orders>`，**额外带医院名、套餐名、价格**：

```json
[
  {
    "orderid": 2,
    "orderdate": "2026-10-20",
    "userid": "13800000001",
    "hpid": 1,
    "smid": 2,
    "state": 2,
    "hospitalname": "熙心健康体检中心·和平院区",
    "setmealname": "中青年体检套餐",
    "price": 599
  }
]
```

### 4.4 预约详情

`POST /tijian/orders/getOrdersById`

请求 `{ "orderid": 2 }`，返回单条订单（结构同上），不存在返回 `null`。

### 4.5 取消预约

`POST /tijian/orders/removeOrders`

请求 `{ "orderid": 1 }`

返回 `int`：`1` 成功，`-1` 已归档不能取消，`0` 订单不存在。

---

## 5. 报告（怿安）

### 5.1 总检结论

`POST /tijian/overallResult/listOverallResultByOrderId`

请求 `{ "orderid": 2 }`，返回 `List<Overallresult>`。

### 5.2 检查项与明细

`POST /tijian/ciReport/listCiReport`

请求 `{ "orderid": 2 }`，返回 `List<CiReport>`，每个检查项带 `details`：

```json
[
  {
    "cirid": 6,
    "ciid": 6,
    "ciname": "血脂",
    "orderid": 2,
    "details": [
      {
        "cidrid": 14,
        "name": "总胆固醇",
        "unit": "mmol/L",
        "minrange": 0,
        "maxrange": 5.2,
        "normalvaluestring": "<5.2",
        "type": 1,
        "value": "5.8",
        "iserror": 1,
        "ciid": 6,
        "orderid": 2
      }
    ]
  }
]
```

`iserror = 1` 表示异常。

---

## 6. 日历（怿安）

`POST /tijian/calendar/listAppointmentCalendar`

请求 `{ "hpid": 1, "year": 2026, "month": 11 }`

返回 `List<CalendarResponseDto>`：

```json
[ { "date": "2026-11-06", "total": 20, "used": 1, "remaining": 19, "full": false } ]
```

`total` 从 `hospital.rule` 解析，失败默认 20。

---

## 7. 前端调用变化

```js
import request from '@/utils/request'   // baseURL 已是 /tijian

// 登录：直接拿对象
const res = await request.post('/users/getUsersByUserIdByPass', { userid, password })
if (res.data == null) { /* 账号或密码错误 */ }
const user = res.data

// 我的预约：直接是数组
const res2 = await request.post('/orders/listOrdersByUserId', { userid: user.userid, state: null })
const orders = res2.data
```

**不再有 `res.data.data`，也用不到 `code` / `message`。**

---

## 8. 文件归属

| 模块 | Controller | 负责人 |
|---|---|---|
| users | `UsersController` | 曲奇饼 |
| hospital | `HospitalController` | 曲奇饼 |
| setmeal | `SetmealController` | 曲奇饼 |
| orders | `OrdersController` | 怿安 |
| overallResult | `OverallresultController` | 怿安 |
| ciReport | `CireportController` | 怿安 |
| calendar | `CalendarController` | 怿安 |

---

## 医生端扩展接口（体检报告 / 日程 / 消息 / 设置）

> 统一返回 `Result { code, message, data }`，`code=200` 成功；当前后端 context-path 为 `/tijiancms`。

### schedule（日程管理）

- `POST /tijiancms/schedule/listByMonth`
  - 参数：`{ "year": 2026, "month": 11 }`
  - 返回：当月预约列表，含 `orderid / date / timeSlot / patientName / sex / age / setmealName / state / stateText / abnormalCount`
  - 说明：`timeSlot` 按当天预约顺序生成（orders 暂无时间段字段），后续可加字段替换
- `POST /tijiancms/schedule/dayDetail`
  - 参数：`{ "date": "2026-11-10" }`
  - 返回：`{ date, appointments, checkReminders, abnormalReminders }`
  - 说明：检查提醒取套餐前 3 个检查项；异常提醒取当天预约的 `cidetailedreport.isError=1`

### message（消息通知）

- `POST /tijiancms/message/list`
  - 参数：`{ "type": "预约提醒", "keyword": "", "status": "unread" }`（均可空，status: all/unread/read）
  - 返回：消息列表，按时间倒序
- `POST /tijiancms/message/markRead`
  - 参数：`{ "msgid": 1 }`
- `POST /tijiancms/message/markAllRead`
  - 参数：无
- `POST /tijiancms/message/unreadCount`
  - 参数：无

### doctor（系统设置）

- `POST /tijiancms/doctor/updateDoctor`
  - 参数：`{ "docid": 1, "realname": "张医生", "sex": 1, "deptno": 2 }`
- `POST /tijiancms/doctor/updatePassword`
  - 参数：`{ "docid": 1, "oldPassword": "123456", "newPassword": "654321" }`
  - 说明：原密码错误或新密码少于 6 位时返回 `code=400`

### 数据库变更

- 新增 `message` 表（见 `sql/init.sql` 第 11 节）
- `sql/seed.sql` 已补充：医生账号（D001/123456）、10 条消息、按 `CURDATE()` 生成的 8 条预约及订单 4 的体检报告

### doctor（医生注册）

- `POST /tijiancms/doctor/register`
  - 参数：`{ "doccode": "D002", "realname": "李医生", "phone": "13900000001", "password": "123456" }`
  - 返回：`1` 成功；用户名已存在返回 `code=400`；参数不合法返回 `code=400`
  - 说明：`doctor` 表新增 `phone` 字段（见 `sql/init.sql`），注册时性别默认男、科室默认检验科
