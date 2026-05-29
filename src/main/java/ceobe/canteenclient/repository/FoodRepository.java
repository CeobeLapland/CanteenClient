package ceobe.canteenclient.repository;

import ceobe.canteenclient.net.ApiResponse;
import ceobe.canteenclient.net.FoodService;
import ceobe.canteenclient.net.PageResponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FoodRepository {







    // ── FXML 注入（对应你的 .fxml 文件）──────────────────────────────
    @FXML
    private TableView<FoodService.Food> foodTable;
    @FXML private Label statusLabel;
    @FXML private TextField nameField;
    @FXML private TextField            caloriesField;
    @FXML private Pagination pagination;

    private final FoodService foodService = FoodService.getInstance();
    private final ObservableList<FoodService.Food> foodList = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        foodTable.setItems(foodList);
        loadPage(0); // 初始化时加载第一页

        // 分页控件联动
        pagination.currentPageIndexProperty().addListener(
                (obs, old, now) -> loadPage(now.intValue()));
    }

    // ════════════════════════════════════════════════════════════════════
    //  示例 1：getFood —— 分页加载
    // ════════════════════════════════════════════════════════════════════

    private void loadPage(int page) {
        statusLabel.setText("加载中…");

        foodService.getFood(
                page,
                PAGE_SIZE,

                // ✅ onSuccess —— 已在 FX 线程，可直接操作 UI
                (ApiResponse<PageResponse<FoodService.Food>> resp) -> {
                    if (resp.isSuccess() && resp.getData() != null) {
                        PageResponse<FoodService.Food> pageData = resp.getData();

                        foodList.setAll(pageData.getContent());

                        // 更新分页控件总页数
                        pagination.setPageCount(
                                Math.max(1, pageData.getTotalPages()));

                        statusLabel.setText(
                                "共 " + pageData.getTotalElements() + " 条记录");
                    } else {
                        statusLabel.setText("加载失败：" + resp.getMessage());
                    }
                },

                // ❌ onError —— 同样在 FX 线程
                (String errMsg) -> {
                    statusLabel.setText("错误：" + errMsg);
                    showAlert("加载失败", errMsg);
                }
        );
    }

    // ════════════════════════════════════════════════════════════════════
    //  示例 2：addFood —— 表单提交
    // ════════════════════════════════════════════════════════════════════

    @FXML
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
    }

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
