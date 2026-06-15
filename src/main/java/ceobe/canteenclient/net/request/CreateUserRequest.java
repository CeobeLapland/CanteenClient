package ceobe.canteenclient.net.request;

import lombok.Data;

/** 创建用户请求 */
@Data
public class CreateUserRequest {

    private String name;

    // TODO: 后续添加 email、password 等字段
}
