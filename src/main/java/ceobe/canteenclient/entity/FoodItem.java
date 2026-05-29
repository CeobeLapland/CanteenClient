package ceobe.canteenclient.entity;

import java.util.List;

public class FoodItem {
    private final String name;
    private final String intro;
    private final String campus;
    private final String canteen;
    private final String floor;
    private final String window;
    private final String location;
    private final String imagePath;
    private final double price;
    private final double score;
    private final List<String> tags;

    public FoodItem(String name,
                    String intro,
                    String campus,
                    String canteen,
                    String floor,
                    String window,
                    String location,
                    String imagePath,
                    double price,
                    double score,
                    List<String> tags) {
        this.name = name;
        this.intro = intro;
        this.campus = campus;
        this.canteen = canteen;
        this.floor = floor;
        this.window = window;
        this.location = location;
        this.imagePath = imagePath;
        this.price = price;
        this.score = score;
        this.tags = tags;
    }

    public String getName() { return name; }
    public String getIntro() { return intro; }
    public String getCampus() { return campus; }
    public String getCanteen() { return canteen; }
    public String getFloor() { return floor; }
    public String getWindow() { return window; }
    public String getLocation() { return location; }
    public String getImagePath() { return imagePath; }
    public double getPrice() { return price; }
    public double getScore() { return score; }
    public List<String> getTags() { return tags; }

    public String getTagsText() {
        return String.join(" / ", tags);
    }
}