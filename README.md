# Ai-agent-scaffold-zqiusu

基于 **DDD 分层架构** 的 AI Agent 应用脚手架，集成 **Spring AI**、**LangChain4j** 与 **Google ADK**，用于快速搭建生产级 AI 智能体应用。

> 本项目继承自 [小傅哥 xfg-frame-archetype](https://bugstack.cn/) 的 DDD 工程模板，在此之上引入 AI Agent 能力栈。

---

## ✨ 特性

- 🧱 **DDD 分层架构**：api / app / domain / infrastructure / trigger / types 六层清晰拆分
- 🤖 **多 AI 框架并行支持**
  - [Spring AI](https://docs.spring.io/spring-ai/reference/) `1.1.0-M3`
  - [LangChain4j](https://docs.langchain4j.dev/) `1.4.0`
  - [Google ADK (Agent Development Kit)](https://github.com/google/adk) `0.4.0`
- 🛠️ **覆盖三类核心能力测试**：Chat Model / Tool Calling / Multi-Agent（Sequential / Parallel / Loop）
- 🐳 **开箱即用的运维脚本**：MySQL、应用部署、API 调用样例（`docs/dev-ops/`）
- ☕ **JDK 17 + Spring Boot** 技术栈

---

## 📦 工程结构

```
Ai-agent-scaffold-zqiusu
├── Ai-agent-scaffold-zqiusu-api            # 接口定义层（对外契约）
├── Ai-agent-scaffold-zqiusu-app            # 应用启动层（Spring Boot 主程序、配置、测试）
├── Ai-agent-scaffold-zqiusu-domain         # 领域层（业务模型与领域服务）
├── Ai-agent-scaffold-zqiusu-infrastructure # 基础设施层（持久化、外部资源适配）
├── Ai-agent-scaffold-zqiusu-trigger        # 触发器层（HTTP / MQ / Job 入口）
├── Ai-agent-scaffold-zqiusu-types          # 通用类型层（枚举、常量、Response 包装）
└── docs/
    └── dev-ops/                            # 运维脚本：app / mysql / api
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8（可选，按需）

### 构建

```bash
mvn clean install -DskipTests
```

### 启动应用

```bash
mvn -pl Ai-agent-scaffold-zqiusu-app spring-boot:run
```

默认使用 `dev` profile，配置位于 `Ai-agent-scaffold-zqiusu-app/src/main/resources/application-dev.yml`。

---

## 🧪 AI 能力示例

测试代码位于 `Ai-agent-scaffold-zqiusu-app/src/test/java/site/zqiusu/test/api/`：

| 分类 | 文件 | 说明 |
|------|------|------|
| **Chat Model** | `model/SpringAiApiTest.java` | Spring AI 调用 OpenAI 兼容接口 |
| | `model/LangChain4jApiTest.java` | LangChain4j 调用 OpenAI 兼容接口 |
| **Tool Calling** | `tools/SpringAiToolTest.java` | Spring AI 工具调用 |
| | `tools/LangChain4jToolTest.java` | LangChain4j 工具调用 |
| **Multi-Agent** | `agent/SequentialAgentTest.java` | Google ADK — 顺序编排 |
| | `agent/ParallelAgentTest.java` | Google ADK — 并行编排 |
| | `agent/LoopAgentTest.java` | Google ADK — 循环编排 |

原始 HTTP 调用样例见 [`docs/dev-ops/api/curl.sh`](docs/dev-ops/api/curl.sh)。

---

## 📚 参考资料

- DDD 概念理论：<https://bugstack.cn/md/road-map/ddd-guide-01.html>
- DDD 建模方法：<https://bugstack.cn/md/road-map/ddd-guide-02.html>
- DDD 工程模型：<https://bugstack.cn/md/road-map/ddd-guide-03.html>
- DDD 架构设计：<https://bugstack.cn/md/road-map/ddd.html>
- DDD 建模案例：<https://bugstack.cn/md/road-map/ddd-model.html>
- Docker 使用文档：<https://bugstack.cn/md/road-map/docker.html>

---

## 📄 License

本项目用于学习与脚手架参考，欢迎 Fork / Star。
