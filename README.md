# Java 代码评测机 (JDK 21)

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://chat.deepseek.com/a/chat/s/LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21-green.svg)](https://jdk.java.net/21/)
[![Docker](https://img.shields.io/badge/Docker-%E6%94%AF%E6%8C%81-2496ED.svg)](https://www.docker.com/)
[![Platform](https://img.shields.io/badge/%E5%B9%B3%E5%8F%B0-Linux%20%7C%20Windows-lightgrey.svg)](https://chat.deepseek.com/a/chat/s/a4c05fb7-659b-4bd6-a1eb-6640f4232a3b)

一个基于 **Azul JDK 21** 的轻量级 Java 代码评测系统，支持安全沙箱环境和跨平台部署。适用于编程竞赛、在线判题系统（OJ）和自动化代码测试场景。暂不支持多测试用例评测

------

## 📦 功能特性

- **多环境部署**：支持 Docker 容器化部署和本地直接运行
- **跨平台兼容**：完美支持 Linux 和 Windows 系统
- **安全沙箱**：基于 SecurityManager 的代码隔离机制
- **资源限制**：精确控制 CPU 时间、内存使用和运行时长
- **灵活配置**：支持自定义评测参数和测试用例格式
- **日志追踪**：详细的运行日志和错误报告系统

------

## 🛠 系统要求

- **JDK 21** ([下载链接](https://www.azul.com/downloads/#downloads-table-zulu))
- **Docker** (可选，仅容器化部署需要)
- **Maven 3.6+** (本地部署需要)
- 操作系统：
  - Linux (推荐 Ubuntu 20.04+)
  - Windows 10/11 或 Windows Server 2019+
- 该代码开发环境为 IntelliJ IDEA 2023.3，Windows 10 企业版，Docker 26.1.3，maven 3.8.4，JDK 21.0.6，Ubuntu 22.04 LTS
------

## � 快速开始

### Docker 部署（推荐）

1. 运行dockerfile构建镜像：

bash
```
# 使用官方的Java镜像作为基础镜像，这里以轻量级的Alpine版本为例
FROM azul/zulu-openjdk:21

# 设置工作目录
WORKDIR /app

# 将本地的JAR包复制到镜像中的/app目录下
COPY shiyi-judger-1.0.0.jar /app/shiyi-judger-1.0.0.jar


# 指定容器启动时执行的命令，使用JAR包运行Java应用程序
ENTRYPOINT ["java", "-jar", "/app/shiyi-judger-1.0.0.jar"]

# 指定暴露的端口
EXPOSE 8090
```

1. 运行容器：

bash
```
docker run -p 89:89 --name shiyi-judger shiyi-judger
```

### 本地部署

1. 克隆仓库：

bash

复制

```
git clone https://github.com/2743305544/JavaCodeSandbox.git
cd java-judge-system
```

1. 构建项目：

bash

复制

```
mvn clean package
```

1. 运行服务：

bash

复制

```
java -jar target/judge-system-1.0.0.jar
```
该评测机提供两种接口
- `/docker/executeCode` 该接口执行时会自动在拉取镜像创建Docker 容器中执行代码，并返回执行结果，每次启动服务时会重新创建容器不再复用之前的容器。该功能可以在平配置文件中关闭
- `/native/executeCode` 该接口会在宿主机本地编译和执行代码，并返回执行结果，每次请求都会重新编译和执行代码，建议把jar包放在docker中运行并挂载端口到宿主机提供沙盒服务。

------

## ⚙️ 配置说明

配置文件位于 `src/main/resources/application.yaml`，关键配置项：


复制

```
spring:
  application:
    name: shiyi-code-sandbox
server:
  port: 8090
docker:
  enable: false # 是否Docker容器执行代码

```

------

## 📚 使用指南

### 提交评测请求

bash

复制

```

{
  "inputList": [
    "demoData"
  ],
  "code": "demoData",
  "language": "demoData",
  "time": 1
}
```

### 响应示例

json

复制

```
{
  "outputList": [
    "demoData"
  ],
  "message": "demoData",
  "status": 1
}
```

------

## 🔒 安全注意事项

1. 生产环境务必启用沙箱模式
2. 建议配合 Docker 的容器资源限制使用
3. 定期更新 JDK 安全补丁
4. 不要暴露服务端口到公网未经授权访问

------

## 🤝 参与贡献

欢迎提交 Issue 和 PR！请遵循以下流程：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/awesome-feature`)
3. 提交修改 (`git commit -m 'Add some feature'`)
4. 推送到分支 (`git push origin feature/awesome-feature`)
5. 创建 Pull Request

------

## 📜 许可证

本项目采用 [MIT 许可证](https://chat.deepseek.com/a/chat/s/LICENSE)

------

## 📞 支持与联系

遇到问题请提交 Issue 或联系：

- 邮箱：[3401187804@qq.com](mailto:your.email@example.com)
- Twitter: @ShiYi99998

------

*为编程教育而生，让代码评测更安全可靠！🚀*
