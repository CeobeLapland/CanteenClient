package ceobe.canteenclient.controller;


import ceobe.canteenclient.entity.ConvertUtil;
import ceobe.canteenclient.entity.FoodItem;
import ceobe.canteenclient.net.service.AdminService;
import ceobe.canteenclient.net.service.FoodService;
import com.sun.tools.javac.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;

public class AdminController {


    @FXML
    private void initialize() {
        setupDragAndDrop();
        addRow();
        refreshExcelLabel();
    }

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }







    @FXML private VBox formBox;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane dropZone;
    @FXML private Label excelFileLabel;
    @FXML private Label statusLabel;
    @FXML private Button backButton;

    private final ObservableList<RowEditor> rowEditors = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> importedItems = FXCollections.observableArrayList();

    private File selectedExcelFile;


    //private Consumer<List<FoodItem>> onConfirmedAction;

    private static final String FIELD_STYLE =
            "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #cfe3fb;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 7 10 7 10;";

    private static final String FIELD_ERROR_STYLE =
            "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #ff6b6b;" +
                    "-fx-border-radius: 10;" +
                    "-fx-padding: 7 10 7 10;";

    private static final String ROW_STYLE =
            "-fx-background-color: rgba(255,255,255,0.78);" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: #c3dcf6;" +
                    "-fx-border-radius: 18;" +
                    "-fx-padding: 14;";


    public ObservableList<FoodItem> getImportedItems() {
        return importedItems;
    }

    @FXML
    private void handleBack() {
        mainController.showPersonalPanel();
    }

    @FXML
    private void handleAdd() {
        addRow();
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleRemove() {
        if (rowEditors.size() <= 1) {
            statusLabel.setText("已经是最少的一栏了。");
            return;
        }
        RowEditor last = rowEditors.remove(rowEditors.size() - 1);
        formBox.getChildren().remove(last.root);
        refreshRowTitles();
        statusLabel.setText("已移除最后一栏。");
    }

    @FXML
    private void handleClear() {
        rowEditors.clear();
        formBox.getChildren().clear();
        importedItems.clear();
        selectedExcelFile = null;
        addRow();
        refreshExcelLabel();
        statusLabel.setText("已清空。");
    }

    @FXML
    private void handleChooseExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择 Excel 文件");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xls", "*.xlsx")
        );

        Stage stage = getStage();
        if (stage == null) {
            statusLabel.setText("当前窗口不可用。");
            return;
        }

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedExcelFile = file;
            refreshExcelLabel();
            statusLabel.setText("已选择文件，点击“确定”后才会读取： " + file.getName());
        }
    }

    @FXML
    private void handleConfirm() {
        importedItems.clear();

        int validManualCount = 0;
        int invalidManualCount = 0;
        List<RowEditor> validRowsToRemove = new ArrayList<>();

        for (RowEditor editor : rowEditors) {
            ValidationResult result = validateFromEditor(editor);
            if (result.valid) {
                importedItems.add(result.item);
                validManualCount++;
                validRowsToRemove.add(editor);
            } else {
                invalidManualCount++;
                applyErrors(editor, result.fieldErrors, result.message);
            }
        }

        for (RowEditor editor : validRowsToRemove) {
            formBox.getChildren().remove(editor.root);
        }
        rowEditors.removeAll(validRowsToRemove);

        if (rowEditors.isEmpty()) {
            addRow();
        } else {
            refreshRowTitles();
        }

        int excelCount = 0;
        if (selectedExcelFile != null) {
            List<FoodItem> excelItems = readExcel(selectedExcelFile);
            excelCount = excelItems.size();
            importedItems.addAll(excelItems);
        }

        if (importedItems.isEmpty()) {
            statusLabel.setText("没有导入到有效数据。手动无效栏保留在界面中，Excel 无有效行已跳过。");

            return;
        }

        statusLabel.setText("导入完成：手动有效 " + validManualCount + " 条，手动无效保留 " + invalidManualCount
               + " 条，Excel 有效 " + excelCount + " 条。");
        System.out.println("[AdminController] 导入完成，手动有效 " + validManualCount + " 条，手动无效 " + invalidManualCount
                + " 条，Excel 有效 " + excelCount + " 条。");

        UploadFood();
    }

    private void addRow() {
        RowEditor editor = new RowEditor(rowEditors.size() + 1);
        rowEditors.add(editor);
        formBox.getChildren().add(editor.root);
        refreshRowTitles();
    }

    private void refreshRowTitles() {
        for (int i = 0; i < rowEditors.size(); i++) {
            rowEditors.get(i).titleLabel.setText("第 " + (i + 1) + " 栏");
        }
    }

    private void refreshExcelLabel() {
        if (selectedExcelFile == null) {
            excelFileLabel.setText("未选择文件");
        } else {
            excelFileLabel.setText(selectedExcelFile.getAbsolutePath());
        }
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() && isExcelFile(db.getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragEntered(event -> {
            if (event.getGestureSource() != dropZone) {
                dropZone.setStyle(dropZone.getStyle() + "; -fx-border-color: #4f9ef5; -fx-background-color: #eef6ff;");
            }
        });

        dropZone.setOnDragExited(event -> dropZone.setStyle(
                "-fx-background-color: #f8fbff; -fx-border-color: #95bfe9; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 18; -fx-background-radius: 18;"
        ));

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (isExcelFile(file)) {
                    selectedExcelFile = file;
                    refreshExcelLabel();
                    statusLabel.setText("已拖入文件，点击“确定”后读取： " + file.getName());
                    success = true;
                } else {
                    statusLabel.setText("只支持 .xls 或 .xlsx 文件。");
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean isExcelFile(File file) {
        if (file == null) return false;
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".xls") || name.endsWith(".xlsx");
    }

    private Stage getStage() {
        if (formBox == null || formBox.getScene() == null) {
            return null;
        }
        Scene scene = formBox.getScene();
        return (Stage) scene.getWindow();
    }

    private void closeWindow() {
        Stage stage = getStage();
        if (stage != null) {
            stage.close();
        }
    }

    private ValidationResult validateFromEditor(RowEditor editor) {
        RawFoodInput input = new RawFoodInput();
        input.name = editor.nameField.getText();
        input.description = editor.descriptionField.getText();
        input.price = editor.priceField.getText();
        input.campus = editor.campusField.getText();
        input.canteen = editor.canteenField.getText();
        input.floor = editor.floorField.getText();
        input.window = editor.windowField.getText();
        input.sellTime = editor.sellTimeField.getText();
        input.tags = editor.tagsField.getText();

        return validateInput(input);
    }

    private ValidationResult validateInput(RawFoodInput input) {
        Map<String, String> errors = new LinkedHashMap<>();

        String name = trimToNull(input.name);
        String description = trimToNull(input.description);
        String priceText = trimToNull(input.price);
        String campus = trimToNull(input.campus);
        String canteen = trimToNull(input.canteen);
        String floor = trimToNull(input.floor);
        String window = trimToNull(input.window);
        String sellTime = trimToNull(input.sellTime);
        String tagsText = trimToNull(input.tags);

        if (name == null) errors.put("name", "name 不能为空");
        if (description == null) errors.put("description", "description 不能为空");
        if (priceText == null) {
            errors.put("price", "price 不能为空");
        }
        if (campus == null) errors.put("campus", "campus 不能为空");
        if (canteen == null) errors.put("canteen", "canteen 不能为空");
        if (floor == null) errors.put("floor", "floor 不能为空");
        if (window == null) errors.put("window", "window 不能为空");
        if (sellTime == null) errors.put("sellTime", "sellTime 不能为空");
        if (tagsText == null) errors.put("tags", "tags 不能为空");

        Integer price = null;
        if (priceText != null) {
            try {
                price = Integer.valueOf(priceText);
                if (price < 0) {
                    errors.put("price", "price 不能小于 0");
                }
            } catch (NumberFormatException ex) {
                errors.put("price", "price 必须是整数（单位：分）");
            }
        }

        if (sellTime != null && !isValidSellTime(sellTime)) {
            errors.put("sellTime", "sellTime 格式应为 11:00-14:00，且结束时间要晚于开始时间");
        }

        List<String> tags = parseTags(tagsText);
        if (tags.isEmpty() && tagsText != null) {
            errors.put("tags", "tags 至少要有一个标签");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors, joinMessages(errors));
        }

        FoodItem item = new FoodItem();
        item.setId(null);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCampus(campus);
        item.setCanteen(canteen);
        item.setFloor(floor);
        item.setWindow(window);
        item.setLocation(buildLocation(campus, canteen, floor, window));
        item.setSellTime(sellTime);
        item.setScore(0.0);
        item.setRatingCount(0);
        item.setTags(tags);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        return ValidationResult.valid(item);
    }

    private String buildLocation(String campus, String canteen, String floor, String window) {
        return campus + " " + canteen + " " + floor + " " + window;
    }

    private boolean isValidSellTime(String value) {
        String[] parts = value.split("-");
        if (parts.length != 2) return false;
        try {
            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());
            return start.isBefore(end);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private List<String> parseTags(String raw) {
        List<String> tags = new ArrayList<>();
        if (raw == null) return tags;
        String[] arr = raw.split("[,，\\s]+");
        for (String s : arr) {
            String t = s.trim();
            if (!t.isEmpty() && !tags.contains(t)) {
                tags.add(t);
            }
        }
        return tags;
    }

    private void applyErrors(RowEditor editor, Map<String, String> errors, String message) {
        editor.resetStyles();

        if (errors.containsKey("name")) editor.nameField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("description")) editor.descriptionField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("price")) editor.priceField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("campus")) editor.campusField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("canteen")) editor.canteenField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("floor")) editor.floorField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("window")) editor.windowField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("sellTime")) editor.sellTimeField.setStyle(FIELD_ERROR_STYLE);
        if (errors.containsKey("tags")) editor.tagsField.setStyle(FIELD_ERROR_STYLE);

        editor.errorLabel.setText(message);
    }

    private String joinMessages(Map<String, String> errors) {
        StringBuilder sb = new StringBuilder();
        for (String msg : errors.values()) {
            if (sb.length() > 0) sb.append("；");
            sb.append(msg);
        }
        return sb.toString();
    }

    private List<FoodItem> readExcel(File file) {
        List<FoodItem> result = new ArrayList<>();

        try (InputStream in = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(in)) {

            if (workbook.getNumberOfSheets() == 0) {
                statusLabel.setText("Excel 中没有工作表。");
                return result;
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                statusLabel.setText("Excel 工作表为空。");
                return result;
            }

            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);

            Map<String, Integer> headerMap = buildHeaderMap(firstRow, formatter, evaluator);
            boolean useHeader = hasUsefulHeader(headerMap);

            int startRow = useHeader ? firstRowNum + 1 : firstRowNum;

            for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row, formatter, evaluator)) {
                    continue;
                }

                RawFoodInput input = useHeader
                        ? extractByHeader(row, headerMap, formatter, evaluator)
                        : extractByFixedOrder(row, formatter, evaluator);

                ValidationResult vr = validateInput(input);
                if (vr.valid) {
                    result.add(vr.item);
                }
            }

        } catch (Exception ex) {
            statusLabel.setText("Excel 读取失败：" + ex.getMessage());
        }

        return result;
    }

    private Map<String, Integer> buildHeaderMap(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> map = new HashMap<>();
        if (row == null) return map;

        for (Cell cell : row) {
            String text = normalizeHeader(formatter.formatCellValue(cell, evaluator));
            if (!text.isEmpty()) {
                map.put(text, cell.getColumnIndex());
            }
        }
        return map;
    }

    private boolean hasUsefulHeader(Map<String, Integer> map) {
        int matched = 0;
        if (findHeader(map, "name", "菜名", "名称", "菜品名") >= 0) matched++;
        if (findHeader(map, "description", "描述", "介绍") >= 0) matched++;
        if (findHeader(map, "price", "价格", "单价") >= 0) matched++;
        if (findHeader(map, "campus", "校区") >= 0) matched++;
        if (findHeader(map, "canteen", "食堂") >= 0) matched++;
        if (findHeader(map, "floor", "楼层") >= 0) matched++;
        if (findHeader(map, "window", "窗口") >= 0) matched++;
        if (findHeader(map, "sellTime", "sell_time", "售卖时间", "营业时间") >= 0) matched++;
        if (findHeader(map, "tags", "标签") >= 0) matched++;
        return matched >= 3;
    }

    private int findHeader(Map<String, Integer> map, String... candidates) {
        for (String c : candidates) {
            Integer idx = map.get(normalizeHeader(c));
            if (idx != null) return idx;
        }
        return -1;
    }

    private RawFoodInput extractByHeader(Row row, Map<String, Integer> headerMap, DataFormatter formatter, FormulaEvaluator evaluator) {
        RawFoodInput input = new RawFoodInput();
        input.name = cellValue(row, headerMap, formatter, evaluator, "name", "菜名", "名称", "菜品名");
        input.description = cellValue(row, headerMap, formatter, evaluator, "description", "描述", "介绍");
        input.price = cellValue(row, headerMap, formatter, evaluator, "price", "价格", "单价");
        input.campus = cellValue(row, headerMap, formatter, evaluator, "campus", "校区");
        input.canteen = cellValue(row, headerMap, formatter, evaluator, "canteen", "食堂");
        input.floor = cellValue(row, headerMap, formatter, evaluator, "floor", "楼层");
        input.window = cellValue(row, headerMap, formatter, evaluator, "window", "窗口");
        input.sellTime = cellValue(row, headerMap, formatter, evaluator, "sellTime", "sell_time", "售卖时间", "营业时间");
        input.tags = cellValue(row, headerMap, formatter, evaluator, "tags", "标签");
        return input;
    }

    private RawFoodInput extractByFixedOrder(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        RawFoodInput input = new RawFoodInput();
        input.name = cellValueByIndex(row, 0, formatter, evaluator);
        input.description = cellValueByIndex(row, 1, formatter, evaluator);
        input.price = cellValueByIndex(row, 2, formatter, evaluator);
        input.campus = cellValueByIndex(row, 3, formatter, evaluator);
        input.canteen = cellValueByIndex(row, 4, formatter, evaluator);
        input.floor = cellValueByIndex(row, 5, formatter, evaluator);
        input.window = cellValueByIndex(row, 6, formatter, evaluator);
        input.sellTime = cellValueByIndex(row, 7, formatter, evaluator);
        input.tags = cellValueByIndex(row, 8, formatter, evaluator);
        return input;
    }

    private String cellValue(Row row, Map<String, Integer> headerMap, DataFormatter formatter,
                             FormulaEvaluator evaluator, String... candidates) {
        for (String c : candidates) {
            Integer idx = headerMap.get(normalizeHeader(c));
            if (idx != null) {
                return cellValueByIndex(row, idx, formatter, evaluator);
            }
        }
        return null;
    }

    private String cellValueByIndex(Row row, int index, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null || index < 0) return null;
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell, evaluator);
        return trimToNull(value);
    }

    private boolean isBlankRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) return true;

        short lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) return true;

        for (int i = row.getFirstCellNum(); i < lastCellNum; i++) {
            String v = cellValueByIndex(row, i, formatter, evaluator);
            if (v != null) return false;
        }
        return true;
    }

    private String normalizeHeader(String text) {
        if (text == null) return "";
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
    }

    private String trimToNull(String text) {
        if (text == null) return null;
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }

    private void createTextFieldStyle(TextInputControl control) {
        control.setStyle(FIELD_STYLE);
    }




    // 先用ConvertUtil把FoodItem转换成CreateFoodRequest，再调用FoodService的addFoodsBatch方法批量上传
    private void UploadFood()
    {
        //这里上传的是副本，避免在上传过程中被修改导致不一致
        List<FoodItem> itemsToUpload = new ArrayList<>(importedItems);
        if (itemsToUpload.isEmpty()) {
            statusLabel.setText("没有数据可上传。");
            return;
        }

        AdminService.getInstance().addFoodsBatch(
                ConvertUtil.itemListToCreateRequestList(itemsToUpload),
                response -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("上传成功！共 " + itemsToUpload.size() + " 条记录。");
                        System.out.println("[AdminController] 上传成功！共 " + itemsToUpload.size() + " 条记录。");
                        importedItems.clear();
                    });
                },
                error -> Platform.runLater(() -> statusLabel.setText("上传失败：" + error))
        );

    }





    private class RowEditor {
        final VBox root = new VBox(8);
        final Label titleLabel = new Label();
        final Label errorLabel = new Label();

        final TextField nameField = new TextField();
        final TextArea descriptionField = new TextArea();
        final TextField priceField = new TextField();
        final TextField campusField = new TextField();
        final TextField canteenField = new TextField();
        final TextField floorField = new TextField();
        final TextField windowField = new TextField();
        final TextField sellTimeField = new TextField();
        final TextField tagsField = new TextField();

        RowEditor(int index) {
            titleLabel.setText("第 " + index + " 栏");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f4f8a;");

            root.setStyle(ROW_STYLE);

            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(10);

            ColumnConstraints labelCol = new ColumnConstraints();
            labelCol.setMinWidth(120);

            ColumnConstraints fieldCol = new ColumnConstraints();
            fieldCol.setHgrow(Priority.ALWAYS);

            grid.getColumnConstraints().addAll(labelCol, fieldCol);

            prepareField(nameField, "name");
            prepareField(descriptionField, "description");
            prepareField(priceField, "price(分)");
            prepareField(campusField, "campus");
            prepareField(canteenField, "canteen");
            prepareField(floorField, "floor");
            prepareField(windowField, "window");
            prepareField(sellTimeField, "sellTime，例如 11:00-14:00");
            prepareField(tagsField, "tags，用逗号或空格分隔");

            descriptionField.setPrefRowCount(2);
            descriptionField.setWrapText(true);

            addRow(grid, 0, "name", nameField);
            addRow(grid, 1, "description", descriptionField);
            addRow(grid, 2, "price", priceField);
            addRow(grid, 3, "campus", campusField);
            addRow(grid, 4, "canteen", canteenField);
            addRow(grid, 5, "floor", floorField);
            addRow(grid, 6, "window", windowField);
            addRow(grid, 7, "sellTime", sellTimeField);
            addRow(grid, 8, "tags", tagsField);

            errorLabel.setStyle("-fx-text-fill: #d64545; -fx-font-size: 12px;");
            errorLabel.setWrapText(true);

            root.getChildren().addAll(titleLabel, grid, errorLabel);
        }

        private void prepareField(TextInputControl control, String prompt) {
            control.setPromptText(prompt);
            createTextFieldStyle(control);
            if (control instanceof TextArea) {
                ((TextArea) control).setPrefHeight(64);
            }
            if (control instanceof Region) {
                ((Region) control).setMaxWidth(Double.MAX_VALUE);
            }
        }

        private void addRow(GridPane grid, int rowIndex, String labelText, TextInputControl control) {
            Label label = new Label(labelText);
            label.setStyle("-fx-font-size: 13px; -fx-text-fill: #2f5d8a; -fx-font-weight: bold;");

            grid.add(label, 0, rowIndex);
            grid.add(control, 1, rowIndex);
            GridPane.setHgrow(control, Priority.ALWAYS);
            control.setMaxWidth(Double.MAX_VALUE);
        }

        void resetStyles() {
            nameField.setStyle(FIELD_STYLE);
            descriptionField.setStyle(FIELD_STYLE);
            priceField.setStyle(FIELD_STYLE);
            campusField.setStyle(FIELD_STYLE);
            canteenField.setStyle(FIELD_STYLE);
            floorField.setStyle(FIELD_STYLE);
            windowField.setStyle(FIELD_STYLE);
            sellTimeField.setStyle(FIELD_STYLE);
            tagsField.setStyle(FIELD_STYLE);
            errorLabel.setText("");
        }
    }

    private static class RawFoodInput {
        String name;
        String description;
        String price;
        String campus;
        String canteen;
        String floor;
        String window;
        String sellTime;
        String tags;
    }

    private static class ValidationResult {
        final boolean valid;
        final FoodItem item;
        final Map<String, String> fieldErrors;
        final String message;

        private ValidationResult(boolean valid, FoodItem item, Map<String, String> fieldErrors, String message) {
            this.valid = valid;
            this.item = item;
            this.fieldErrors = fieldErrors;
            this.message = message;
        }

        static ValidationResult valid(FoodItem item) {
            return new ValidationResult(true, item, Collections.emptyMap(), null);
        }

        static ValidationResult invalid(Map<String, String> fieldErrors, String message) {
            return new ValidationResult(false, null, fieldErrors, message);
        }
    }
}