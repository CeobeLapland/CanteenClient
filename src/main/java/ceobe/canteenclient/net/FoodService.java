package ceobe.canteenclient.net;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.Consumer;

/**
 * 食物相关接口封装示例。
 *
 * <p>推荐为每个业务领域建一个 Service 类，内部统一持有
 * {@link NetworkManager} 引用，Controller / ViewModel 只调用 Service。
 *
 * <p>所有回调均在 <b>JavaFX Application Thread</b> 上触发，可直接操作 UI。
 */
public class FoodService {

    private final NetworkManager net = NetworkManager.getInstance();

    // ── 单例（可选，也可以直接 new FoodService()）────────────────────
    private static final FoodService INSTANCE = new FoodService();
    public static FoodService getInstance() { return INSTANCE; }
    private FoodService() {}

    // ════════════════════════════════════════════════════════════════════
    //  GET  /api/food?page=0&size=10
    // ════════════════════════════════════════════════════════════════════

    /**
     * 分页获取食物列表。
     *
     * <p>如果上一次请求还未完成，本次调用会被自动丢弃（防止轮询堆叠）。
     *
     * @param page      页码（从 0 开始，与后端一致）
     * @param size      每页条数
     * @param onSuccess 成功回调，参数为 {@link PageResponse<Food>}
     * @param onError   失败回调，参数为错误信息字符串
     */
    public void getFood(int page, int size,
                        Consumer<ApiResponse<PageResponse<Food>>> onSuccess,
                        Consumer<String> onError) {

        String path = "/api/food?page=" + page + "&size=" + size;

        boolean submitted = net.get(
                path,
                new TypeReference<ApiResponse<PageResponse<Food>>>() {},
                onSuccess,
                onError
        );

        if (!submitted) {
            System.out.println("[FoodService] getFood 请求正在进行中，本次丢弃");
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  POST  /api/food
    // ════════════════════════════════════════════════════════════════════

    /**
     * 新增一条食物记录。
     *
     * @param request   请求体（会被序列化为 JSON）
     * @param onSuccess 成功回调，参数为后端返回的 {@link ApiResponse<Food>}
     * @param onError   失败回调
     */
    public void addFood(FoodRequest request,
                        Consumer<ApiResponse<Food>> onSuccess,
                        Consumer<String> onError) {

        net.post(
                "/api/food",
                request,
                new TypeReference<ApiResponse<Food>>() {},
                onSuccess,
                onError
        );
    }

    // ════════════════════════════════════════════════════════════════════
    //  PUT  /api/food/{id}
    // ════════════════════════════════════════════════════════════════════

    public void updateFood(long id, FoodRequest request,
                           Consumer<ApiResponse<Food>> onSuccess,
                           Consumer<String> onError) {
        net.put(
                "/api/food/" + id,
                request,
                new TypeReference<ApiResponse<Food>>() {},
                onSuccess,
                onError
        );
    }

    // ════════════════════════════════════════════════════════════════════
    //  DELETE  /api/food/{id}
    // ════════════════════════════════════════════════════════════════════

    public void deleteFood(long id,
                           Consumer<ApiResponse<Void>> onSuccess,
                           Consumer<String> onError) {
        net.delete(
                "/api/food/" + id,
                new TypeReference<ApiResponse<Void>>() {},
                onSuccess,
                onError
        );
    }

    // ════════════════════════════════════════════════════════════════════
    //  内部 DTO（实际项目请放到独立文件）
    // ════════════════════════════════════════════════════════════════════

    /** 食物实体（与后端 Food 对应） */
    public static class Food {
        public Long   id;
        public String name;
        public double calories;
        // 按需添加字段 / getter / setter
    }

    /** 新增 / 修改食物的请求体 */
    public static class FoodRequest {
        public String name;
        public double calories;

        public FoodRequest() {}
        public FoodRequest(String name, double calories) {
            this.name     = name;
            this.calories = calories;
        }
    }
}
