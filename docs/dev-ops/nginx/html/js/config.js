// 修改此处即可切换后端服务地址；部署到其他机器时请填写浏览器可访问的地址。
window.APP_CONFIG = Object.freeze({
  apiBaseUrl: 'http://127.0.0.1:8091',
  endpoints: Object.freeze({
    agents: '/api/v1/query_ai_agent_config_list',
    createSession: '/api/v1/create_session',
    chat: '/api/v1/chat'
  }),
  requestTimeoutMs: 180000,
  maxMessageLength: 10000
});
