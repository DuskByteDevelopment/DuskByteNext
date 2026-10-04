// ============================================================
// DuskByte Cloud API Worker
// API Base: dbapi.3d3k.org
// D1 Database: duskbyte_cloud_config
//
// ===== 部署步骤 (纯网页端) =====
// 1. Cloudflare Dashboard → Workers & Pages → Create Worker → 名字随便填
// 2. 进入 Worker → Settings → Variables → D1 Database Bindings
//    → Add binding: Variable name = DB, D1 database = duskbyte_cloud_config
//    (如果还没建D1数据库就先去 D1 那里建一个叫 duskbyte_cloud_config)
// 3. 把下面整个代码贴进 Worker 的编辑器 → Deploy
// 4. 部署后先访问: https://你的worker域名/api/init-db  (初始化建表 + 老库补缺列)
//    ★ 2026-09-27: handleInitDb 已升级 —— 除了建表,还会 ALTER TABLE 给老表
//      补缺失的列(role/hwid/last_login/...),否则 register/login 会一直报
//      "table users has no column named role: SQLITE_ERROR"。
//      返回 JSON 的 migration 字段会列出: added=本次补的列,
//      present=已存在跳过的列, failed=失败的列(需关注)。
// 5. 然后访问: https://你的worker域名/api/health  (确认正常)
// 6. Custom Domain: Workers → Triggers → Custom Domains → 添加 dbapi.3d3k.org
// ============================================================

// ===== SQL 建表语句 (内嵌在Worker里) =====
const SCHEMA_SQL = `
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    hwid TEXT DEFAULT '',
    token TEXT DEFAULT '',
    role TEXT DEFAULT 'user',
    banned INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now')),
    last_login TEXT DEFAULT ''
);

CREATE TABLE IF NOT EXISTS cloud_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    config_key TEXT NOT NULL,
    config_value TEXT NOT NULL DEFAULT '',
    updated_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, config_key)
);

CREATE TABLE IF NOT EXISTS sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
`;

export default {
    async fetch(request, env) {
        const url = new URL(request.url);
        const path = url.pathname;
        const method = request.method;

        const corsHeaders = {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Auth-Token',
            'Access-Control-Max-Age': '86400',
        };

        if (method === 'OPTIONS') {
            return new Response(null, { status: 204, headers: corsHeaders });
        }

        try {
            let response;

            // ========== INIT DB ==========
            if (path === '/api/init-db' && method === 'GET')
                response = await handleInitDb(env);

            // ========== AUTH ==========
            else if (path === '/api/auth/register' && method === 'POST')
                response = await handleRegister(request, env);
            else if (path === '/api/auth/login' && method === 'POST')
                response = await handleLogin(request, env);
            else if (path === '/api/auth/logout' && method === 'POST')
                response = await handleLogout(request, env);
            else if (path === '/api/auth/me' && method === 'GET')
                response = await handleMe(request, env);

            // ========== CLOUD CONFIG ==========
            else if (path === '/api/config/get' && method === 'GET')
                response = await handleConfigGet(request, env);
            else if (path === '/api/config/set' && method === 'POST')
                response = await handleConfigSet(request, env);
            else if (path === '/api/config/delete' && method === 'POST')
                response = await handleConfigDelete(request, env);
            else if (path === '/api/config/list' && method === 'GET')
                response = await handleConfigList(request, env);
            else if (path === '/api/config/sync' && method === 'POST')
                response = await handleConfigSync(request, env);

            // ========== PRESETS ==========
            else if (path === '/api/presets/list' && method === 'GET')
                response = await handlePresetList(request, env);
            else if (path === '/api/presets/get' && method === 'GET')
                response = await handlePresetGet(request, env);
            else if (path === '/api/presets/save' && method === 'POST')
                response = await handlePresetSave(request, env);
            else if (path === '/api/presets/delete' && method === 'POST')
                response = await handlePresetDelete(request, env);

            // ========== ADMIN ==========
            else if (path === '/api/admin/users' && method === 'GET')
                response = await handleAdminUsers(request, env);
            else if (path === '/api/admin/ban' && method === 'POST')
                response = await handleAdminBan(request, env);
            else if (path === '/api/admin/set-role' && method === 'POST')
                response = await handleAdminSetRole(request, env);

            // ========== HEALTH ==========
            else if (path === '/api/health' && method === 'GET')
                response = jsonResponse({ status: 'ok', version: '1.0.0', timestamp: new Date().toISOString() });
            else
                response = jsonResponse({ error: 'Not Found', path: path }, 404);

            for (const [key, value] of Object.entries(corsHeaders))
                response.headers.set(key, value);
            return response;

        } catch (err) {
            console.error('Worker Error:', err);
            return jsonResponse({ error: 'Internal Server Error', message: err.message }, 500, corsHeaders);
        }
    }
};

