(() => {
  'use strict';
  const COOKIE_NAME = 'ai_agent_session';
  const DAY = 86400000;
  const secure = location.protocol === 'https:' ? '; Secure' : '';

  function logout() {
    document.cookie = COOKIE_NAME + '=; Path=/; Max-Age=0; SameSite=Lax' + secure;
  }

  function readSession() {
    try {
      const item = document.cookie.split(';').map(value => value.trim())
        .find(value => value.startsWith(COOKIE_NAME + '='));
      if (!item) return null;
      const session = JSON.parse(decodeURIComponent(item.slice(COOKIE_NAME.length + 1)));
      if (session.username === 'admin' && Number.isFinite(session.expiresAt) && session.expiresAt > Date.now()) {
        // 兼容早期登录页仅保存 username 的 Cookie，统一使用登录账号作为 userId。
        return { ...session, userId: session.username };
      }
      logout();
    } catch (_) { /* Cookie 被禁用、损坏或不可访问时，视为未登录。 */ }
    return null;
  }

  function login(username, password, remember) {
    if (username.trim() !== 'admin' || password !== 'admin') return null;
    const session = { username: 'admin', userId: 'admin', issuedAt: Date.now(), expiresAt: Date.now() + (remember ? 7 : 1) * DAY, remember };
    const lifetime = remember ? '; Max-Age=604800' : '';
    // 体验登录只存账号与有效期，不保存密码。正式鉴权应改为服务端签发 HttpOnly Cookie。
    document.cookie = COOKIE_NAME + '=' + encodeURIComponent(JSON.stringify(session)) + '; Path=/; SameSite=Lax' + lifetime + secure;
    if (!readSession()) throw new Error('无法保存登录状态，请允许 Cookie 并通过 HTTP 或 HTTPS 访问此页面。');
    return session;
  }

  function requireSession() {
    const session = readSession();
    if (!session) {
      document.documentElement.dataset.authState = 'checking';
      location.replace('login.html');
    }
    return session;
  }

  window.AgentAuth = Object.freeze({ readSession, login, logout, requireSession });
})();
