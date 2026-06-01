package ceobe.canteenclient.controller;

import ceobe.canteenclient.entity.FoodItem;

import ceobe.canteenclient.net.ApiResponse;
import ceobe.canteenclient.net.FoodService;
import ceobe.canteenclient.net.PageResponse;
import ceobe.canteenclient.net.dto.Dtos;
import ceobe.canteenclient.net.dto.Dtos.WindowDto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.Node;

import java.util.*;
import java.util.stream.Collectors;

public class FoodController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> campusBox;
    @FXML private ComboBox<String> canteenBox;
    @FXML private ComboBox<String> floorBox;
    @FXML private ComboBox<String> windowBox;
    @FXML private VBox foodListBox;
    @FXML private HBox pageBar;

    private MainController mainController;
    private FoodService foodService = FoodService.getInstance();

    private final int pageSize = 20;
    private int currentPage = 1;

    private final ObservableList<FoodItem> allFoods = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> filteredFoods = FXCollections.observableArrayList();


    private String selectedCampus, selectedCanteen, selectedFloor, selectedWindow;
    private List<Dtos.WindowDto> allWindows = new ArrayList<>();


    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        // 1. 初始化可见性：仅【校区】下拉框可见
        resetAllComboBoxVisibility();
        campusBox.setVisible(true);
        // 2. 加载初始校区选项
        loadCampusOptions();
        // 3. 绑定所有下拉框的选择事件
        bindSelectionEvents();


        foodService.test(
                (ApiResponse<String> resp) -> {
                    if (resp.isSuccess()) {
                        System.out.println("接口测试成功，响应数据：" + resp.getData());
                    } else {
                        System.err.println("接口测试失败，错误信息：" + resp.getMessage());
                    }
                },
                (String errMsg) -> {
                    System.err.println("接口测试请求错误，错误信息：" + errMsg);
                }
        );

        initTags();

        buildMockData();


        applyFilters();
        renderPage(1);
    }


    //region 标签云相关
    // ===================== FXML 节点 =====================
    @FXML
    private ScrollPane tagScrollPane;   // 滚动面板（纵向滚动）
    @FXML
    private FlowPane tagFlowPane;       // 自动换行面板（放标签按钮）

    // ===================== 数据 =====================
    private List<String> allTags = new ArrayList<>();  // 所有标签
    private List<String> curTags = new ArrayList<>();  // 当前选中的标签
    private List<Button> curTagButtons = new ArrayList<>(); // 当前标签按钮（用于样式切换）


    public void initTags() {
        // 1. 设置滚动面板：只纵向滚动，横向不滚动
        tagScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tagScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // 2. FlowPane 自动换行配置
        tagFlowPane.setHgap(8);    // 按钮横向间距
        tagFlowPane.setVgap(8);    // 按钮纵向间距
        tagFlowPane.setPrefWrapLength(320); // 自动换行宽度（和你面板宽度一致）

        // 3. 测试数据（你可以替换成自己的）
        allTags = List.of(
                "早餐", "午餐", "晚餐", "小吃", "快餐", "麻辣烫", "麻辣香锅",
                "清真", "面食", "米饭", "奶茶", "咖啡", "水果", "减脂餐",
                "麻辣", "清淡", "油炸", "汤面", "炒饭", "盖浇饭", "包子粥铺",
                "热销", "米饭", "面食", "粉类", "素食", "荤菜",
                "辣味", "清淡", "早餐", "午餐", "晚餐", "饮品", "甜品"
        );

        // 4. 渲染标签
        renderTags();
    }

    // ===================== 核心：动态生成自适应宽度标签按钮 =====================
    private void renderTags() {
        tagFlowPane.getChildren().clear(); // 清空旧标签

        for (String tag : allTags) {
            Button tagBtn = new Button(tag);
            tagBtn.setStyle("""
                    -fx-background-color: #f5f5f5;
                    -fx-text-fill: #333;
                    -fx-padding: 6 12;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-cursor: hand;
                    """);

            // 自适应宽度（根据文字自动撑开，不固定宽度）
            tagBtn.setPrefWidth(Button.USE_COMPUTED_SIZE);

            // 点击事件：切换选中状态
            tagBtn.setOnAction(e -> toggleTag(tagBtn, tag));

            tagFlowPane.getChildren().add(tagBtn);
        }
    }

    // ===================== 标签选中/取消切换 =====================
    private void toggleTag(Button btn, String tag) {
        if (curTags.contains(tag)) {
            // 取消选中
            curTags.remove(tag);
            curTagButtons.remove(btn);
            btn.setStyle("""
                    -fx-background-color: #f5f5f5;
                    -fx-text-fill: #333;
                    -fx-padding: 6 12;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-cursor: hand;
                    """);
        } else {
            // 选中
            curTags.add(tag);
            curTagButtons.add(btn);
            btn.setStyle("""
                    -fx-background-color: #4285F4;
                    -fx-text-fill: white;
                    -fx-padding: 6 12;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-cursor: hand;
                    """);
        }
    }

    // ===================== 外部获取选中的标签 =====================
    public List<String> getCurTags() {
        return curTags;
    }

    // ===================== 外部设置标签数据（刷新用） =====================
    public void setAllTags(List<String> allTags) {
        this.allTags = allTags;
        this.curTags.clear();
        this.curTagButtons.clear();
        renderTags();
    }

    // 清除所有标签选中状态
    @FXML
    private void clearTags() {
        this.curTags.clear();
        for (Button btn : curTagButtons) {
            btn.setStyle("""
                    -fx-background-color: #f5f5f5;
                    -fx-text-fill: #333;
                    -fx-padding: 6 12;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-cursor: hand;
                    """);
        }
        this.curTagButtons.clear();
    }
    //endregion

    //region 下拉框筛选相关
    // ====================== 对外方法：设置数据源 ======================
    // 外部加载完数据后调用此方法刷新筛选框
    /*public void setWindowData(List<WindowDto> windowList) {
        allWindows.clear();
        allWindows.addAll(windowList);
        loadCampusOptions();
    }*/

    // ====================== 核心：选项加载方法 ======================
    /**
     * 加载【校区】选项（初始加载）
     */
    private void loadCampusOptions() {
        campusBox.getItems().clear();
        campusBox.getItems().add("全部");
        // 提取所有不重复校区（Stream去重，轻量高效）
        List<String> campuses = allWindows.stream()
                .map(WindowDto::getCampus)
                .distinct()
                .toList();
        campusBox.getItems().addAll(campuses);
        campusBox.setValue("全部");
        selectedCampus = null;
    }

    /**
     * 加载【食堂】选项（根据选中的校区）
     */
    private void loadCanteenOptions(String targetCampus) {
        canteenBox.getItems().clear();
        canteenBox.getItems().add("全部");
        List<String> canteens = allWindows.stream()
                .filter(dto -> targetCampus.equals(dto.getCampus()))
                .map(WindowDto::getCanteen)
                .distinct()
                .toList();
        canteenBox.getItems().addAll(canteens);
        canteenBox.setValue("全部");
        selectedCanteen = null;

        // 重置后续控件
        floorBox.setVisible(false);
        windowBox.setVisible(false);
        selectedFloor = null;
        selectedWindow = null;
    }

    /**
     * 加载【楼层】选项（根据选中的校区+食堂）
     */
    private void loadFloorOptions(String targetCampus, String targetCanteen) {
        floorBox.getItems().clear();
        floorBox.getItems().add("全部");
        List<String> floors = allWindows.stream()
                .filter(dto -> targetCampus.equals(dto.getCampus())
                        && targetCanteen.equals(dto.getCanteen()))
                .map(WindowDto::getFloor)
                .distinct()
                .toList();
        floorBox.getItems().addAll(floors);
        floorBox.setValue("全部");
        selectedFloor = null;

        // 重置后续控件
        windowBox.setVisible(false);
        selectedWindow = null;
    }

    /**
     * 加载【窗口】选项（根据选中的校区+食堂+楼层）
     */
    private void loadWindowOptions(String targetCampus, String targetCanteen, String targetFloor) {
        windowBox.getItems().clear();
        windowBox.getItems().add("全部");
        List<String> windows = allWindows.stream()
                .filter(dto -> targetCampus.equals(dto.getCampus())
                        && targetCanteen.equals(dto.getCanteen())
                        && targetFloor.equals(dto.getFloor()))
                .map(WindowDto::getName)
                .distinct()
                .toList();
        windowBox.getItems().addAll(windows);
        windowBox.setValue("全部");
        selectedWindow = null;
    }

    // ====================== 核心：选择事件绑定 ======================
    private void bindSelectionEvents() {
        // 1. 校区选择事件
        campusBox.setOnAction(e -> {
            selectedCampus = "全部".equals(campusBox.getValue()) ? null : campusBox.getValue();
            handleCampusChange();
        });

        // 2. 食堂选择事件
        canteenBox.setOnAction(e -> {
            selectedCanteen = "全部".equals(canteenBox.getValue()) ? null : canteenBox.getValue();
            handleCanteenChange();
        });

        // 3. 楼层选择事件
        floorBox.setOnAction(e -> {
            selectedFloor = "全部".equals(floorBox.getValue()) ? null : floorBox.getValue();
            handleFloorChange();
        });

        // 4. 窗口选择事件（无后续控件）
        windowBox.setOnAction(e -> {
            selectedWindow = "全部".equals(windowBox.getValue()) ? null : windowBox.getValue();
        });
    }

    // ====================== 选择变更处理（显隐+逻辑） ======================
    private void handleCampusChange() {
        if (selectedCampus == null) {
            // 选【全部】：隐藏所有后续控件
            canteenBox.setVisible(false);
            floorBox.setVisible(false);
            windowBox.setVisible(false);
            selectedCanteen = selectedFloor = selectedWindow = null;
            return;
        }
        // 选中具体校区：显示食堂，加载对应数据
        canteenBox.setVisible(true);
        loadCanteenOptions(selectedCampus);
    }

    private void handleCanteenChange() {
        if (selectedCanteen == null) {
            // 选【全部】：隐藏楼层、窗口
            floorBox.setVisible(false);
            windowBox.setVisible(false);
            selectedFloor = selectedWindow = null;
            return;
        }
        // 选中具体食堂：显示楼层，加载对应数据
        floorBox.setVisible(true);
        loadFloorOptions(selectedCampus, selectedCanteen);
    }

    private void handleFloorChange() {
        if (selectedFloor == null) {
            // 选【全部】：隐藏窗口
            windowBox.setVisible(false);
            selectedWindow = null;
            return;
        }
        // 选中具体楼层：显示窗口，加载对应数据
        windowBox.setVisible(true);
        loadWindowOptions(selectedCampus, selectedCanteen, selectedFloor);
    }

    // ====================== 工具方法 ======================
    /**
     * 重置所有下拉框为不可见
     */
    private void resetAllComboBoxVisibility() {
        campusBox.setVisible(false);
        canteenBox.setVisible(false);
        floorBox.setVisible(false);
        windowBox.setVisible(false);
    }

    // ====================== 对外获取选中结果 ======================
    // 你可以通过这些getter获取最终筛选条件
    public String getSelectedCampus() { return selectedCampus; }
    public String getSelectedCanteen() { return selectedCanteen; }
    public String getSelectedFloor() { return selectedFloor; }
    public String getSelectedWindow() { return selectedWindow; }

    // endregion



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

        for (int i = 1; i <= 36; i++) {
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
    private void handleSearch() {
        //applyFilters();
        renderPage(1);
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
                //mainController.showFoodDetail(item);
                mainController.hadleFoodItemSelected(item);
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








    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        String campus = campusBox.getValue();
        String canteen = canteenBox.getValue();
        String floor = floorBox.getValue();
        String window = windowBox.getValue();

        List<String> selectedTags = curTags; //new ArrayList<>(tagListView.getSelectionModel().getSelectedItems());

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


    // ════════════════════════════════════════════════════════════════════
    //  示例 1：getFood —— 分页加载
    // ════════════════════════════════════════════════════════════════════

    /*private void loadPage(int page) {
        //statusLabel.setText("加载中…");
        foodService.getFoodPaged(
                page,
                pageSize,
                // ✅ onSuccess —— 已在 FX 线程，可直接操作 UI
                (ApiResponse<PageResponse<Dtos.FoodDetailDto>> resp) -> {
                    if (resp.isSuccess() && resp.getData() != null) {
                        PageResponse<Dtos.FoodDetailDto> pageData = resp.getData();

                        allFoods.setAll(pageData.getContent());

                        // 更新分页控件总页数
                        pagination.setPageCount(Math.max(1, pageData.getTotalPages()));

                        //statusLabel.setText("共 " + pageData.getTotalElements() + " 条记录");
                        System.out.println("加载成功，当前页数据：" + pageData.getContent());
                    } else {
                        //statusLabel.setText("加载失败：" + resp.getMessage());
                    }
                },

                // ❌ onError —— 同样在 FX 线程
                (String errMsg) -> {
                    statusLabel.setText("错误：" + errMsg);
                    showAlert("加载失败", errMsg);
                }
        );
    }*/

    // ════════════════════════════════════════════════════════════════════
    //  示例 2：addFood —— 表单提交
    // ════════════════════════════════════════════════════════════════════

    /*@FXML
    private void onAddFoodButtonClick() {
        String name     = nameField.getText().trim();
        String calStr   = caloriesField.getText().trim();

        // 简单前端校验
        if (name.isEmpty() || calStr.isEmpty()) {
            showAlert("提示", "请填写完整信息");
            return;
        }

        double calories;
        try {
            calories = Double.parseDouble(calStr);
        } catch (NumberFormatException e) {
            showAlert("提示", "卡路里格式不正确");
            return;
        }

        FoodService.FoodRequest req = new FoodService.FoodRequest(name, calories);
        statusLabel.setText("提交中…");

        foodService.addFood(
                req,

                // ✅ onSuccess
                (ApiResponse<FoodService.Food> resp) -> {
                    if (resp.isSuccess()) {
                        statusLabel.setText("添加成功：" + resp.getData().name);
                        nameField.clear();
                        caloriesField.clear();
                        loadPage(0); // 刷新列表
                    } else {
                        statusLabel.setText("添加失败：" + resp.getMessage());
                    }
                },

                // ❌ onError
                (String errMsg) -> {
                    statusLabel.setText("错误：" + errMsg);
                    showAlert("添加失败", errMsg);
                }
        );
    }*/

    // ════════════════════════════════════════════════════════════════════
    //  工具
    // ════════════════════════════════════════════════════════════════════

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}