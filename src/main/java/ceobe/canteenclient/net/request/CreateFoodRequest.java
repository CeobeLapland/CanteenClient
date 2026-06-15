package ceobe.canteenclient.net.request;

import lombok.Data;

import java.util.List;

/** 新增食物的请求体 */
@Data
public class CreateFoodRequest {

    private String name;

    private String description;

    private Integer price;//单位为分

    //private String imageUrl;
    // 新增字段：地理/销售信息
    private String campusName;
    private String canteenName;
    private String floorName;
    private String windowName;

    private String sellTime;

    // 标签列表，允许为空
    private List<String> tags;

}