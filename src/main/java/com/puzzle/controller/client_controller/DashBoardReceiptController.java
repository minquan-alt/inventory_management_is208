package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

@Controller
public class DashBoardReceiptController {

    private static final int LOW_STOCK_THRESHOLD = 20;
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardNVNKGUI.fxml";
    private static final String ISSUE_FXML = "/views/GUI/NhapKhoGUI.fxml";

    @FXML private Button getDashboardView;
    @FXML private Button getReceiptView;
    @FXML private Label helloUser;
    @FXML private Label quantityInventory;
    @FXML private Label warningInventory;
    @FXML private Label username;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private BarChart<String, Number> barChart;

    private UserResponse user;
    @Autowired
    private InventoryService inventoryService;

    @FXML
    public void initialize() {
        setupEventHandlers();
        setupChart();
    }

    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    @FXML private Button logoutButton;


    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đăng xuất");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleViewSwitch(LOGIN_FXML, "Đăng nhập");
        }
    }

    private void setupEventHandlers() {
        logoutButton.setOnAction(event -> handleLogout());
        getDashboardView.setOnAction(event -> handleViewSwitch(DASHBOARD_FXML, "Dashboard"));
        getReceiptView.setOnAction(event -> handleViewSwitch(ISSUE_FXML, "Nhập kho"));
    }

    public void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, logoutButton, user);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        }
    }

    public void initData(UserResponse user) {
        this.user = user;

        if (Platform.isFxApplicationThread()) {
            updateUI();
            loadChartData();
        } else {
            Platform.runLater(() -> {
                updateUI();
                loadChartData();
            });
        }
    }

    private void updateUI() {
        if (user == null) return;

        helloUser.setText("Hello " + user.getName());
        username.setText(user.getUsername());

        Map<String, Integer> inventoryStats = getInventoryStatistics();
        warningInventory.setText(String.valueOf(inventoryStats.get("warning_products")));
        quantityInventory.setText(String.valueOf(inventoryStats.get("total_quantity")));
    }


    private Map<String, Integer> getInventoryStatistics() {
        if (inventoryService == null) {
            return Map.of("warning_products", 0, "total_quantity", 0);
        }

        List<InventoryResponse> inventories = inventoryService.getInventory();
        if (inventories == null || inventories.isEmpty()) {
            return Map.of("warning_products", 0, "total_quantity", 0);
        }

        int warningCount = 0;
        int totalQuantity = 0;

        for (InventoryResponse inventory : inventories) {
            int quantity = inventory.getQuantity();
            if (quantity < LOW_STOCK_THRESHOLD) {
                warningCount++;
            }
            totalQuantity += quantity;
        }

        Map<String, Integer> results = new HashMap<>();
        results.put("warning_products", warningCount);
        results.put("total_quantity", totalQuantity);
        return results;
    }

    private void setupChart() {
        if (barChart == null) {
            AlertUtil.showError("Lỗi: BarChart không được khởi tạo. Kiểm tra tệp FXML.");
            return;
        }

        yAxis.setLabel("Số lượng");
        yAxis.setAutoRanging(true);

        barChart.setTitle("Biểu đồ tồn kho");
        barChart.setBarGap(5);
        barChart.setCategoryGap(20);
        barChart.setLegendVisible(true);
    }

    private void loadChartData() {
        if (barChart == null || inventoryService == null) {
            return;
        }

        List<InventoryResponse> inventories = inventoryService.getInventory();
        if (inventories == null || inventories.isEmpty()) {
            AlertUtil.showError("Không có dữ liệu tồn kho để hiển thị.");
            return;
        }

        List<InventoryResponse> validInventories = inventories.stream()
                .filter(inv -> inv.getProduct_name() != null && !inv.getProduct_name().trim().isEmpty())
                .collect(Collectors.toList());

        if (validInventories.isEmpty()) {
            AlertUtil.showError("Không có dữ liệu hợp lệ để hiển thị.");
            return;
        }

        List<String> productNames = validInventories.stream()
                .map(InventoryResponse::getProduct_name)
                .collect(Collectors.toList());
        xAxis.setCategories(FXCollections.observableArrayList(productNames));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng tồn kho");

        validInventories.forEach(inventory ->
                series.getData().add(new XYChart.Data<>(
                        inventory.getProduct_name(),
                        inventory.getQuantity()
                ))
        );

        // Update chart
        barChart.getData().clear();
        barChart.getData().add(series);

        System.out.println("Chart data loaded with " + series.getData().size() + " data points.");
    }
}