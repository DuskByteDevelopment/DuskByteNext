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
            try {
                JsonObject data = response.toJson();
                this.authToken = data.get("token").getAsString();
                JsonObject user = data.getAsJsonObject("user");
                this.userId = user.get("id").getAsInt();
                this.username = user.get("username").getAsString();
                this.email = user.get("email").getAsString();
            } catch (Exception ignored) {}
        }

        return response;
    }

    public CloudResponse login(String username, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        CloudResponse response = post("/auth/login", body.toString(), false);

        if (response.success) {
            try {
                JsonObject data = response.toJson();
                this.authToken = data.get("token").getAsString();
                JsonObject user = data.getAsJsonObject("user");
                this.userId = user.get("id").getAsInt();
                this.username = user.get("username").getAsString();
                this.email = user.get("email").getAsString();
            } catch (Exception ignored) {}
        }

        return response;
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
        try {
            URL url = new URL(BASE_URL + path);
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

            return new CloudResponse(statusCode, responseText);
        } catch (Exception e) {
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
            return json.has(key) ? json.get(key).getAsString() : null;
        }

        public String getMessage() { return getString("message"); }
        public String getError() { return getString("error"); }
    }
}
