package dev.duskbyte.managers.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class CloudApiClient {
    private static CloudApiClient INSTANCE;
    private static final String BASE_URL = "https://dbapi.3d3k.org/api";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CloudApiClient.class);
    private final Gson gson = new Gson();

    private String authToken = null;
    private String username = null;
    private String email = null;
    private int userId = -1;

    public CloudApiClient() {
        INSTANCE = this;
    }

    public static CloudApiClient getInstance() {
        return INSTANCE;
    }

    // =================== AUTH ===================

    public CloudResponse register(String username, String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("email", email);
        body.addProperty("password", password);

        CloudResponse response = post("/auth/register", body.toString(), false);

        if (response.success) {
            parseAuthPayload(response);
        }

        return response;
    }

    public CloudResponse login(String username, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        CloudResponse response = post("/auth/login", body.toString(), false);

        if (response.success) {
            parseAuthPayload(response);
        }

        return response;
    }

    /**
     * 解析登录/注册响应,兼容三种常见结构:
     * 1) {"token":"...", "user":{...}}
     * 2) {"data":{"token":"...", "user":{...}}}
     * 3) {"token":"...", "username":"...", ...}(用户字段在根上)
     */
    private void parseAuthPayload(CloudResponse response) {
        try {
            JsonObject data = response.toJson();
            JsonObject payload = (data.has("data") && data.get("data").isJsonObject())
                    ? data.getAsJsonObject("data") : data;

            if (payload.has("token") && payload.get("token").isJsonPrimitive()) {
                this.authToken = payload.get("token").getAsString();
            }

            JsonObject user = (payload.has("user") && payload.get("user").isJsonObject())
                    ? payload.getAsJsonObject("user") : payload;

            if (user.has("username") && user.get("username").isJsonPrimitive()) {
                this.username = user.get("username").getAsString();
                this.userId = (user.has("id") && user.get("id").isJsonPrimitive())
                        ? user.get("id").getAsInt() : -1;
                this.email = (user.has("email") && user.get("email").isJsonPrimitive())
                        ? user.get("email").getAsString() : null;
            }
        } catch (Exception ignored) {}
    }

    public CloudResponse logout() {
        CloudResponse response = post("/auth/logout", "{}", true);
        this.authToken = null;
        this.username = null;
        this.email = null;
        this.userId = -1;
        return response;
    }

    public boolean isLoggedIn() {
        return authToken != null && !authToken.isEmpty();
    }

    public String getAuthToken() { return authToken; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getUserId() { return userId; }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    // =================== CLOUD CONFIG ===================

    public CloudResponse getConfig(String key) {
        return getAuth("/config/get?key=" + encodeParam(key));
    }

    public CloudResponse setConfig(String key, String value) {
        JsonObject body = new JsonObject();
        body.addProperty("key", key);
        body.addProperty("value", value);
        return post("/config/set", body.toString(), true);
    }

    public CloudResponse deleteConfig(String key) {
        JsonObject body = new JsonObject();
        body.addProperty("key", key);
        return post("/config/delete", body.toString(), true);
    }

    public CloudResponse listConfigs() {
        return getAuth("/config/list");
    }

    public CloudResponse syncConfigs(Map<String, String> configs) {
        JsonObject body = new JsonObject();
        JsonObject configObj = new JsonObject();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            configObj.addProperty(entry.getKey(), entry.getValue());
        }
        body.add("configs", configObj);
        return post("/config/sync", body.toString(), true);
    }

    // =================== PRESETS ===================

    public CloudResponse listPresets() {
        return getAuth("/presets/list");
    }

    public CloudResponse getPreset(String presetId) {
        return getAuth("/presets/get?id=" + encodeParam(presetId));
    }

    public CloudResponse savePreset(String id, String name, Map<String, String> data) {
        JsonObject body = new JsonObject();
        body.addProperty("id", id);
        if (name != null) body.addProperty("name", name);
        JsonObject dataObj = new JsonObject();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            dataObj.addProperty(entry.getKey(), entry.getValue());
        }
        body.add("data", dataObj);
        return post("/presets/save", body.toString(), true);
    }

    // =================== HTTP HELPERS ===================

    private CloudResponse getAuth(String path) {
        return request("GET", path, null, true);
    }

    private CloudResponse post(String path, String body, boolean withAuth) {
        return request("POST", path, body, withAuth);
    }

    public CloudResponse request(String method, String path, String body, boolean withAuth) {
        URL url = null;
        try {
            url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (withAuth && authToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            if (body != null && (method.equals("POST") || method.equals("PUT"))) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = conn.getResponseCode();
            // 只记 URL 与状态码,不记成功响应体(避免 token 泄漏进日志)
            LOGGER.info("[Cloud] {} {} -> HTTP {}", method, url, statusCode);

            InputStream is = (statusCode >= 200 && statusCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseText = "";
            if (is != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    responseText = sb.toString();
                }
            }

            // 错误响应(4xx/5xx)没有 token,记下原始 body 方便定位服务端问题
            if (statusCode >= 400) {
                String shown = responseText.length() > 500 ? responseText.substring(0, 500) + "..." : responseText;
                LOGGER.warn("[Cloud] {} {} -> HTTP {} body: {}", method, url, statusCode, shown);
            }

            return new CloudResponse(statusCode, responseText);
        } catch (Exception e) {
            LOGGER.warn("[Cloud] {} {} failed: {}", method, url, e.toString());
            return new CloudResponse(-1, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            return param;
        }
    }

    // =================== RESPONSE CLASS ===================

    public static class CloudResponse {
        public final int statusCode;
        public final String rawBody;
        public final boolean success;

        public CloudResponse(int statusCode, String rawBody) {
            this.statusCode = statusCode;
            this.rawBody = rawBody;
            this.success = statusCode >= 200 && statusCode < 300;
        }

        public JsonObject toJson() {
            try {
                return JsonParser.parseString(rawBody).getAsJsonObject();
            } catch (Exception e) {
                return new JsonObject();
            }
        }

        public String getString(String key) {
            JsonObject json = toJson();
            // 值可能是嵌套对象(如 {"error":{"code":...}}),只取字符串类型,避免抛异常
            return (json.has(key) && json.get(key).isJsonPrimitive()) ? json.get(key).getAsString() : null;
        }

        public String getMessage() { return getString("message"); }
        public String getError() { return getString("error"); }
    }
}
