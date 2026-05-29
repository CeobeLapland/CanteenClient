package ceobe.canteenclient.net;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 与后端 ApiResponse<T> 对齐的响应模型。
 * 后端使用 @JsonInclude(NON_NULL)，故 data / message 可能为 null。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    private String timestamp; // ISO 字符串，前端一般只做展示

    // ── Jackson 需要无参构造 ──────────────────────────────────────────
    public ApiResponse() {}

    // ── Getters ──────────────────────────────────────────────────────
    public boolean isSuccess()  { return success;    }
    public int     getCode()    { return code;       }
    public String  getMessage() { return message;    }
    public T       getData()    { return data;       }
    public String  getTimestamp(){ return timestamp; }

    @Override
    public String toString() {
        return "ApiResponse{success=" + success + ", code=" + code
                + ", message='" + message + "', data=" + data + '}';
    }
}
