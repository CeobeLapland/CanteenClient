package ceobe.canteenclient.net;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 与后端 PageResponse<T> 对齐的分页响应模型。
 * 通常作为 ApiResponse 的 data 字段嵌套传输：
 *   ApiResponse<PageResponse<Food>>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    /** 当前页码，从 0 开始（与后端一致） */
    @JsonProperty("page")
    private int page;

    @JsonProperty("size")
    private int size;

    @JsonProperty("totalElements")
    private long totalElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("last")
    private boolean last;

    // ── Jackson 需要无参构造 ──────────────────────────────────────────
    public PageResponse() {}

    // ── Getters ──────────────────────────────────────────────────────
    public List<T> getContent()       { return content;       }
    public int     getPage()          { return page;          }
    public int     getSize()          { return size;          }
    public long    getTotalElements() { return totalElements; }
    public int     getTotalPages()    { return totalPages;    }
    public boolean isLast()           { return last;          }

    @Override
    public String toString() {
        return "PageResponse{page=" + page + ", size=" + size
                + ", totalElements=" + totalElements
                + ", totalPages=" + totalPages
                + ", last=" + last
                + ", content=" + content + '}';
    }
}
