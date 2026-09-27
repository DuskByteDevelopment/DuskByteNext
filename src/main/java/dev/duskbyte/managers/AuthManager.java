package dev.duskbyte.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.duskbyte.DuskByte;
import dev.duskbyte.managers.cloud.CloudApiClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class AuthManager {
    private static AuthManager INSTANCE;
    private static final Gson GSON = new Gson();
    private boolean authenticated = false;
    private String username = null;
    private String email = null;
    private int userId = -1;

    // 本地存储路径
    private final File authFile;

    public AuthManager() {
        INSTANCE = this;
        String temp = System.getProperty("java.io.tmpdir");
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            temp = System.getProperty("user.home");
        }
        File folder = new File(temp, "UJHfsGGjbPfVZ");
        authFile = new File(folder, "auth.json");
        loadToken();
    }

    public static AuthManager getInstance() {
        return INSTANCE;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getUserId() { return userId; }

    /**
     * 注册
     */
    public CloudApiClient.CloudResponse register(String username, String email, String password) {
        CloudApiClient api = DuskByte.INSTANCE.cloudApiClient;
        CloudApiClient.CloudResponse response = api.register(username, email, password);

        // 必须真的拿到 token 才算注册成功,避免 HTTP 2xx 但响应体不带 token 时出现"假登录"
        if (response.success && hasToken(api)) {
            this.authenticated = true;
            this.username = api.getUsername();
            this.email = api.getEmail();
            this.userId = api.getUserId();
            DuskByte.INSTANCE.authenticated = true;
            saveToken(api.getAuthToken());
        }

        return response;
    }

    /**
     * 登录
     */
    public CloudApiClient.CloudResponse login(String username, String password) {
        CloudApiClient api = DuskByte.INSTANCE.cloudApiClient;
        CloudApiClient.CloudResponse response = api.login(username, password);

        if (response.success && hasToken(api)) {
            this.authenticated = true;
            this.username = api.getUsername();
            this.email = api.getEmail();
            this.userId = api.getUserId();
            DuskByte.INSTANCE.authenticated = true;
            saveToken(api.getAuthToken());
        }

        return response;
    }

    private static boolean hasToken(CloudApiClient api) {
        String token = api.getAuthToken();
        return token != null && !token.isEmpty();
    }

    /**
     * 登出
     */
    public void logout() {
        CloudApiClient api = DuskByte.INSTANCE.cloudApiClient;
        if (api.isLoggedIn()) {
            api.logout();
        }
        this.authenticated = false;
        this.username = null;
        this.email = null;
        this.userId = -1;
        DuskByte.INSTANCE.authenticated = false;
        deleteToken();
    }

    /**
     * 尝试用本地保存的 token 自动登录
     */
    public boolean tryAutoLogin() {
        String savedToken = loadToken();
        if (savedToken == null || savedToken.isEmpty()) return false;

        CloudApiClient api = DuskByte.INSTANCE.cloudApiClient;
        api.setAuthToken(savedToken);

        // 注意: BASE_URL 已经带 /api,这里必须写 /auth/me。
        // 之前写成 /api/auth/me 会打到 /api/api/auth/me → 404 → 每次启动都把
        // 保存的 token 删掉,导致"登录了下次还要重新登录"。
        CloudApiClient.CloudResponse me = api.request("GET", "/auth/me", null, true);
        if (me.success) {
            try {
                JsonObject data = me.toJson();
                // 兼容 data 包裹 / user 包裹 / 直接返回用户字段 三种响应结构
                JsonObject payload = (data.has("data") && data.get("data").isJsonObject())
                        ? data.getAsJsonObject("data") : data;
                JsonObject user = (payload.has("user") && payload.get("user").isJsonObject())
                        ? payload.getAsJsonObject("user") : payload;

                if (user.has("username") && user.get("username").isJsonPrimitive()) {
                    this.authenticated = true;
                    this.username = user.get("username").getAsString();
                    this.email = (user.has("email") && user.get("email").isJsonPrimitive())
                            ? user.get("email").getAsString() : null;
                    this.userId = (user.has("id") && user.get("id").isJsonPrimitive())
                            ? user.get("id").getAsInt() : -1;
                    DuskByte.INSTANCE.authenticated = true;
                    return true;
                }

                // 200 但拿不到用户信息 → token 无效,删掉
                deleteToken();
                return false;
            } catch (Exception e) {
                // token 过期了
                deleteToken();
                return false;
            }
        } else {
            deleteToken();
            return false;
        }
    }

    // ===== 本地 Token 存储 =====

    private void saveToken(String token) {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("token", token);
            authFile.getParentFile().mkdirs();
            Files.writeString(authFile.toPath(), GSON.toJson(obj));
        } catch (IOException ignored) {}
    }

    private String loadToken() {
        try {
            if (authFile.exists()) {
                String content = Files.readString(authFile.toPath());
                JsonObject obj = JsonParser.parseString(content).getAsJsonObject();
                return obj.has("token") ? obj.get("token").getAsString() : null;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void deleteToken() {
        try {
            if (authFile.exists()) {
                Files.delete(authFile.toPath());
            }
        } catch (IOException ignored) {}
    }
}
