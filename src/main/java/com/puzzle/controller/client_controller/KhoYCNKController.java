package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.puzzle.config.FXSessionManager;
import com.puzzle.dto.response.StockInDetailsResponse;
import com.puzzle.dto.response.StockInResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import com.puzzle.utils.AlertUtil;
import com.puzzle.utils.SceneManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpSession;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Controller
public class KhoYCNKController implements Initializable {

    // Constants for FXML paths
    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    private static final String KIEM_KE_FXML = "/views/GUI/KiemKeGUI.fxml";
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardQLKGUI.fxml";
    private static final String YEU_CAU_XUAT_KHO_FXML = "/views/GUI/KhoYCXKGUI.fxml";
    private static final String YEU_CAU_NHAP_KHO_FXML = "/views/GUI/KhoYCNKGUI.fxml";
    private static final String PRODUCT_MANAGER_FXML = "/views/GUI/SanPhamGUI.fxml";

    @FXML private Button logoutButton;
    @FXML private TableView<StockInDisplayRow> stockInTable;
    @FXML private TableColumn<StockInDisplayRow, Long> requestIdColumn;
    @FXML private TableColumn<StockInDisplayRow, String> productNameColumn;
    @FXML private TableColumn<StockInDisplayRow, Integer> quantityColumn;
    @FXML private TableColumn<StockInDisplayRow, String> statusColumn;
    @FXML private Label userNameLabel;

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ProductService productService;
    
    private UserResponse currentUser;

    public void initData(UserResponse user) {
        this.currentUser = user;
        userNameLabel.setText(user.getName());
        loadData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logoutButton.setOnAction(event -> handleLogout());
        
        requestIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRequestId()));
        productNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProductName()));
        quantityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));

        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus()));
        statusColumn.setCellFactory(col -> {
            ComboBox<String> comboBox = new ComboBox<>();
            return new TableCell<>() {
                {
                    comboBox.getItems().addAll("PENDING", "APPROVED", "DECLINED");
                    comboBox.setOnAction(e -> {
                        StockInDisplayRow row = getTableView().getItems().get(getIndex());
                        String selected = comboBox.getValue();
                        if (!row.getStatus().equals(selected)) {
                            row.setStatus(selected);
                            updateStatus(row.getRequestId(), selected);
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            };
        });
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

    private void handleViewSwitch(String fxmlPath, String viewName) {
        try {
            SceneManager.switchScene(fxmlPath, logoutButton, currentUser);
        } catch (IOException e) {
            AlertUtil.showError("Lỗi khi chuyển đến trang " + viewName + ": " + e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Lỗi không xác định khi chuyển trang: " + e.getMessage());
        }
    }

    private void updateStatus(Long requestId, String newStatus) {
        try {
            HttpSession session = FXSessionManager.getSession(); // Lấy session thật

            switch (newStatus) {
                case "APPROVED" -> inventoryService.approveStockInRequest(requestId, session);
                case "DECLINED" -> inventoryService.declinedStockInRequest(requestId, session);
                default -> throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText("Cập nhật trạng thái thành công!");
                alert.showAndWait();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Lỗi");
                alert.setContentText("Không thể cập nhật trạng thái: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private void loadData() {
        List<StockInResponse> requests = inventoryService.getStockInRequests();
        List<StockInDisplayRow> rows = new ArrayList<>();

        for (StockInResponse req : requests) {
            List<StockInDetailsResponse> details = inventoryService.getStockInDetailsResponses(req.getRequest_id());

            for (StockInDetailsResponse detail : details) {
                String productName = productService.getProductNameById(detail.getProduct_id());
                rows.add(StockInDisplayRow.builder()
                        .requestId(req.getRequest_id())
                        .productName(productName)
                        .quantity(detail.getQuantity())
                        .status(req.getStatus())
                        .build());
            }
        }

        stockInTable.getItems().setAll(rows);
    }

    // Navigation methods using handleViewSwitch
    @FXML
    private void handleGoToKiemKe(ActionEvent event) {
        handleViewSwitch(KIEM_KE_FXML, "Kiểm kê");
    }

    @FXML
    private void handleGoToDashboard(ActionEvent event) {
        handleViewSwitch(DASHBOARD_FXML, "Dashboard");
    }

    @FXML
    private void handleGoToYeuCauXuatKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_XUAT_KHO_FXML, "Yêu cầu xuất kho");
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_NHAP_KHO_FXML, "Yêu cầu nhập kho");
    }

    @FXML
    public void handleGoToProductManagerController(ActionEvent event) {
        handleViewSwitch(PRODUCT_MANAGER_FXML, "Quản lý sản phẩm");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockInDisplayRow {
        Long requestId;
        String productName;
        Integer quantity;
        String status;
    }
}