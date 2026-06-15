package ceobe.canteenclient.net.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO（Data Transfer Object）数据传输对象
 * <p>将多个 DTO 集中在一个文件中便于管理，也可拆分为独立文件。
 * DTO 用于向客户端返回数据，不暴露 Entity 内部关联（避免循环序列化）。
 */
public class Dtos {

    @Data
    public static class UserDto {
        private Long id;
        private String name;
        private String permission; // 例如 "USER"、"ADMIN"
        private LocalDateTime createdAt;

    }



    @Data
    public static class FoodSummaryDto {
        private Long id;
        private String name;
        private Integer price;

        private String campus;
        private String canteen;
        private Float averageRating;
    }


    @Data
    public static class FoodDetailDto {
        private Long id;
        private String name;
        private String description;
        private Integer price;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        // 新增详细信息字段
        private String campus;
        private String canteen;
        private String floor;
        private String window;
        private String sellTime;

        private List<String> tags;
        private Float averageRating;
        private Integer ratingCount;

    }


    @Data
    public static class PostSummaryDto {
        private Long id;
        private String title;

        private Integer viewCount;
        private Integer likeCount;
        private UserDto author;
        private List<String> foods;
        private int commentCount;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Data
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




    @Data
    public static class CommentDto {
        private Long id;
        private String content;
        private UserDto author;
        private LocalDateTime createdAt;
        // 后续扩展（楼中楼）
        // private Long parentId;
        // private List<CommentDto> replies;
    }





    @Data
    public static class WindowSearchDto {
        private String name;

        private String floor;
        private String canteen;
        private String campus;

    }


    @Data
    public static class TagDto {
        private Long id;
        private String name;
    }

    @Data
    public static class SeasoningDto {
        private Long id;
        private String name;
    }
}
