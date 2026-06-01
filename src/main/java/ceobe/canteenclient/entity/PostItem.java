package ceobe.canteenclient.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PostItem {
    private final String id;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;
    private final String type;

    private long viewCount;
    private long likeCount;
    private long commentCount;

    public PostItem(String id,
                    String title,
                    String content,
                    String author,
                    LocalDateTime createdAt,
                    String type,
                    long viewCount,
                    long likeCount,
                    long commentCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.type = type;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getType() { return type; }
    public long getViewCount() { return viewCount; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }

    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }

    public void addView() { this.viewCount++; }
    public void addLike() { this.likeCount++; }
    public void addComment() { this.commentCount++; }

    public String getShortTime() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getPreview() {
        if (content == null) return "";
        String text = content.replaceAll("\\s+", " ").trim();
        return text.length() <= 60 ? text : text.substring(0, 60) + "...";
    }
}