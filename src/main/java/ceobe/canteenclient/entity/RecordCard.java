package ceobe.canteenclient.entity;

public class RecordCard {
    private final String author;
    private final String title;
    private final String viewCount;
    private final String likeCount;
    private final String browseTime;

    public RecordCard(String author, String title, String viewCount, String likeCount, String browseTime) {
        this.author = author;
        this.title = title;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.browseTime = browseTime;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getViewCount() {
        return viewCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getBrowseTime() {
        return browseTime;
    }
}