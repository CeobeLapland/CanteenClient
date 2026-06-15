package ceobe.canteenclient.net.request;

import lombok.Data;

/** 发布评论请求 */
@Data
public class CreateCommentRequest {
    private String content;

    private Long userId;
}