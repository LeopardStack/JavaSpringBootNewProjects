# 教务管理系统 · 后端

> 基于 **Spring Boot 3 + JDK 17** 的教务管理系统后端，配套前端仓库 [my-first-vue](https://github.com/LeopardStack/my-first-vue)。

---

## 📌 项目信息

| 项目 | 内容 |
|------|------|
| Group ID | `com.scnu` |
| Artifact ID | `SpringBootJDK17Demo` |
| Version | `0.0.1-SNAPSHOT` |
| Spring Boot | `3.5.12` |
| Java 版本 | `17` |
| 构建工具 | Maven |
| 数据库 | PostgreSQL 15（主从读写分离） |

---

## ✨ 功能特性

- 🐘 **PostgreSQL 主从读写分离**：写库（primary）+ 读库（replica）
- 🔀 **动态数据源路由**：`AbstractRoutingDataSource` + `ThreadLocal`，Service 层手动切换
- 🔐 **JWT 无状态认证**：登录拿 token，后续请求 `Authorization: Bearer xxx`
- 👥 **RBAC 三层权限模型**：用户 → 角色 → 权限
- 🛡️ Spring Security 6 + BCrypt 密码加密
- 🚀 MyBatis-Plus 简化 CRUD
- 📊 Spring Boot Actuator 健康检查

---

## 🏗️ 架构

```text
┌─────────────┐         ┌────────────────────────────┐
│  Vue Front  │ ──HTTP──▶│  Spring Boot (Port 8080)  │
└─────────────┘         │  ├─ JwtAuthFilter          │
                        │  ├─ SecurityConfig         │
                        │  └─ RoutingDataSource      │
                        └──────────┬─────────────────┘
                              ┌────┴────┐
                       Write  │         │  Read
                              ▼         ▼
                        ┌─────────┐ ┌─────────┐
                        │ Primary │ │ Replica │
                        │  .103   │ │  .104   │
                        │  :5432  │ │  :5432  │
                        └─────────┘ └─────────┘
                          流复制同步
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+（或使用项目自带 `mvnw`）
- PostgreSQL 15 主从已搭建（详见底部"环境参考"）

### 数据库初始化

在**主库**执行建表脚本（脚本见 `db/schema.sql`），数据会通过流复制自动同步到从库。

脚本包含：

| 表 | 用途 |
|----|------|
| `sys_user` | 用户表 |
| `sys_role` | 角色表（admin / teacher / student） |
| `sys_permission` | 权限表（菜单 + 按钮统一存储，`type` 字段区分） |
| `sys_user_role` | 用户-角色 关联 |
| `sys_role_permission` | 角色-权限 关联 |

初始化数据包含 3 个测试用户：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `123456` | 管理员（拥有全部权限） |
| `teacher` | `123456` | 教师 |
| `student` | `123456` | 学生 |

### 配置数据库连接

修改 `src/main/resources/application.yaml`：

```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
  datasource:
    primary:
      jdbc-url: jdbc:postgresql://192.168.56.103:5432/appdb
      username: pguser
      password: pgpassword123
      driver-class-name: org.postgresql.Driver
    replica:
      jdbc-url: jdbc:postgresql://192.168.56.104:5432/appdb
      username: pguser
      password: pgpassword123
      driver-class-name: org.postgresql.Driver

app:
  jwt:
    secret: "scnu-secret-key-2026-please-change-in-production-env"
    expiration-ms: 86400000   # 24 小时
```

### 启动

```bash
# 推荐
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# 或直接 Maven
mvn spring-boot:run
```

### 打包

```bash
mvn clean package
java -jar target/SpringBootJDK17Demo-0.0.1-SNAPSHOT.jar
```

---

## 📡 API 文档

### POST `/api/auth/login`

登录拿 token。

**请求：**
```json
{ "username": "admin", "password": "123456" }
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### GET `/api/auth/me`

获取当前用户信息（必须携带 token）。

**请求头：**
```text
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应：**
```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "roles": ["admin"],
  "permissions": ["admin:dashboard", "user:add", "user:edit", "..."],
  "menus": [
    {
      "code": "admin:dashboard",
      "name": "数据总览",
      "path": "/admin/dashboard",
      "icon": "el-icon-data-analysis",
      "children": []
    }
  ]
}
```

---

## 📁 项目结构

```text
src/main/java/com/scnu/springbootjdk17demo/
├── config/
│   ├── BeanConfig.java                # PasswordEncoder (BCrypt) Bean
│   ├── DataSourceConfig.java          # 主从数据源 + RoutingDataSource
│   ├── DataSourceContextHolder.java   # ThreadLocal 路由标记
│   ├── RoutingDataSource.java         # 继承 AbstractRoutingDataSource
│   └── SecurityConfig.java            # Spring Security 配置
├── controller/
│   └── AuthController.java            # /login, /me
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── UserInfoResponse.java          # 含菜单树 MenuVO
├── entity/
│   ├── SysUser.java
│   ├── SysRole.java
│   └── SysPermission.java
├── mapper/
│   ├── SysUserMapper.java             # MyBatis-Plus BaseMapper
│   ├── SysRoleMapper.java             # 含 selectByUserId
│   └── SysPermissionMapper.java       # 含 selectByUserId (JOIN 三表)
├── security/
│   └── JwtAuthFilter.java             # JWT 校验过滤器
├── service/
│   └── UserService.java               # 登录 + loadUserInfo
└── util/
    └── JwtUtil.java                   # JWT 生成 / 解析 / 校验
