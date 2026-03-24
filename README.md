# SpringBootJDK17Demo

> 基于 Spring Boot 3.5.12 + JDK 17 的 Demo 项目，使用 Maven 构建。

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
| 打包方式 | Jar |
| 配置格式 | YAML |

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+（或使用项目自带的 `mvnw`）

### 克隆项目

```bash
git clone git@github.com:LeopardStack/JavaSpringBootNewProjects.git
cd SpringBootJDK17Demo
```

### 运行项目

```bash
# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run

# Windows 使用
mvnw.cmd spring-boot:run

# 或直接使用 Maven
mvn spring-boot:run
```

### 打包

```bash
mvn clean package
java -jar target/SpringBootJDK17Demo-0.0.1-SNAPSHOT.jar
```

---

## 📦 依赖说明

| 依赖 | 说明 |
|------|------|
| `spring-boot-starter-web` | Web 开发核心，内嵌 Tomcat |
| `spring-boot-devtools` | 热重载，开发时自动重启（runtime） |
| `lombok` | 简化 Java 代码，自动生成 getter/setter 等 |
| `spring-boot-starter-test` | 单元测试支持（JUnit 5） |

---

## 📁 项目结构

```
SpringBootJDK17Demo/
├── src/
│   ├── main/
│   │   ├── java/com/scnu/springbootjdk17demo/
│   │   │   └── SpringBootJdk17DemoApplication.java   # 启动类
│   │   └── resources/
│   │       └── application.yaml                      # 配置文件
│   └── test/
│       └── java/com/scnu/springbootjdk17demo/
│           └── SpringBootJdk17DemoApplicationTests.java
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── pom.xml
├── mvnw
├── mvnw.cmd
└── .gitignore
```

---

## ⚙️ Maven 配置（settings.xml）

本项目配置了阿里云镜像加速，解决国内访问 Maven 中央库慢的问题。

将以下配置保存为 `~/.m2/settings.xml`（Windows: `C:\Users\你的用户名\.m2\settings.xml`）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">

  <mirrors>

    <!-- 默认 HTTP 拦截器（保留） -->
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>

    <!-- 阿里云 central 镜像：代理 Maven 中央仓库 -->
    <mirror>
      <id>aliYunMaven</id>
      <name>aliyun maven</name>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>

    <!-- 阿里云 public 镜像：聚合 central + jcenter -->
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>

    <!-- 清华大学镜像 -->
    <mirror>
      <id>tsinghuaUniversityMaven</id>
      <name>tsinghuaUniversity Maven</name>
      <mirrorOf>external:http:*</mirrorOf>
      <url>https://repo.maven.apache.org/maven2/</url>
    </mirror>

    <!-- 华为镜像 -->
    <mirror>
      <id>huaWeiMaven</id>
      <name>huaWei Maven</name>
      <mirrorOf>external:http:*</mirrorOf>
      <url>https://repo.huaweicloud.com/repository/maven/</url>
    </mirror>

  </mirrors>

</settings>
```

> **说明：** `aliyunmaven`（`mirrorOf=*`）会拦截所有仓库请求，优先级最高，适合国内网络环境。

---

## 🔧 Git 提交流程

```bash
# 首次推送（已完成）
git init
git add .
git commit -m "Initial Spring Boot project"
git remote add origin git@github.com:LeopardStack/JavaSpringBootNewProjects.git
git branch -M main
git push -u origin main

# 后续每次提交
git add .
git commit -m "你的提交说明"
git push
```

> **注意：** 国内访问 GitHub 推荐使用 **SSH 方式**（`git@github.com:...`），比 HTTPS 更稳定。

---

## 📄 License

This project is for learning and demonstration purposes.
