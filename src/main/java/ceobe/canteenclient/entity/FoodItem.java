package ceobe.canteenclient.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

//@Data 注解会自动生成 getter、setter、toString、equals 和 hashCode 方法
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {
    //把所有的final都删了
    private Long id;//目前用不到
    
    private String name;
    private String description;
    private Integer price;//单位为分

    private String campus;
    private String canteen;
    private String floor;
    private String window;

    private String location;//由 campus + canteen + floor + window 组成，方便展示
    
    private String sellTime;//例如 "11:00-14:00"

    //private String imagePath;
    private double score;//默认0.0，满分5.0
    private Integer ratingCount;//默认0
    
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public String getTagsText() {
        if (tags == null || tags.isEmpty()) {
            return "无标签";
        }
        return String.join(", ", tags);
    }
}