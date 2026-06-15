package ceobe.canteenclient.net.service;

import ceobe.canteenclient.net.ApiResponse;
import ceobe.canteenclient.net.NetworkManager;
import ceobe.canteenclient.net.PageResponse;
import ceobe.canteenclient.net.dto.Dtos;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.Consumer;

import ceobe.canteenclient.net.dto.Dtos.FoodDetailDto;
import ceobe.canteenclient.net.request.CreateFoodRequest;

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


    /**
     * 测试函数
     * <p>GET /api/v1/foods/test
     */
    public void test(Consumer<ApiResponse<String>> onSuccess,
                     Consumer<String> onError) {
        net.get(
                "/api/v1/foods/test",
                new TypeReference<ApiResponse<String>>() {
                },
                onSuccess,
                onError
        );
    }

    
    //  GET  /api/food?page=0&size=20
    

    /**
     * 分页获取食物列表。
     *
     * <p>如果上一次请求还未完成，本次调用会被自动丢弃（防止轮询堆叠）。
     *
     * @param page      页码（从 0 开始，与后端一致）
     * @param size      每页条数
     * @param onSuccess 成功回调，参数为 {@link PageResponse <FoodDetailDto>}
     * @param onError   失败回调，参数为错误信息字符串
     */
    public void getFoodPaged(int page, int size,
                             Consumer<ApiResponse<PageResponse<FoodDetailDto>>> onSuccess,
                             Consumer<String> onError) {

        //String path = "/api/food?page=" + page + "&size=" + size + "&sort=name,asc";
        //更新一下API，现在是GET /api/v1/foods/details?page=0&size=20
        String path = "/api/v1/foods/details?page=" + page + "&size=" + size;

        boolean submitted = net.get(
                path,
                new TypeReference<ApiResponse<PageResponse<FoodDetailDto>>>() {},
                onSuccess,
                onError
        );

        if (!submitted) {
            System.out.println("[FoodService] getFood 请求正在进行中，本次丢弃");
        }
    }

    
    //这里面只写和读取有关的接口，增删改的接口放在 AdminService 里



    /**
     * 获取所有window列表（不分页）
     * <p>GET /api/v1/foods/windows
     */
    public void getAllWindows(Consumer<ApiResponse<Dtos.WindowSearchDto>> onSuccess,
                              Consumer<String> onError) {
        net.get(
                "/api/v1/foods/windows",
                new TypeReference<ApiResponse<Dtos.WindowSearchDto>>() {
                },
                onSuccess,
                onError
        );
    }

    /**
     * 获取所有Tag列表（不分页），以DTO形式返回
     * <p>GET /api/v1/foods/tags
     */
    public void getAllTags(Consumer<ApiResponse<Dtos.TagDto>> onSuccess,
                           Consumer<String> onError) {
        net.get(
                "/api/v1/foods/tags",
                new TypeReference<ApiResponse<Dtos.TagDto>>() {
                },
                onSuccess,
                onError
        );
    }
}