// ============================================================
// UTILS
// ============================================================
function jsonResponse(data, status = 200, extraHeaders = {}) {
    return new Response(JSON.stringify(data), {
        status,
        headers: { 'Content-Type': 'application/json', ...extraHeaders }
    });
}

function jsonError(message, status = 400) {
    return jsonResponse({ error: message }, status);
}

async function hashPassword(password, salt) {
    const encoder = new TextEncoder();
    const data = encoder.encode(salt + password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    return Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, '0')).join('');
}

function generateToken() {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return Array.from(array).map(b => b.toString(16).padStart(2, '0')).join('');
}

function generateSalt() {
    const array = new Uint8Array(16);
    crypto.getRandomValues(array);
    return Array.from(array).map(b => b.toString(16).padStart(2, '0')).join('');
}

async function authenticate(request, env) {
    const authHeader = request.headers.get('Authorization');
    const tokenFromHeader = authHeader ? authHeader.replace('Bearer ', '') : null;
    const tokenFromParam = new URL(request.url).searchParams.get('token');
    const token = tokenFromParam || tokenFromHeader;
    if (!token) return null;

    const session = await env.DB.prepare(
        "SELECT s.*, u.id as uid, u.username, u.email, u.banned FROM sessions s JOIN users u ON s.user_id = u.id WHERE s.token = ? AND s.expires_at > datetime('now')"
    ).bind(token).first();

    if (!session || session.banned) return null;
    return { userId: session.uid, username: session.username, email: session.email, token: token };
}

function requireAuth(user) {
    if (!user) return jsonError('Unauthorized - Please login first', 401);
    return null;
}

// ===== 管理员密码 (修改这里) =====
const ADMIN_PASSWORD = 'Yao990815';

function isAdmin(request) {
    const url = new URL(request.url);
    const pwd = url.searchParams.get('pwd');
    return pwd === ADMIN_PASSWORD;
}

// ============================================================
// INIT DB (首次部署时访问一次;老库缺列时再访问一次即可补列)
//
// ★ 2026-09-27 修复: 老版本只 CREATE TABLE IF NOT EXISTS —— 表已存在时
//   什么都不做,导致老 users 表缺 role/hwid/last_login 等列,
//   register/login 报 "table users has no column named role" 500 错误。
//   现在建表后用 ALTER TABLE ADD COLUMN 幂等补列(列已存在自动跳过)。
// ============================================================
async function handleInitDb(env) {
    // 1) 建表(不存在才建,已存在跳过)
    const statements = SCHEMA_SQL
        .split(';')
        .map(s => s.trim())
        .filter(s => s.length > 0)
        .map(s => env.DB.prepare(s + ';'));
    await env.DB.batch(statements);

    // 2) 老表补缺列(幂等;列已存在会记进 present)
    const TABLE_COLUMNS = {
        users: [
            "password_hash TEXT DEFAULT ''",
            "salt TEXT DEFAULT ''",
            "hwid TEXT DEFAULT ''",
            "token TEXT DEFAULT ''",
            "role TEXT DEFAULT 'user'",
            "banned INTEGER DEFAULT 0",
            "created_at TEXT DEFAULT ''",
            "last_login TEXT DEFAULT ''",
        ],
        cloud_config: [
            "user_id INTEGER NOT NULL DEFAULT 0",
            "config_key TEXT NOT NULL DEFAULT ''",
            "config_value TEXT NOT NULL DEFAULT ''",
            "updated_at TEXT DEFAULT ''",
        ],
        sessions: [
            "user_id INTEGER NOT NULL DEFAULT 0",
            "token TEXT DEFAULT ''",
            "expires_at TEXT DEFAULT ''",
            "created_at TEXT DEFAULT ''",
        ],
    };
    const report = {};
    for (const [table, columns] of Object.entries(TABLE_COLUMNS)) {
        const added = [], present = [], failed = [];
        for (const definition of columns) {
            const name = definition.split(/\s+/)[0];
            try {
                await env.DB.prepare(`ALTER TABLE ${table} ADD COLUMN ${definition}`).run();
                added.push(name);
            } catch (e) {
                const msg = String((e && e.message) || e);
                if (msg.toLowerCase().includes('duplicate column')) present.push(name);
                else failed.push(name + ' (' + msg + ')');
            }
        }
        report[table] = { added, present, failed };
    }

    return jsonResponse({
        success: true,
        message: 'Database initialized and migrated',
        tables: ['users', 'cloud_config', 'sessions'],
        migration: report
    });
}

