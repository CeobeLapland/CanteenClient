package ceobe.canteenclient.net.request;

import lombok.Data;

import java.util.Set;

/** 发布帖子请求 */
@Data
public class CreatePostRequest {

    private String title;

    private String content;

    private Integer viewCount = 0;   // 可选，默认 0

    private Integer likeCount = 0;   // 可选，默认 0

    /**
     * 帖子作者 ID
     */
    private Long userId;

    /**
     * 关联的菜品 ID 集合（至少关联一道菜）
     * 若不强制要求，可去掉 @NotEmpty
     */
    private Set<Long> foodIds;
}
