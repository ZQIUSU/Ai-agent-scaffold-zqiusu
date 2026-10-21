(() => {
  'use strict';
  const user = AgentAuth.requireSession();
  if (!user) return;
  const config = window.APP_CONFIG;
  const $ = id => document.getElementById(id);
  const state = { agents: [], conversations: [], current: null, loading: false, busy: false, controller: null, cancelled: false };
  let toastTimer;

  function node(tag, className, text) {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  }

  function icon(name) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('class', 'icon');
    svg.setAttribute('aria-hidden', 'true');
    const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
    use.setAttribute('href', '#' + name);
    svg.append(use);
    return svg;
  }

  function toast(message) {
    clearTimeout(toastTimer);
    $('toast').textContent = message;
    $('toast').hidden = false;
    toastTimer = setTimeout(() => { $('toast').hidden = true; }, 3000);
  }

  function setServiceStatus(text, failed = false) {
    $('service-status').classList.toggle('error', failed);
    $('service-status').lastElementChild.textContent = text;
  }

  function selectedAgent() {
    return state.agents.find(agent => agent.agentId === $('agent-select').value);
  }

  function updateControls() {
    const ready = Boolean(selectedAgent()) && !state.loading;
    $('agent-select').disabled = state.loading || state.busy || !state.agents.length;
    $('reload-agents').disabled = state.loading || state.busy;
    $('new-chat').disabled = state.busy;
    $('message-input').disabled = !ready;
    $('send-button').disabled = !ready || state.busy || !$('message-input').value.trim();
    $('stop-button').hidden = !state.busy;
    document.querySelectorAll('.history-item').forEach(button => { button.disabled = state.busy; });
    if (!state.busy) $('composer-status').textContent = ready ? selectedAgent().agentName + ' · 已就绪' : '选择一个智能体，开始对话';
    $('chat-form').setAttribute('aria-busy', String(state.busy));
  }

  function adjustInput() {
    const input = $('message-input');
    input.style.height = 'auto';
    input.style.height = Math.min(input.scrollHeight, matchMedia('(max-width:760px)').matches ? 110 : 150) + 'px';
    $('character-count').textContent = input.value.length + ' / ' + config.maxMessageLength;
    $('character-count').hidden = input.value.length < config.maxMessageLength * .8;
    updateControls();
  }

  async function request(endpoint, options = {}) {
    if (!AgentAuth.requireSession()) throw new Error('登录已过期，请重新登录。');
    let response;
    try {
      response = await fetch(config.apiBaseUrl.replace(/\/$/, '') + endpoint, { ...options, credentials: 'omit', cache: 'no-store' });
    } catch (error) {
      if (error.name === 'AbortError' || options.signal?.aborted) throw error;
      throw new Error('暂时无法连接智能体服务，请确认后端服务已启动后重试。');
    }
    if (!response.ok) {
      if (response.status === 405 && endpoint === config.endpoints.createSession) throw new Error('创建会话接口需要更新，请重启更新后的后端服务再试。');
      throw new Error('请求未成功（HTTP ' + response.status + '），请稍后重试。');
    }
    let result;
    try { result = await response.json(); } catch (_) { throw new Error('服务返回的数据格式不正确，请稍后重试。'); }
    if (!result || typeof result !== 'object') throw new Error('服务返回的数据格式不正确，请稍后重试。');
    if (String(result.code) !== '0000') throw new Error(result.info || '智能体服务暂时不可用，请稍后重试。');
    return result.data;
  }

  async function loadAgents() {
    if (state.loading || state.busy) return;
    state.loading = true;
    $('agent-error').hidden = true;
    setServiceStatus('正在连接');
    updateControls();
    const previous = $('agent-select').value;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    try {
      const data = await request(config.endpoints.agents, { signal: controller.signal });
      if (!Array.isArray(data)) throw new Error('智能体列表格式不正确，请检查服务配置。');
      state.agents = data.filter(agent => agent && agent.agentId != null && typeof agent.agentName === 'string')
        .map(agent => ({ agentId: String(agent.agentId), agentName: agent.agentName, agentDesc: typeof agent.agentDesc === 'string' ? agent.agentDesc : '' }));
      if (!state.agents.length) throw new Error('暂时没有可用智能体，请配置智能体后重新加载。');
      $('agent-select').replaceChildren(...state.agents.map(agent => {
        const option = node('option', '', agent.agentName);
        option.value = agent.agentId;
        return option;
      }));
      if (state.agents.some(agent => agent.agentId === previous)) $('agent-select').value = previous;
      updateAgentDescription();
      setServiceStatus('服务已连接');
    } catch (error) {
      state.agents = [];
      const option = node('option', '', '暂无可用智能体');
      option.value = '';
      $('agent-select').replaceChildren(option);
      $('agent-error-text').textContent = controller.signal.aborted ? '连接超时，请确认服务已启动后重试。' : error.message;
      $('agent-error').hidden = false;
      setServiceStatus('连接异常', true);
    } finally {
      clearTimeout(timer);
      state.loading = false;
      updateControls();
    }
  }

  function updateAgentDescription() {
    const agent = selectedAgent();
    $('agent-description').textContent = agent?.agentDesc && agent.agentDesc !== agent.agentName
      ? agent.agentDesc : '从一个问题到一个计划，你的 AI 智能体随时准备与你协作。';
    updateControls();
  }

  // 只创建文本节点和明确允许的格式元素；模型输出中的 HTML 不会被执行。
  function appendInline(parent, text) {
    const pattern = /(`[^`\n]+`|\*\*[^*\n]+\*\*|\[[^\]\n]+\]\(https?:\/\/[^\s)]+\))/g;
    let offset = 0;
    for (const match of text.matchAll(pattern)) {
      parent.append(document.createTextNode(text.slice(offset, match.index)));
      const token = match[0];
      if (token.startsWith('`')) parent.append(node('code', '', token.slice(1, -1)));
      else if (token.startsWith('**')) parent.append(node('strong', '', token.slice(2, -2)));
      else {
        const parts = token.match(/^\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)$/);
        const link = node('a', '', parts[1]);
        link.href = parts[2];
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        parent.append(link);
      }
      offset = match.index + token.length;
    }
    parent.append(document.createTextNode(text.slice(offset)));
  }

  function renderMarkdown(text, target) {
    const lines = text.replace(/\r\n/g, '\n').split('\n');
    let paragraph = [], list = null;
    const flush = () => {
      if (paragraph.length) {
        const p = node('p');
        appendInline(p, paragraph.join('\n'));
        target.append(p);
        paragraph = [];
      }
    };
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      if (/^\s*```/.test(line)) {
        flush(); list = null;
        const code = [];
        while (++i < lines.length && !/^\s*```/.test(lines[i])) code.push(lines[i]);
        const pre = node('pre');
        pre.append(node('code', '', code.join('\n')));
        target.append(pre);
      } else if (/^#{1,6}\s+/.test(line)) {
        flush(); list = null;
        const heading = node(line.startsWith('###') ? 'h4' : 'h3');
        appendInline(heading, line.replace(/^#{1,6}\s+/, ''));
        target.append(heading);
      } else if (/^\s*(?:[-*+]\s+|\d+[.)]\s+)/.test(line)) {
        flush();
        const type = /^\s*\d/.test(line) ? 'ol' : 'ul';
        if (!list || list.tagName.toLowerCase() !== type) { list = node(type); target.append(list); }
        const item = node('li');
        appendInline(item, line.replace(/^\s*(?:[-*+]\s+|\d+[.)]\s+)/, ''));
        list.append(item);
      } else if (!line.trim()) { flush(); list = null; }
      else { list = null; paragraph.push(line); }
    }
    flush();
  }

  function renderMessage(message) {
    const article = node('article', 'message ' + message.role + (message.error ? ' message-error' : ''));
    const meta = node('div', 'message-meta');
    const avatar = node('span', 'message-avatar', message.role === 'user' ? 'A' : undefined);
    if (message.role === 'assistant') avatar.append(icon('spark'));
    avatar.setAttribute('aria-hidden', 'true');
    const time = node('time', 'message-time', new Date(message.time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }));
    time.dateTime = new Date(message.time).toISOString();
    meta.append(avatar, node('span', '', message.role === 'user' ? '你' : message.agentName), time);
    const bubble = node('div', 'message-bubble');
    if (message.pending) {
      const pending = node('div', 'pending');
      const dots = node('span', 'typing-dots');
      dots.setAttribute('aria-hidden', 'true');
      dots.append(node('i'), node('i'), node('i'));
      pending.append(dots, node('span', '', message.content));
      bubble.append(pending);
    } else if (message.role === 'assistant' && !message.error) renderMarkdown(message.content, bubble);
    else bubble.textContent = message.content;
    article.append(meta, bubble);
    if (message.role === 'assistant' && !message.pending) {
      const tools = node('div', 'message-tools');
      const button = node('button', 'text-button', message.error ? '重试' : '复制回答');
      button.type = 'button';
      if (message.error) button.addEventListener('click', () => {
        if (!state.busy) requestReply(message, state.current);
      });
      else {
        button.prepend(icon('copy'));
        button.addEventListener('click', async () => {
          try { await navigator.clipboard.writeText(message.content); toast('回答已复制'); }
          catch (_) { toast('当前浏览器不支持复制，请选择文本后复制。'); }
        });
      }
      tools.append(button);
      article.append(tools);
    }
    return article;
  }

  function renderHistory() {
    $('conversation-list').replaceChildren();
    if (!state.conversations.length) {
      $('conversation-list').append(node('p', 'history-empty', '一个好想法，\n从一句对话开始。'));
      return;
    }
    state.conversations.forEach(conversation => {
      const button = node('button', 'history-item' + (conversation === state.current ? ' active' : ''));
      button.type = 'button';
      button.disabled = state.busy;
      button.title = conversation.title;
      button.append(node('strong', '', conversation.title), node('span', '', conversation.agentName));
      button.setAttribute('aria-current', conversation === state.current ? 'true' : 'false');
      button.addEventListener('click', () => {
        if (state.busy) return;
        state.current = conversation;
        if (state.agents.some(agent => agent.agentId === conversation.agentId)) $('agent-select').value = conversation.agentId;
        updateAgentDescription();
        renderConversation();
        renderHistory();
        closeSidebar();
      });
      $('conversation-list').append(button);
    });
  }

  function renderConversation() {
    const messages = state.current?.messages || [];
    $('welcome').hidden = Boolean(messages.length);
    $('messages').hidden = !messages.length;
    $('messages').replaceChildren();
    if (messages.length) {
      $('messages').append(node('p', 'conversation-date', '今天 · 与 AI 一起，把想法向前推进'));
      messages.forEach(message => $('messages').append(renderMessage(message)));
    }
    requestAnimationFrame(() => { $('conversation-scroll').scrollTop = $('conversation-scroll').scrollHeight; });
  }

  async function requestReply(reply, conversation) {
    if (state.busy || !conversation || !AgentAuth.requireSession()) return;
    state.busy = true;
    state.cancelled = false;
    state.controller = new AbortController();
    const controller = state.controller;
    const timer = setTimeout(() => controller.abort(), config.requestTimeoutMs);
    reply.pending = true;
    reply.error = false;
    reply.content = conversation.sessionId ? '正在思考你的问题…' : '正在创建会话…';
    renderConversation();
    updateControls();
    renderHistory();
    $('composer-status').textContent = '正在准备这次对话';
    try {
      // 每段对话仅在首次发送时创建会话；追问和已有会话中的重试复用该 ID。
      if (!conversation.sessionId) {
        const session = await request(config.endpoints.createSession, {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ agentId: conversation.agentId, userId: user.userId }), signal: controller.signal
        });
        if (!session || typeof session.sessionId !== 'string' || !session.sessionId) throw new Error('未获取到有效会话，请重试。');
        conversation.sessionId = session.sessionId;
      }
      reply.content = '正在思考你的问题…';
      $('composer-status').textContent = reply.agentName + ' · 正在思考';
      renderConversation();
      // 当前 Controller 的 chat 使用表单参数绑定，保持现有接口兼容。
      const result = await request(config.endpoints.chat, {
        method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: new URLSearchParams({ agentId: conversation.agentId, userId: user.userId, sessionId: conversation.sessionId, message: reply.question }), signal: controller.signal
      });
      if (!result || typeof result.content !== 'string' || !result.content.trim()) throw new Error('智能体暂未返回内容，请重试或换个问题。');
      reply.content = result.content;
    } catch (error) {
      reply.error = true;
      reply.content = state.cancelled ? '已停止等待这次回答。你可以重试，或继续提出新的问题。'
        : controller.signal.aborted ? '等待回答超时，请稍后重试。' : error.message;
    } finally {
      clearTimeout(timer);
      reply.pending = false;
      state.busy = false;
      state.controller = null;
      if (AgentAuth.requireSession()) {
        renderConversation();
        updateControls();
        renderHistory();
      }
    }
  }

  function sendMessage() {
    const question = $('message-input').value.trim();
    const agent = selectedAgent();
    if (!question || !agent || state.busy || state.loading || !AgentAuth.requireSession()) return;
    if (question.length > config.maxMessageLength) return toast('问题过长，请缩短后再发送。');
    if (!state.current || state.current.agentId !== agent.agentId) {
      state.current = { title: question, agentId: agent.agentId, agentName: agent.agentName, sessionId: null, messages: [] };
      state.conversations.unshift(state.current);
      if (state.conversations.length > 20) state.conversations.pop();
    }
    const time = Date.now();
    state.current.messages.push({ role: 'user', content: question, time });
    const reply = { role: 'assistant', agentId: agent.agentId, agentName: agent.agentName, question, time, content: '', pending: true };
    state.current.messages.push(reply);
    $('message-input').value = '';
    adjustInput();
    requestReply(reply, state.current);
  }

  function closeSidebar() {
    $('sidebar').classList.remove('open');
    $('sidebar-scrim').hidden = true;
    $('menu-toggle').setAttribute('aria-expanded', 'false');
    if (matchMedia('(max-width:760px)').matches) $('sidebar').inert = true;
  }

  $('user-name').textContent = user.username;
  $('welcome-user').textContent = user.username;
  $('message-input').maxLength = config.maxMessageLength;
  $('chat-form').addEventListener('submit', event => { event.preventDefault(); sendMessage(); });
  $('message-input').addEventListener('input', adjustInput);
  $('message-input').addEventListener('keydown', event => {
    if (event.key === 'Enter' && !event.shiftKey && !event.isComposing && event.keyCode !== 229) { event.preventDefault(); sendMessage(); }
  });
  $('stop-button').addEventListener('click', () => { state.cancelled = true; state.controller?.abort(); });
  $('reload-agents').addEventListener('click', loadAgents);
  $('retry-agents').addEventListener('click', loadAgents);
  $('agent-select').addEventListener('change', () => {
    if (state.current && state.current.agentId !== $('agent-select').value) {
      state.current = null;
      renderConversation();
      renderHistory();
    }
    updateAgentDescription();
  });
  $('new-chat').addEventListener('click', () => {
    if (state.busy) return;
    state.current = null;
    $('message-input').value = '';
    adjustInput();
    renderConversation();
    renderHistory();
    closeSidebar();
    $('message-input').focus();
  });
  document.querySelectorAll('.suggestion').forEach(button => button.addEventListener('click', () => {
    if (state.busy || !selectedAgent()) return;
    $('message-input').value = button.dataset.prompt;
    adjustInput();
    $('message-input').focus();
  }));
  $('logout').addEventListener('click', () => {
    state.controller?.abort();
    AgentAuth.logout();
    location.replace('login.html');
  });
  $('menu-toggle').addEventListener('click', () => {
    const open = !$('sidebar').classList.contains('open');
    $('sidebar').classList.toggle('open', open);
    $('sidebar').inert = !open;
    $('sidebar-scrim').hidden = !open;
    $('menu-toggle').setAttribute('aria-expanded', String(open));
  });
  $('sidebar-scrim').addEventListener('click', closeSidebar);
  document.addEventListener('keydown', event => { if (event.key === 'Escape') closeSidebar(); });
  matchMedia('(max-width:760px)').addEventListener('change', event => { closeSidebar(); $('sidebar').inert = event.matches; });
  window.addEventListener('pageshow', () => { AgentAuth.requireSession(); });
  document.addEventListener('visibilitychange', () => { if (!document.hidden) AgentAuth.requireSession(); });
  $('sidebar').inert = matchMedia('(max-width:760px)').matches;
  loadAgents();
})();
