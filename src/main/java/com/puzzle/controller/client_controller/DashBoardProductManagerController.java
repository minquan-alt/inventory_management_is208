package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;

import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashBoardProductManagerController {
    @FXML private Label welcomeLabel;
    @FXML private Label inventoryLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label userNameLabel;

    @FXML private BarChart<String, Number> monthlyChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;


    private UserResponse currentUser;
    private InventoryService inventoryService;
    private ProductService productService;

    private static final int LOW_STOCK_THRESHOLD = 20;

    public void initData(UserResponse user, InventoryService inventoryService, ProductService productService) {
        this.currentUser = user;
        this.inventoryService = inventoryService;
        this.productService = productService;

        if (Platform.isFxApplicationThread()) {
            updateDashboard();
        } else {
            Platform.runLater(this::updateDashboard);
        }
    }
    private void updateDashboard() {
        if (currentUser != null) {
            welcomeLabel.setText("Hello " + currentUser.getName() + ", welcome back!");
            userNameLabel.setText(currentUser.getUsername());
        }

        Task<List<InventoryResponse>> task = new Task<>() {
            @Override
            protected List<InventoryResponse> call() {
                return inventoryService.getInventory();
            }
        };

        task.setOnSucceeded(event -> {
            List<InventoryResponse> inventoryList = task.getValue();
            if (inventoryList == null || inventoryList.isEmpty()) {
                inventoryLabel.setText("0");
                lowStockLabel.setText("0");
                return;
            }

            int totalQuantity = inventoryList.stream().mapToInt(InventoryResponse::getQuantity).sum();
            long lowStockCount = inventoryList.stream().filter(i -> i.getQuantity() < LOW_STOCK_THRESHOLD).count();

            inventoryLabel.setText(String.valueOf(totalQuantity));
            lowStockLabel.setText(String.valueOf(lowStockCount));

            renderChart(inventoryList);
        });

        task.setOnFailed(e -> {
            System.err.println("Lỗi tải dữ liệu dashboard: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }


    private void renderChart(List<InventoryResponse> inventoryList) {
        if (monthlyChart == null || xAxis == null) return;

        List<InventoryResponse> valid = inventoryList.stream()
                .filter(i -> i.getProduct_name() != null && !i.getProduct_name().isBlank())
                .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tồn kho sản phẩm");

        valid.forEach(item -> {
            series.getData().add(new XYChart.Data<>(item.getProduct_name(), item.getQuantity()));
        });

        xAxis.setCategories(FXCollections.observableArrayList(
                valid.stream().map(InventoryResponse::getProduct_name).collect(Collectors.toList())
        ));

        monthlyChart.setAnimated(false);

        monthlyChart.getData().clear();
        monthlyChart.getData().add(series);
    }

    public void handleGoToInventoryCheck(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/KiemKeGUI.fxml"));
            Parent root = loader.load();

            InventoryCheckController controller = loader.getController();

            controller.initData(currentUser, inventoryService, productService);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý kiểm kê");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}