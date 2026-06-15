package ceobe.canteenclient.net.service;

import ceobe.canteenclient.net.ApiResponse;
import ceobe.canteenclient.net.NetworkManager;
import ceobe.canteenclient.net.dto.Dtos;
import ceobe.canteenclient.net.request.CreateFoodRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.Consumer;

public class AdminService
{
    private final NetworkManager net = NetworkManager.getInstance();

    // 单例
    private static final AdminService INSTANCE = new AdminService();
    public static AdminService getInstance() { return INSTANCE; }
    private AdminService() {}





    //  POST  /api/food

    /**
     * 新增一条食物记录。
     *
     * @param request   请求体（会被序列化为 JSON）
     * @param onSuccess 成功回调，参数为后端返回的 {@link ApiResponse < Dtos.FoodDetailDto >}
     * @param onError   失败回调
     */
    public void addFood(CreateFoodRequest request,
                        Consumer<ApiResponse<Dtos.FoodDetailDto>> onSuccess,
                        Consumer<String> onError) {

        net.post(
                "/api/food",
                request,
                new TypeReference<ApiResponse<Dtos.FoodDetailDto>>() {},
                onSuccess,
                onError
        );
    }



    /** 批量新增菜品
     * <p>POST /api/v1/foods/batch
     */
    public void addFoodsBatch(java.util.List<CreateFoodRequest> requests,
                              Consumer<ApiResponse<java.util.List<Dtos.FoodDetailDto>>> onSuccess,
                              Consumer<String> onError) {

        net.post(
                "/api/v1/foods/batch",
                requests,
                new TypeReference<ApiResponse<java.util.List<Dtos.FoodDetailDto>>>() {
                },
                onSuccess,
                onError
        );
    }




    //  PUT  /api/food/{id}

    public void updateFood(long id, CreateFoodRequest request,
                           Consumer<ApiResponse<Dtos.FoodDetailDto>> onSuccess,
                           Consumer<String> onError) {
        net.put(
                "/api/food/" + id,
                request,
                new TypeReference<ApiResponse<Dtos.FoodDetailDto>>() {},
                onSuccess,
                onError
        );
    }


    //  DELETE  /api/food/{id}

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
}
