package ceobe.canteenclient.net.request;

import lombok.Data;

import java.util.Set;

/** 更新帖子请求（允许部分字段为空，表示不更新） */
@Data
public class UpdatePostRequest {

    private String title;

    private String content;

    private Integer viewCount;

    private Integer likeCount;

    private Set<Long> foodIds;
}