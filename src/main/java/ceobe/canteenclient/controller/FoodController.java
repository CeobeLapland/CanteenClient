package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.FoodItem;
import ceobe.canteenclient.net.ApiResponse;
import ceobe.canteenclient.net.FoodService;
import ceobe.canteenclient.net.PageResponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.scene.Node;

import java.util.*;
import java.util.stream.Collectors;

public class FoodController {

    @FXML private ListView<String> tagListView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> campusBox;
    @FXML private ComboBox<String> canteenBox;
    @FXML private ComboBox<String> floorBox;
    @FXML private ComboBox<String> windowBox;
    @FXML private VBox foodListBox;
    @FXML private HBox pageBar;

    private MainController mainController;

    private final ObservableList<FoodItem> allFoods = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> filteredFoods = FXCollections.observableArrayList();

    private final int pageSize = 20;
    private int currentPage = 1;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        setupTags();
        setupFilters();
        buildMockData();

        applyFilters();
        renderPage(1);
    }

    private void setupTags() {
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagListView.setItems(FXCollections.observableArrayList(
                "热销", "米饭", "面食", "粉类", "素食", "荤菜",
                "辣味", "清淡", "早餐", "午餐", "晚餐", "饮品", "甜品"
        ));
    }

    private void setupFilters() {
        List<String> campuses = List.of("全部", "东校区", "西校区", "南校区", "北校区");
        List<String> canteens = List.of("全部", "一食堂", "二食堂", "三食堂", "四食堂");
        List<String> floors = List.of("全部", "1楼", "2楼", "3楼");
        List<String> windows = List.of("全部", "A01", "A02", "B01", "B02", "C01", "C02");

        campusBox.setItems(FXCollections.observableArrayList(campuses));
        canteenBox.setItems(FXCollections.observableArrayList(canteens));
        floorBox.setItems(FXCollections.observableArrayList(floors));
        windowBox.setItems(FXCollections.observableArrayList(windows));

        campusBox.getSelectionModel().selectFirst();
        canteenBox.getSelectionModel().selectFirst();
        floorBox.getSelectionModel().selectFirst();
        windowBox.getSelectionModel().selectFirst();
    }

    private void buildMockData() {
        Random random = new Random(7);

        String[] campuses = {"东校区", "西校区", "南校区", "北校区"};
        String[] canteens = {"一食堂", "二食堂", "三食堂", "四食堂"};
        String[] floors = {"1楼", "2楼", "3楼"};
        String[] windows = {"A01", "A02", "B01", "B02", "C01", "C02"};

        List<List<String>> tagPool = List.of(
                List.of("热销", "米饭", "荤菜"),
                List.of("面食", "午餐", "辣味"),
                List.of("粉类", "晚餐", "清淡"),
                List.of("素食", "早餐"),
                List.of("饮品", "甜品"),
                List.of("热销", "清淡", "素食"),
                List.of("荤菜", "晚餐"),
                List.of("早餐", "米饭")
        );

        for (int i = 1; i <= 72; i++) {
            String campus = campuses[i % campuses.length];
            String canteen = canteens[i % canteens.length];
            String floor = floors[i % floors.length];
            String window = windows[i % windows.length];
            String name = "特色菜 " + i;
            String intro = "这是“" + name + "”的介绍：口味稳定，适合日常就餐，出餐速度较快，适合学生高峰期选择。";
            String location = campus + " · " + canteen + " · " + floor + " · " + window;
            double price = 6.0 + (i % 12) * 1.5;
            double score = 3.5 + (random.nextInt(16) / 10.0);
            String imagePath = null; // 后面你接数据库后可替换成真实图片路径
            List<String> tags = new ArrayList<>(tagPool.get(i % tagPool.size()));

            allFoods.add(new FoodItem(
                    name, intro, campus, canteen, floor, window, location, imagePath, price, score, tags
            ));
        }
    }

    @FXML
    private void clearTags() {
        tagListView.getSelectionModel().clearSelection();
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
        renderPage(1);
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String campus = campusBox.getValue();
        String canteen = canteenBox.getValue();
        String floor = floorBox.getValue();
        String window = windowBox.getValue();

        List<String> selectedTags = new ArrayList<>(tagListView.getSelectionModel().getSelectedItems());

        List<FoodItem> result = allFoods.stream()
                .filter(item -> {
                    boolean keywordMatch = keyword.isBlank()
                            || item.getName().toLowerCase(Locale.ROOT).contains(keyword)
                            || item.getIntro().toLowerCase(Locale.ROOT).contains(keyword)
                            || item.getLocation().toLowerCase(Locale.ROOT).contains(keyword)
                            || item.getWindow().toLowerCase(Locale.ROOT).contains(keyword)
                            || item.getTags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(keyword));

                    boolean campusMatch = campus == null || "全部".equals(campus) || campus.equals(item.getCampus());
                    boolean canteenMatch = canteen == null || "全部".equals(canteen) || canteen.equals(item.getCanteen());
                    boolean floorMatch = floor == null || "全部".equals(floor) || floor.equals(item.getFloor());
                    boolean windowMatch = window == null || "全部".equals(window) || window.equals(item.getWindow());

                    boolean tagMatch = selectedTags.isEmpty()
                            || item.getTags().stream().anyMatch(selectedTags::contains);

                    return keywordMatch && campusMatch && canteenMatch && floorMatch && windowMatch && tagMatch;
                })
                .collect(Collectors.toList());

        filteredFoods.setAll(result);
    }

    private void renderPage(int page) {
        int totalPages = Math.max(1, (int) Math.ceil(filteredFoods.size() / (double) pageSize));
        currentPage = Math.min(Math.max(page, 1), totalPages);

        foodListBox.getChildren().clear();

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredFoods.size());
        List<FoodItem> pageItems = filteredFoods.subList(Math.min(fromIndex, filteredFoods.size()), toIndex);

        for (FoodItem item : pageItems) {
            foodListBox.getChildren().add(createFoodCard(item));
        }

        if (pageItems.isEmpty()) {
            Label empty = new Label("没有找到符合条件的食物");
            empty.setStyle("-fx-text-fill: #5f7290; -fx-font-size: 16px;");
            foodListBox.getChildren().add(empty);
        }

        buildPageBar(totalPages);
    }

    private Node createFoodCard(FoodItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("food-card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(700);
        card.setCursor(Cursor.HAND);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(item.getName());
        name.getStyleClass().add("card-title");

        Label score = new Label(String.format("评分 %.1f", item.getScore()));
        score.getStyleClass().add("card-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label price = new Label(String.format("￥%.1f", item.getPrice()));
        price.getStyleClass().add("card-meta");

        top.getChildren().addAll(name, spacer, score, price);

        Label location = new Label("地点： " + item.getLocation());
        location.getStyleClass().add("card-meta");

        Label tags = new Label("标签： " + item.getTagsText());
        tags.getStyleClass().add("card-meta");

        card.getChildren().addAll(top, location, tags);

        card.setOnMouseClicked(e -> {
            if (mainController != null) {
                mainController.showFoodDetail(item);
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