// ============================================================
// AUTH HANDLERS
// ============================================================
async function handleRegister(request, env) {
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { username, email, password, hwid } = body;
    if (!username || !email || !password) return jsonError('Username, email, and password are required');
    if (username.length < 3 || username.length > 20) return jsonError('Username must be 3-20 characters');
    if (password.length < 6) return jsonError('Password must be at least 6 characters');

    const existing = await env.DB.prepare('SELECT id FROM users WHERE username = ? OR email = ?').bind(username, email).first();
    if (existing) return jsonError('Username or email already exists', 409);

    const salt = generateSalt();
    const passwordHash = await hashPassword(password, salt);

    // 第一个注册的用户自动成为 admin
    const userCount = await env.DB.prepare('SELECT COUNT(*) as count FROM users').first();
    const role = (userCount && userCount.count === 0) ? 'admin' : 'user';

    const result = await env.DB.prepare('INSERT INTO users (username, email, password_hash, salt, hwid, role) VALUES (?, ?, ?, ?, ?, ?)').bind(username, email, passwordHash, salt, hwid || '', role).run();

    const token = generateToken();
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
    await env.DB.prepare('INSERT INTO sessions (user_id, token, expires_at) VALUES (?, ?, ?)').bind(result.meta.last_row_id, token, expiresAt).run();
    await env.DB.prepare("UPDATE users SET last_login = datetime('now') WHERE id = ?").bind(result.meta.last_row_id).run();

    return jsonResponse({ success: true, message: 'Registration successful', token: token, user: { id: result.meta.last_row_id, username: username, email: email } }, 201);
}

async function handleLogin(request, env) {
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { username, password, hwid } = body;
    if (!username || !password) return jsonError('Username and password are required');

    const user = await env.DB.prepare('SELECT * FROM users WHERE username = ? OR email = ?').bind(username, username).first();
    if (!user) return jsonError('Invalid credentials', 401);
    if (user.banned) return jsonError('Account is banned', 403);

    const passwordHash = await hashPassword(password, user.salt);
    if (passwordHash !== user.password_hash) return jsonError('Invalid credentials', 401);

    if (hwid) await env.DB.prepare('UPDATE users SET hwid = ? WHERE id = ?').bind(hwid, user.id).run();

    const token = generateToken();
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();
    await env.DB.prepare('INSERT INTO sessions (user_id, token, expires_at) VALUES (?, ?, ?)').bind(user.id, token, expiresAt).run();
    await env.DB.prepare("UPDATE users SET last_login = datetime('now') WHERE id = ?").bind(user.id).run();

    return jsonResponse({ success: true, message: 'Login successful', token: token, user: { id: user.id, username: user.username, email: user.email } });
}

async function handleLogout(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    await env.DB.prepare('DELETE FROM sessions WHERE token = ?').bind(user.token).run();
    return jsonResponse({ success: true, message: 'Logged out successfully' });
}

async function handleMe(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    return jsonResponse({ success: true, user: { id: user.userId, username: user.username, email: user.email } });
}

// ============================================================
// CLOUD CONFIG HANDLERS
// ============================================================
async function handleConfigGet(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    const key = new URL(request.url).searchParams.get('key');
    if (!key) return jsonError('Key parameter is required');

    const config = await env.DB.prepare('SELECT * FROM cloud_config WHERE user_id = ? AND config_key = ?').bind(user.userId, key).first();
    if (!config) return jsonResponse({ success: true, key: key, value: null, found: false });
    return jsonResponse({ success: true, key: config.config_key, value: config.config_value, updatedAt: config.updated_at, found: true });
}

async function handleConfigSet(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { key, value } = body;
    if (!key || value === undefined) return jsonError('Key and value are required');

    await env.DB.prepare(
        "INSERT INTO cloud_config (user_id, config_key, config_value, updated_at) VALUES (?, ?, ?, datetime('now')) ON CONFLICT(user_id, config_key) DO UPDATE SET config_value = excluded.config_value, updated_at = datetime('now')"
    ).bind(user.userId, key, String(value)).run();

    return jsonResponse({ success: true, message: 'Config saved', key: key, value: String(value) });
}

async function handleConfigDelete(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { key } = body;
    if (!key) return jsonError('Key is required');

    await env.DB.prepare('DELETE FROM cloud_config WHERE user_id = ? AND config_key = ?').bind(user.userId, key).run();
    return jsonResponse({ success: true, message: 'Config deleted', key: key });
}

async function handleConfigList(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    const results = await env.DB.prepare('SELECT * FROM cloud_config WHERE user_id = ?').bind(user.userId).all();
    const configs = {};
    for (const row of results.results) {
        configs[row.config_key] = row.config_value;
    }
    return jsonResponse({ success: true, configs: configs, count: results.results.length });
}

