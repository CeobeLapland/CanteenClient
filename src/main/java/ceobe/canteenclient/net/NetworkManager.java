package ceobe.canteenclient.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 全局单例网络管理器（JavaFX + Java HttpClient）。
 *
 * <p>核心特性：
 * <ol>
 *   <li><b>防重复</b>：同一个 requestKey 正在进行中时，后续相同请求直接丢弃，
 *       避免按钮连击 / 轮询堆叠。</li>
 *   <li><b>请求排队</b>：底层使用固定线程池（默认4线程），
 *       超出并发的请求自动在队列中等待，不会丢失。</li>
 *   <li><b>JavaFX线程回调</b>：所有 onSuccess / onError 均通过
 *       {@code Platform.runLater} 切回 FX 线程，可直接操作 UI。</li>
 * </ol>
 *
 * <p>使用前调用 {@link #init(String)} 设置 baseUrl，应用退出时调用 {@link #shutdown()}。
 */
public class NetworkManager {

    // ═══════════════════════════════════════════════════════════════════
    //  单例
    // ═══════════════════════════════════════════════════════════════════

    private static volatile NetworkManager instance;

    public static NetworkManager getInstance() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) instance = new NetworkManager();
            }
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  字段
    // ═══════════════════════════════════════════════════════════════════

    private String baseUrl = "http://localhost:8080";

    /** 固定线程池：控制最大并发数，多余请求自动排队 */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /** 正在进行中的请求 key 集合，用于防重复 */
    private final Set<String> pendingKeys = ConcurrentHashMap.newKeySet();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    // ═══════════════════════════════════════════════════════════════════
    //  初始化 / 销毁
    // ═══════════════════════════════════════════════════════════════════

    private NetworkManager() {}

    /**
     * 设置后端基础 URL，例如 {@code "http://localhost:8080"}。
     * 建议在 JavaFX Application.start() 最开始调用一次。
     */
    public void init(String baseUrl) {
        System.out.println("NetworkManager 初始化，baseUrl = " + baseUrl);
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        //mapper.registerModule(new JavaTimeModule());
        //mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /** 应用退出时调用，优雅关闭线程池 */
    public void shutdown() {
        executor.shutdown();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  公开 API —— GET
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 发送 GET 请求并将响应体反序列化为指定类型。
     *
     * @param path      接口路径，例如 {@code "/api/food?page=0&size=10"}
     * @param typeRef   Jackson TypeReference，用于泛型反序列化
     * @param onSuccess 成功回调（已切换到 FX 线程）
     * @param onError   失败回调（已切换到 FX 线程），参数为异常信息字符串
     * @param <T>       目标类型
     *
     * @return 请求是否成功提交（false 表示同一 key 正在进行中，本次已被丢弃）
     */
    public <T> boolean get(String path,
                           TypeReference<T> typeRef,
                           Consumer<T> onSuccess,
                           Consumer<String> onError) {
        String key = "GET:" + path;
        if (!acquireKey(key)) return false;

        executor.submit(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .timeout(REQUEST_TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> res = httpClient.send(
                        req, HttpResponse.BodyHandlers.ofString());

                handleResponse(res, typeRef, onSuccess, onError);
            } catch (Exception e) {
                runOnFx(() -> onError.accept("网络异常：" + e.getMessage()));
            } finally {
                releaseKey(key);
            }
        });

        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  公开 API —— POST
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 发送 POST 请求（JSON body）。
     *
     * @param path      接口路径，例如 {@code "/api/food"}
     * @param body      请求体对象，会被序列化为 JSON；传 null 则发送空 body
     * @param typeRef   响应体类型
     * @param onSuccess 成功回调（FX 线程）
     * @param onError   失败回调（FX 线程）
     */
    public <T> boolean post(String path,
                            Object body,
                            TypeReference<T> typeRef,
                            Consumer<T> onSuccess,
                            Consumer<String> onError) {
        String key = "POST:" + path;
        if (!acquireKey(key)) return false;

        executor.submit(() -> {
            try {
                String json = body != null ? mapper.writeValueAsString(body) : "";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .timeout(REQUEST_TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> res = httpClient.send(
                        req, HttpResponse.BodyHandlers.ofString());

                handleResponse(res, typeRef, onSuccess, onError);
            } catch (Exception e) {
                runOnFx(() -> onError.accept("网络异常：" + e.getMessage()));
            } finally {
                releaseKey(key);
            }
        });

        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  公开 API —— PUT / DELETE（结构与 POST/GET 相同，按需扩展）
    // ═══════════════════════════════════════════════════════════════════

    public <T> boolean put(String path,
                           Object body,
                           TypeReference<T> typeRef,
                           Consumer<T> onSuccess,
                           Consumer<String> onError) {
        String key = "PUT:" + path;
        if (!acquireKey(key)) return false;

        executor.submit(() -> {
            try {
                String json = body != null ? mapper.writeValueAsString(body) : "";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .timeout(REQUEST_TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> res = httpClient.send(
                        req, HttpResponse.BodyHandlers.ofString());

                handleResponse(res, typeRef, onSuccess, onError);
            } catch (Exception e) {
                runOnFx(() -> onError.accept("网络异常：" + e.getMessage()));
            } finally {
                releaseKey(key);
            }
        });

        return true;
    }

    public <T> boolean delete(String path,
                              TypeReference<T> typeRef,
                              Consumer<T> onSuccess,
                              Consumer<String> onError) {
        String key = "DELETE:" + path;
        if (!acquireKey(key)) return false;

        executor.submit(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .timeout(REQUEST_TIMEOUT)
                        .header("Accept", "application/json")
                        .DELETE()
                        .build();

                HttpResponse<String> res = httpClient.send(
                        req, HttpResponse.BodyHandlers.ofString());

                handleResponse(res, typeRef, onSuccess, onError);
            } catch (Exception e) {
                runOnFx(() -> onError.accept("网络异常：" + e.getMessage()));
            } finally {
                releaseKey(key);
            }
        });

        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  内部工具
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 解析 HTTP 响应：
     * <ul>
     *   <li>HTTP 2xx → 反序列化并走 onSuccess</li>
     *   <li>其他 → 尝试解析后端 ApiResponse.message，走 onError</li>
     * </ul>
     */
    private <T> void handleResponse(HttpResponse<String> res,
                                    TypeReference<T> typeRef,
                                    Consumer<T> onSuccess,
                                    Consumer<String> onError) {
        int status = res.statusCode();
        String rawBody = res.body();

        if (status >= 200 && status < 300) {
            try {
                T parsed = mapper.readValue(rawBody, typeRef);
                runOnFx(() -> onSuccess.accept(parsed));
            } catch (Exception e) {
                runOnFx(() -> onError.accept("响应解析失败：" + e.getMessage()));
            }
        } else {
            // 尝试从后端标准错误体取 message
            String errMsg = "HTTP " + status;
            try {
                Map<?, ?> errBody = mapper.readValue(rawBody, Map.class);
                Object msg = errBody.get("message");
                if (msg != null) errMsg = msg.toString();
            } catch (Exception ignored) {}

            String finalErrMsg = errMsg;
            runOnFx(() -> onError.accept(finalErrMsg));
        }
    }

    /** 尝试占用 key；若已存在则返回 false（防重复核心逻辑） */
    private boolean acquireKey(String key) {
        return pendingKeys.add(key); // ConcurrentHashSet.add 原子操作
    }

    /** 请求完成后释放 key，允许下一次同路径请求进入 */
    private void releaseKey(String key) {
        pendingKeys.remove(key);
    }

    /** 确保回调在 JavaFX Application Thread 上执行 */
    private void runOnFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