```

---

## 🔀 读写分离用法

在 Service 方法里通过 `DataSourceContextHolder` 手动切换：

```java
// 查询走从库
DataSourceContextHolder.set("replica");
try {
SysUser user = userMapper.selectOne(...);
        } finally {
        DataSourceContextHolder.clear();   // ⚠️ 一定要清，避免线程复用污染
}

// 写入走主库
        DataSourceContextHolder.set("primary");
try {
        userMapper.updateById(upd);
} finally {
        DataSourceContextHolder.clear();
}
```

> **TODO**：后续可以改造成自定义 `@ReadOnly` 注解 + AOP 切面，自动根据注解切换数据源，避免业务代码侵入。

---

## 🔐 安全设计

### Spring Security 配置要点

- **关闭** CSRF / Form Login / HTTP Basic
- **STATELESS** 会话（不创建 Session）
- `JwtAuthFilter` 在 `UsernamePasswordAuthenticationFilter` 之前执行
- 白名单：`/api/auth/login`、`/actuator/**`
- CORS 允许 `http://localhost:*`，配合前端开发

### JWT 流程

```text
登录    → 校验密码 → 生成 JWT → 返回 token
请求    → JwtAuthFilter 解析 token → 加载用户信息 → 注入 SecurityContext
登出    → 前端清 token（无状态，服务端无需操作）
```

### 权限注入

`JwtAuthFilter` 会把用户的角色和权限码都注入到 Spring Security 的 `Authorities`：

- 角色：以 `ROLE_` 前缀，如 `ROLE_admin`
- 权限：直接是权限码，如 `user:add`

后续可以在 Controller 用 `@PreAuthorize("hasAuthority('user:add')")` 做方法级权限控制。

---

## 📦 主要依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| `spring-boot-starter-web` | 3.5.12 | Web 核心 |
| `spring-boot-starter-security` | 3.5.12 | 安全框架 |
| `spring-boot-starter-actuator` | 3.5.12 | 健康检查 |
| `mybatis-plus-spring-boot3-starter` | 3.5.9 | ORM |
| `postgresql` | runtime | PG 驱动 |
| `jjwt-api / jjwt-impl / jjwt-jackson` | 0.12.6 | JWT |
| `lombok` | provided | 简化代码 |

---

## ⚙️ Maven 镜像配置

国内推荐配置阿里云镜像，加速依赖下载。在 `~/.m2/settings.xml`（Windows: `C:\Users\<用户名>\.m2\settings.xml`）添加：

```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 🐳 环境参考（PG 主从）

本项目在 **VirtualBox + Ubuntu 22.04** 上跑 Docker 容器搭建 PG 主从：

| 节点 | IP | 角色 | 容器 | 端口 |
|------|-----|------|------|------|
| ubuntu-node2 | `192.168.56.103` | Primary | `postgres:15` | 5432 |
| ubuntu-node3 | `192.168.56.104` | Replica | `postgres:15` | 5432 |

两台节点同时跑 `prometheuscommunity/postgres-exporter:latest`（端口 9187）用于 Prometheus 监控。

主从同步通过 **PostgreSQL 流复制** 实现。

---

## 🔧 Git 提交流程

```bash
git add .
git commit -m "你的提交说明"
git push
```

> 国内访问 GitHub 推荐使用 **SSH** 方式（`git@github.com:...`），比 HTTPS 稳定。

---

## 📄 License

For learning and demonstration purposes.