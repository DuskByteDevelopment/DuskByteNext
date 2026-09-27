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

        if (response.success) {
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

        if (response.success) {
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

        // 用 token 访问 /api/auth/me 验证是否还有效
        CloudApiClient.CloudResponse me = api.request("GET", "/api/auth/me", null, true);
        if (me.success) {
            try {
                JsonObject data = me.toJson();
                JsonObject user = data.getAsJsonObject("user");
                this.authenticated = true;
                this.username = user.get("username").getAsString();
                this.email = user.get("email").getAsString();
                this.userId = user.get("id").getAsInt();
                DuskByte.INSTANCE.authenticated = true;
                return true;
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
