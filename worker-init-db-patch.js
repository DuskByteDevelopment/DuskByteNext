// ===========================================================================
// DuskByte Cloud API — handleInitDb 补丁【自包含版】
//
// 用法: Cloudflare Worker → Edit code → 找到 async function handleInitDb(env){...}
//       整个函数替换为本文件中的函数 → Deploy → 访问一次
//       https://dbapi.3d3k.org/api/init-db
//       返回 JSON 出现 "migration" 字段 = 新代码已生效;
//       migration.users.added 应包含 role、last_login、hwid 等。
//
// 背景(2026-09-27 排查):
//   POST /api/auth/register、/api/auth/login 返回 500:
//   D1_ERROR: table users has no column named role: SQLITE_ERROR
//   根因: users 表由老版本 schema 建立,缺 role/hwid/last_login 等列;
//   原 handleInitDb 只有 CREATE TABLE IF NOT EXISTS —— 表存在时什么都不做,
//   缺列永远补不上。此版本在建表后用 ALTER TABLE ADD COLUMN 幂等补列。
// ===========================================================================

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
