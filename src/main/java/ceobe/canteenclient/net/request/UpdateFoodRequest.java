package ceobe.canteenclient.net.request;

import lombok.Data;

import java.util.List;

/** 更新菜品请求（允许部分字段为空，表示不更新） */
@Data
public class UpdateFoodRequest {

    private String name;

    private String description;

    private Integer price;

    //private String imageUrl;

    // 新增字段：地理/销售信息
    private String campusName;
    private String canteenName;
    private String floorName;

    private String windowName;//创建请求时不知道ID

    private String sellTime;

    /** 标签列表，允许为空 */
    private List<String> tags;
}