package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ProductService productService;

    private static final int LOW_STOCK_THRESHOLD = 20;

    // Constants for FXML paths
    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    private static final String INVENTORY_CHECK_FXML = "/views/GUI/KiemKeGUI.fxml";
    private static final String PRODUCT_MANAGER_FXML = "/views/GUI/SanPhamGUI.fxml";

    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        lowStockLabel.setOnMouseClicked(event -> handleWarningProduct());
        logoutButton.setOnAction(event -> handleLogout());
    }


private void handleWarningProduct() {
    List<InventoryResponse> inventoryProducts = inventoryService.getInventory();
    
    List<InventoryResponse> lowStockProducts = inventoryProducts.stream()
            .filter(product -> product.getQuantity() < LOW_STOCK_THRESHOLD)
            .collect(Collectors.toList());
    
    if (lowStockProducts.isEmpty()) {
        AlertUtil.showInfo("Thông báo", "Không có sản phẩm nào sắp hết hàng!");
        return;
    }
    
    showWarningProductModal(lowStockProducts);
}

private void showWarningProductModal(List<InventoryResponse> lowStockProducts) {
    try {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Cảnh báo sản phẩm sắp hết hàng");
        modalStage.setResizable(false);
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Danh sách sản phẩm sắp hết hàng");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        
        TableView<InventoryResponse> tableView = new TableView<>();
        tableView.setPrefWidth(500);
        tableView.setPrefHeight(300);
        
        TableColumn<InventoryResponse, String> nameColumn = new TableColumn<>("Tên sản phẩm");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        nameColumn.setPrefWidth(300);
        
        TableColumn<InventoryResponse, Integer> quantityColumn = new TableColumn<>("Số lượng");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);
        
        quantityColumn.setCellFactory(column -> {
            return new TableCell<InventoryResponse, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    }
                }
            };
        });
        
        TableColumn<InventoryResponse, String> statusColumn = new TableColumn<>("Trạng thái");
        statusColumn.setCellValueFactory(cellData -> {
            int quantity = cellData.getValue().getQuantity();
            if (quantity == 0) {
                return new SimpleStringProperty("Hết hàng");
            } else if (quantity < LOW_STOCK_THRESHOLD) {
                return new SimpleStringProperty("Sắp hết");
            } else {
                return new SimpleStringProperty("Bình thường");
            }
        });
        statusColumn.setPrefWidth(100);
        
        statusColumn.setCellFactory(column -> {
            return new TableCell<InventoryResponse, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("Hết hàng".equals(item)) {
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        } else if ("Sắp hết".equals(item)) {
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
        
        tableView.getColumns().addAll(nameColumn, quantityColumn, statusColumn);
        
        tableView.setItems(FXCollections.observableArrayList(lowStockProducts));
        
        Label summaryLabel = new Label(String.format("Tổng cộng: %d sản phẩm sắp hết hàng", lowStockProducts.size()));
        summaryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Button closeButton = new Button("Đóng");
        closeButton.setPrefWidth(100);
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        closeButton.setOnAction(e -> modalStage.close());

        HBox buttonLayout = new HBox(10);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(closeButton);
        
        mainLayout.getChildren().addAll(titleLabel, tableView, summaryLabel, buttonLayout);
        
        Scene scene = new Scene(mainLayout);
        modalStage.setScene(scene);
        
        modalStage.centerOnScreen();
        
        modalStage.showAndWait();
        
    } catch (Exception e) {
        AlertUtil.showError("Lỗi khi hiển thị danh sách sản phẩm: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận đăng xuất");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleViewSwitch(LOGIN_FXML, "Đăng nhập");
        }
    }

    public void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, logoutButton, currentUser);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        }
    }

    public void initData(UserResponse user) {
        this.currentUser = user;

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

    // Navigation methods using handleViewSwitch
    @FXML
    public void handleGoToInventoryCheck(ActionEvent event) {
        handleViewSwitch(INVENTORY_CHECK_FXML, "Quản lý kiểm kê");
    }

    @FXML
    public void handleGoToProductManagerController(ActionEvent event) {
        handleViewSwitch(PRODUCT_MANAGER_FXML, "Quản lý sản phẩm");
    }
}