async function handleConfigSync(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { configs } = body;
    if (!configs || typeof configs !== 'object') return jsonError('Configs object is required');

    const statements = [];
    for (const [key, value] of Object.entries(configs)) {
        statements.push(
            env.DB.prepare(
                "INSERT INTO cloud_config (user_id, config_key, config_value, updated_at) VALUES (?, ?, ?, datetime('now')) ON CONFLICT(user_id, config_key) DO UPDATE SET config_value = excluded.config_value, updated_at = datetime('now')"
            ).bind(user.userId, key, String(value))
        );
    }
    await env.DB.batch(statements);
    return jsonResponse({ success: true, message: 'Synced ' + statements.length + ' configs', count: statements.length });
}

// ============================================================
// PRESET HANDLERS
// ============================================================
async function handlePresetList(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    const presets = await env.DB.prepare("SELECT config_key, config_value FROM cloud_config WHERE user_id = ? AND config_key LIKE 'preset:%'").bind(user.userId).all();
    const presetNames = presets.results.filter(function(r) { return r.config_key.endsWith(':name'); }).map(function(r) {
        return { id: r.config_key.replace('preset:', '').replace(':name', ''), name: r.config_value };
    });
    return jsonResponse({ success: true, presets: presetNames });
}

async function handlePresetGet(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    const presetId = new URL(request.url).searchParams.get('id');
    if (!presetId) return jsonError('Preset id is required');

    const configs = await env.DB.prepare("SELECT config_key, config_value FROM cloud_config WHERE user_id = ? AND config_key LIKE ?").bind(user.userId, 'preset:' + presetId + ':%').all();
    const data = {};
    for (const row of configs.results) {
        data[row.config_key.replace('preset:' + presetId + ':', '')] = row.config_value;
    }
    return jsonResponse({ success: true, preset: data });
}

async function handlePresetSave(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { id, name, data } = body;
    if (!id || !data) return jsonError('Preset id and data are required');

    const statements = [];
    if (name) {
        statements.push(
            env.DB.prepare(
                "INSERT INTO cloud_config (user_id, config_key, config_value, updated_at) VALUES (?, ?, ?, datetime('now')) ON CONFLICT(user_id, config_key) DO UPDATE SET config_value = excluded.config_value, updated_at = datetime('now')"
            ).bind(user.userId, 'preset:' + id + ':name', name)
        );
    }
    for (const [key, value] of Object.entries(data)) {
        statements.push(
            env.DB.prepare(
                "INSERT INTO cloud_config (user_id, config_key, config_value, updated_at) VALUES (?, ?, ?, datetime('now')) ON CONFLICT(user_id, config_key) DO UPDATE SET config_value = excluded.config_value, updated_at = datetime('now')"
            ).bind(user.userId, 'preset:' + id + ':' + key, String(value))
        );
    }
    await env.DB.batch(statements);
    return jsonResponse({ success: true, message: 'Preset saved', presetId: id });
}

async function handlePresetDelete(request, env) {
    const user = await authenticate(request, env);
    const authErr = requireAuth(user);
    if (authErr) return authErr;
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { id } = body;
    if (!id) return jsonError('Preset id is required');

    const allKeys = await env.DB.prepare("SELECT config_key FROM cloud_config WHERE user_id = ? AND config_key LIKE ?").bind(user.userId, 'preset:' + id + ':%').all();
    const statements = allKeys.results.map(function(row) {
        return env.DB.prepare('DELETE FROM cloud_config WHERE user_id = ? AND config_key = ?').bind(user.userId, row.config_key);
    });
    await env.DB.batch(statements);
    return jsonResponse({ success: true, message: 'Preset deleted', presetId: id });
}

// ============================================================
// ADMIN HANDLERS
// ============================================================
async function handleAdminUsers(request, env) {
    if (!isAdmin(request)) return jsonError('Admin access required', 403);
    const users = await env.DB.prepare('SELECT id, username, email, role, banned, created_at, last_login FROM users').all();
    return jsonResponse({ success: true, users: users.results });
}

async function handleAdminBan(request, env) {
    if (!isAdmin(request)) return jsonError('Admin access required', 403);
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { userId, banned } = body;
    if (!userId) return jsonError('User ID is required');

    await env.DB.prepare('UPDATE users SET banned = ? WHERE id = ?').bind(banned ? 1 : 0, userId).run();
    if (banned) await env.DB.prepare('DELETE FROM sessions WHERE user_id = ?').bind(userId).run();
    return jsonResponse({ success: true, message: 'User ' + (banned ? 'banned' : 'unbanned') });
}

async function handleAdminSetRole(request, env) {
    if (!isAdmin(request)) return jsonError('Admin access required', 403);
    let body;
    try { body = await request.json(); } catch(e) { return jsonError('Invalid JSON'); }
    const { userId, role } = body;
    if (!userId || !role) return jsonError('User ID and role are required');
    if (!['user', 'admin'].includes(role)) return jsonError('Role must be user or admin');

    await env.DB.prepare('UPDATE users SET role = ? WHERE id = ?').bind(role, userId).run();
    return jsonResponse({ success: true, message: 'Role updated to ' + role });
}
