package ceobe.canteenclient.net.request;

import lombok.Data;

import java.util.List;
import java.util.Set;

/** 筛选菜品请求（GET 请求的查询参数） */
@Data
public class FilterFoodRequest {

    private String name;    // 按名称模糊匹配

    private String campus;  // 按校区精确匹配

    private String canteen; // 按食堂精确匹配

    private String floor;   // 按楼层精确匹配

    private String window;  // 按窗口精确匹配

    private List<String> tags;     // 按标签模糊匹配（至少包含一个标签）

    private Integer minPrice;   // 价格区间下限

    private Integer maxPrice;   // 价格区间上限
}