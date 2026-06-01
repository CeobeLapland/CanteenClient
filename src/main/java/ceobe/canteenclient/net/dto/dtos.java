package ceobe.canteenclient.net.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO（Data Transfer Object）数据传输对象
 * <p>将多个 DTO 集中在一个文件中便于管理，也可拆分为独立文件。
 * DTO 用于向客户端返回数据，不暴露 Entity 内部关联（避免循环序列化）。
 */
public class Dtos {

    public static class UserDto {
        private Long id;
        private String name;
        private String permission; // 例如 "USER"、"ADMIN"
        private LocalDateTime createdAt;

    }




    public static class FoodSummaryDto {
        private Long id;
        private String name;
        private Integer price;
        private String imageUrl;
        // 新增简要字段，方便客户端列表展示
        private String campus;
        private String canteen;
        private Float averageRating;
    }



    public static class FoodDetailDto {
        private Long id;
        private String name;
        private String description;
        private Integer price;
        private String imageUrl;
        private LocalDateTime createdAt;

        // 新增详细信息字段
        private String campus;
        private String canteen;
        private String floor;
        private String window;
        private String sellTime;
        private List<String> tags;
        private Float averageRating;
        private Integer ratingCount;

        /** 该菜品关联的帖子数量（统计信息） */
        private int postCount;
    }



    public static class PostSummaryDto {
        private Long id;
        private String title;

        private Integer viewCount;
        private Integer likeCount;
        private UserDto author;
        private List<FoodSummaryDto> foods;
        private int commentCount;
        private LocalDateTime createdAt;
    }


    public static class PostDetailDto {
        private Long id;
        private String title;
        private String content;

        private Integer viewCount;
        private Integer likeCount;
        private UserDto author;
        private Set<FoodSummaryDto> foods;
        private List<CommentDto> comments;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }





    public static class CommentDto {
        private Long id;
        private String content;
        private UserDto author;
        private LocalDateTime createdAt;
        // 后续扩展（楼中楼）
        // private Long parentId;
        // private List<CommentDto> replies;
    }






    public static class WindowDto {
        private String name;

        private String floor;
        private String canteen;
        private String campus;

        // getters
        public String getName() {
            return name;
        }
        public String getFloor() {
            return floor;
        }
        public String getCanteen() {
            return canteen;
        }
        public String getCampus() {
            return campus;
        }
    }



    public static class TagDto {
        private String name;
    }
}
