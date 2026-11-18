# 熙心健康体检系统

用户端和医生端使用同一个 Spring Boot 后端，预约状态、套餐和体检报告双向联动。

| 目录 | 用途 |
| --- | --- |
| `mobile-client/xxhealth-fronted` | 用户端 Vue 前端，端口 8080 |
| `xxhealth-fronted` | 医生端 Vue 前端，端口 8081 |
| `src`、`pom.xml` | 两端共用的后端，端口 8088 |
| `sql` | 数据库结构、升级脚本及演示数据 |

`mobile-client/src` 是保留的早期用户端后端源码。运行联动版本时只启动根目录后端。

## 首次运行

准备 JDK 17、Node.js/npm 和 MySQL 8。Maven 可使用仓库自带的 `mvnw.cmd`。

在项目根目录打开 MySQL 客户端：

```powershell
mysql --default-character-set=utf8mb4 -u root -p
```

首次创建空数据库时依次执行：

```sql
SOURCE sql/init.sql;
SOURCE sql/seed.sql;
SOURCE sql/doctor_workspace.sql;
SOURCE sql/doctor_operations.sql;
```

`seed.sql` 会清空并重建用户端演示数据，仅供首次初始化使用。已有数据库升级时先备份，再仅执行两个 `doctor_*.sql` 脚本。

后端默认连接本机 `health_check_system` 数据库，账号 `root`，演示密码 `123456`。可通过 `XXHEALTH_DB_URL`、`XXHEALTH_DB_USERNAME`、`XXHEALTH_DB_PASSWORD` 环境变量覆盖。

在 PowerShell 中安装两个前端的依赖：

```powershell
npm.cmd --prefix mobile-client/xxhealth-fronted ci
npm.cmd --prefix xxhealth-fronted ci
```

双击 `Start-Demo.cmd`，自动构建缺失的后端 JAR、启动三个服务并打开两个端。双击 `Stop-Demo.cmd` 停止由脚本启动的服务。更多启动选项见 [DEMO.md](DEMO.md)。

## 手动启动

分别在三个终端运行：

```powershell
.\mvnw.cmd spring-boot:run
```

```powershell
npm.cmd --prefix mobile-client/xxhealth-fronted run serve
```

```powershell
npm.cmd --prefix xxhealth-fronted run serve
```

- 用户端：<http://127.0.0.1:8080/#/login>
- 医生端：<http://127.0.0.1:8081/#/doctor/dashboard>
- 后端：<http://127.0.0.1:8088/tijiancms>

用户可注册账号或使用 `sql/seed.sql` 中的演示账号，密码为 `123456`。

## 验证

数据库初始化并启动后执行：

```powershell
.\mvnw.cmd verify
npm.cmd --prefix mobile-client/xxhealth-fronted run build
npm.cmd --prefix xxhealth-fronted run build
```

后端集成测试涵盖医生工作台、运营模块及用户端预约和报告联动。
