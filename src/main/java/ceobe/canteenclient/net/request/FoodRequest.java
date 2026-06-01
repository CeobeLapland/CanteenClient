package ceobe.canteenclient.net.request;

/** 新增 / 修改食物的请求体 */
public class FoodRequest {
    public String name;
    public double calories;

    public FoodRequest() {}
    public FoodRequest(String name, double calories) {
        this.name     = name;
        this.calories = calories;
    }
}