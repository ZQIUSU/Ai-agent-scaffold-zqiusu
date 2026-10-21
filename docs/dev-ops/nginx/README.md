# AI Agent 静态工作空间

`html/login.html` 为登录页，`html/index.html` 为对话页，无需前端构建或外部 CDN。

## 本地访问

在仓库根目录执行：

```sh
python3 -m http.server 8080 --directory docs/dev-ops/nginx/html
```

打开 `http://127.0.0.1:8080/login.html`，使用 `admin / admin` 登录。也可以将 Nginx 的 `root` 指向 `html` 目录，并将 `index` 配置为 `index.html`。Cookie 需要通过 HTTP 或 HTTPS 访问才能正常保存。

在 `html/js/config.js` 中设置 `apiBaseUrl`，默认 `http://127.0.0.1:8091`。这是浏览器访问的地址：远程部署时应填写实际可访问的服务地址，HTTPS 页面应使用 HTTPS 后端或同源反向代理。

## 接口与会话

- 智能体列表：`GET /api/v1/query_ai_agent_config_list`。
- 首次发送时创建会话：`POST /api/v1/create_session`，JSON 包含 `agentId` 和登录账号对应的 `userId`，返回的 `sessionId` 保存在当前对话中。
- 获取回答及后续追问：`POST /api/v1/chat`，表单包含 `agentId`、`userId`、当前对话的 `sessionId` 和 `message`。已有会话时不再调用创建会话接口。
- 创建会话接口新增了 POST 支持，保留 GET + JSON 请求体的原有调用方式；更新后需要重启 Java 应用。

前端首次获取 `sessionId` 后将其保存在当前对话中，追问直接调用 `chat`，避免每条消息前重复请求 `create_session`。切换智能体后会重新获取相应的会话 ID，点击左侧已有对话后继续复用其保存的 ID。

后端保留原有 `userSessions` 和 `SessionKey`，按 `agentId + userId` 缓存会话；重复请求 `create_session` 会返回已有 ID。“开启新对话”仅开启新的页面记录分组，同一用户与同一智能体仍共享后端上下文，不保证生成新 ID 或隔离上下文。

如果获取会话失败，重试会再次请求 `create_session`；如果会话已获取但回答失败，重试只调用 `chat` 并复用原 ID。刷新页面会清空页面内存中的记录与 ID，下次发送重新获取后端缓存的会话。后端目前使用内存缓存和内存会话，重启服务会清空它们。

普通文本、Markdown 标题、列表、加粗、代码块及 HTTP(S) 链接会安全地渲染；原始 HTML 作为文本展示。页面使用普通对话接口，不依赖 SSE。

## 登录与记录

体验登录在 `js/auth.js` 中校验默认账号，仅在 Cookie 中保存账号、用户 ID 和有效期，不保存密码。启用“保持登录 7 天”会设置 7 天有效期，否则使用浏览器会话 Cookie，并限制最长 24 小时。旧版登录页的 Cookie 可继续使用。

未登录、Cookie 过期或损坏时，对话页跳转到登录页；退出登录会清除 Cookie。此实现用于本地体验，正式身份校验与 HttpOnly 会话应由后端提供。

对话列表保留在当前页面内存中，最多展示 20 个对话，不向本地存储写入聊天内容；刷新后重新开始。发送时 Enter 提交，Shift + Enter 换行，中文输入法确认文字不会触发发送。“停止等待”终止浏览器等待，不保证取消服务端模型执行。
