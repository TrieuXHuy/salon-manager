package com.salonnbooking.desktop.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.salonnbooking.desktop.session.AuthSession;
import com.salonnbooking.desktop.util.JsonUtil;

public class ApiClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        return send(request, responseType);
    }

    public String getRaw(String path) throws Exception {
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        String body = response.body();
        if (status < 200 || status >= 300) {
            throw new ApiException(status, "API request failed: " + status, body);
        }
        return body;
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws Exception {
        String json = body == null ? "" : JsonUtil.toJson(body);
        HttpRequest request = baseRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    public <T> T patch(String path, Object body, Class<T> responseType) throws Exception {
        String json = body == null ? "" : JsonUtil.toJson(body);
        HttpRequest request = baseRequest(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) throws Exception {
        String json = body == null ? "" : JsonUtil.toJson(body);
        HttpRequest request = baseRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    public <T> T delete(String path, Class<T> responseType) throws Exception {
        HttpRequest request = baseRequest(path)
                .DELETE()
                .build();
        return send(request, responseType);
    }

    private HttpRequest.Builder baseRequest(String path) {
        String resolved = path.startsWith("/") ? path : ("/" + path);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + resolved))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        String token = AuthSession.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private <T> T send(HttpRequest request, Class<T> responseType) throws Exception {
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        String body = response.body();

        if (status < 200 || status >= 300) {
            throw new ApiException(status, "API request failed: " + status, body);
        }
        if (responseType == Void.class) {
            return null;
        }
        return JsonUtil.fromJson(body, responseType);
    }
}
