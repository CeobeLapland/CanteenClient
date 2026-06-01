package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.PostItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ChannelController {

    @FXML private VBox typeButtonBox;
    @FXML private TextField searchField;
    @FXML private VBox postListBox;
    @FXML private HBox pageBar;

    @FXML private Button sortTimeBtn;
    @FXML private Button sortViewsBtn;
    @FXML private Button sortLikesBtn;
    @FXML private Button ascBtn;
    @FXML private Button descBtn;

    private MainController mainController;

    private final ObservableList<PostItem> allPosts = FXCollections.observableArrayList();
    private final ObservableList<PostItem> filteredPosts = FXCollections.observableArrayList();

    private final int pageSize = 20;
    private int currentPage = 1;

    private String selectedType = "全部";

    private enum SortMode { TIME, VIEWS, LIKES }
    private SortMode sortMode = SortMode.TIME;
    private boolean ascending = false; // 默认按降序，更适合时间/热度排序

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        buildTypeButtons();
        buildMockData();     // 这里以后换成你的数据库读取
        applyFilterSort();
        renderPage(1);
        refreshSortButtonStyles();
    }

    private void buildTypeButtons() {
        List<String> types = Arrays.asList("全部", "求助", "二手", "活动", "吐槽", "经验", "表白");

        typeButtonBox.getChildren().clear();
        for (String type : types) {
            Button btn = new Button(type);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.getStyleClass().add("secondary-btn");
            btn.setOnAction(e -> {
                selectedType = type;
                refreshTypeButtonStyles();
                handleSearch();
            });
            typeButtonBox.getChildren().add(btn);
        }
        refreshTypeButtonStyles();
    }

    private void refreshTypeButtonStyles() {
        for (Node node : typeButtonBox.getChildren()) {
            if (node instanceof Button btn) {
                btn.getStyleClass().remove("primary-btn");
                btn.getStyleClass().remove("secondary-btn");
                if (btn.getText().equals(selectedType)) {
                    btn.getStyleClass().add("primary-btn");
                } else {
                    btn.getStyleClass().add("secondary-btn");
                }
            }
        }
    }

    private void buildMockData() {
        allPosts.clear();

        String[] types = {"求助", "二手", "活动", "吐槽", "经验", "表白"};
        String[] authors = {"小林", "阿杰", "Mia", "Leo", "小周", "小陈"};

        for (int i = 1; i <= 86; i++) {
            String type = types[i % types.length];
            String author = authors[i % authors.length];
            LocalDateTime time = LocalDateTime.now().minusHours(i * 3L);

            allPosts.add(new PostItem(
                    String.valueOf(i),
                    type + "帖子标题 " + i,
                    "这里是帖子正文内容示例，编号 " + i + "。你后面把这一段换成数据库里的真实内容就行。"
                            + " 这段文字主要用于展示评论区、点赞、浏览量和推荐栏的效果。",
                    author,
                    time,
                    type,
                    100 + i * 13L,
                    20 + i * 5L,
                    2 + i % 8
            ));
        }
    }

    @FXML
    private void handleCreatePost() {
        if (mainController != null) {
            mainController.showChannelComposePanel();
        }
    }

    @FXML
    private void handleSearch() {
        applyFilterSort();
        renderPage(1);
    }

    @FXML
    private void sortByTime() {
        sortMode = SortMode.TIME;
        refreshSortButtonStyles();
        handleSearch();
    }

    @FXML
    private void sortByViews() {
        sortMode = SortMode.VIEWS;
        refreshSortButtonStyles();
        handleSearch();
    }

    @FXML
    private void sortByLikes() {
        sortMode = SortMode.LIKES;
        refreshSortButtonStyles();
        handleSearch();
    }

    @FXML
    private void sortAscending() {
        ascending = true;
        refreshSortButtonStyles();
        handleSearch();
    }

    @FXML
    private void sortDescending() {
        ascending = false;
        refreshSortButtonStyles();
        handleSearch();
    }

    private void refreshSortButtonStyles() {
        List<Button> sortButtons = Arrays.asList(sortTimeBtn, sortViewsBtn, sortLikesBtn);
        for (Button btn : sortButtons) {
            btn.getStyleClass().remove("primary-btn");
            btn.getStyleClass().remove("secondary-btn");
            btn.getStyleClass().add("page-btn");
        }

        sortTimeBtn.getStyleClass().add(sortMode == SortMode.TIME ? "page-btn-active" : "");
        sortViewsBtn.getStyleClass().add(sortMode == SortMode.VIEWS ? "page-btn-active" : "");
        sortLikesBtn.getStyleClass().add(sortMode == SortMode.LIKES ? "page-btn-active" : "");

        ascBtn.getStyleClass().remove("page-btn-active");
        descBtn.getStyleClass().remove("page-btn-active");
        if (ascending) {
            ascBtn.getStyleClass().add("page-btn-active");
        } else {
            descBtn.getStyleClass().add("page-btn-active");
        }
    }

    private void applyFilterSort() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<PostItem> result = allPosts.stream()
                .filter(post -> {
                    boolean typeMatch = "全部".equals(selectedType) || selectedType.equals(post.getType());
                    boolean keywordMatch = keyword.isBlank()
                            || post.getTitle().toLowerCase(Locale.ROOT).contains(keyword)
                            || post.getContent().toLowerCase(Locale.ROOT).contains(keyword)
                            || post.getAuthor().toLowerCase(Locale.ROOT).contains(keyword)
                            || post.getType().toLowerCase(Locale.ROOT).contains(keyword);
                    return typeMatch && keywordMatch;
                })
                .collect(Collectors.toList());

        Comparator<PostItem> comparator;
        switch (sortMode) {
            case VIEWS:
                comparator = Comparator.comparingLong(PostItem::getViewCount);
                break;
            case LIKES:
                comparator = Comparator.comparingLong(PostItem::getLikeCount);
                break;
            case TIME:
            default:
                comparator = Comparator.comparing(PostItem::getCreatedAt);
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        result.sort(comparator);
        filteredPosts.setAll(result);
    }

    private void renderPage(int page) {
        int totalPages = Math.max(1, (int) Math.ceil(filteredPosts.size() / (double) pageSize));
        currentPage = Math.min(Math.max(page, 1), totalPages);

        postListBox.getChildren().clear();

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredPosts.size());

        List<PostItem> pageItems = filteredPosts.subList(
                Math.min(fromIndex, filteredPosts.size()),
                toIndex
        );

        for (PostItem item : pageItems) {
            postListBox.getChildren().add(createPostCard(item));
        }

        if (pageItems.isEmpty()) {
            Label empty = new Label("没有找到符合条件的帖子");
            empty.setStyle("-fx-text-fill: #5f7290; -fx-font-size: 16px;");
            postListBox.getChildren().add(empty);
        }

        buildPageBar(totalPages);
    }

    private Node createPostCard(PostItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("food-card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(740);
        card.setCursor(Cursor.HAND);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(item.getTitle());
        title.getStyleClass().add("card-title");

        Label type = new Label("[" + item.getType() + "]");
        type.getStyleClass().add("card-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label likes = new Label("点赞 " + item.getLikeCount());
        likes.getStyleClass().add("card-meta");

        Label views = new Label("浏览 " + item.getViewCount());
        views.getStyleClass().add("card-meta");

        top.getChildren().addAll(title, type, spacer, likes, views);

        Label author = new Label("发帖人： " + item.getAuthor() + "    时间： " + item.getShortTime());
        author.getStyleClass().add("card-meta");

        Label preview = new Label(item.getPreview());
        preview.getStyleClass().add("card-meta");
        preview.setWrapText(true);

        card.getChildren().addAll(top, author, preview);

        card.setOnMouseClicked(e -> {
            if (mainController != null) {
                item.addView(); // 进入详情时，视为一次浏览
                mainController.showChannelDetailPanel(item);
            }
        });

        return card;
    }

    private void buildPageBar(int totalPages) {
        pageBar.getChildren().clear();

        Button prev = new Button("上一页");
        prev.getStyleClass().add("page-btn");
        prev.setDisable(currentPage == 1);
        prev.setOnAction(e -> renderPage(currentPage - 1));
        pageBar.getChildren().add(prev);

        for (int i = 1; i <= totalPages; i++) {
            int pageNo = i;
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("page-btn");
            if (i == currentPage) {
                pageBtn.getStyleClass().add("page-btn-active");
            }
            pageBtn.setOnAction(e -> renderPage(pageNo));
            pageBar.getChildren().add(pageBtn);
        }

        Button next = new Button("下一页");
        next.getStyleClass().add("page-btn");
        next.setDisable(currentPage >= totalPages);
        next.setOnAction(e -> renderPage(currentPage + 1));
        pageBar.getChildren().add(next);
    }
}