package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.puzzle.config.FXSessionManager;
import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Controller
public class KhoYCXKController implements Initializable {

    // Constants for FXML paths
    private static final String LOGIN_FXML = "/views/GUI/LoginGUI.fxml";
    private static final String KIEM_KE_FXML = "/views/GUI/KiemKeGUI.fxml";
    private static final String DASHBOARD_FXML = "/views/GUI/DashBoardQLKGUI.fxml";
    private static final String YEU_CAU_XUAT_KHO_FXML = "/views/GUI/KhoYCXKGUI.fxml";
    private static final String YEU_CAU_NHAP_KHO_FXML = "/views/GUI/KhoYCNKGUI.fxml";
    private static final String PRODUCT_MANAGER_FXML = "/views/GUI/SanPhamGUI.fxml";

    @FXML private Button logoutButton;
    @FXML private TableView<StockOutDisplayRow> stockOutTable;
    @FXML private TableColumn<StockOutDisplayRow, Long> MaXKColumn;
    @FXML private TableColumn<StockOutDisplayRow, Long> MaNVColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> NgayTaoColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> NguoiDuyetColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> NgayDuyetColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> statusColumn;
    @FXML private TableColumn<StockOutDisplayRow, Void> actionColumn;

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

        MaXKColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRequestId()));
        MaNVColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEmployeeId()));
        NgayTaoColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCreatedAt()));
        NguoiDuyetColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getApprovedBy()));
        NgayDuyetColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getApprovedAt()));
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatus()));

        statusColumn.setCellFactory(col -> {
            ComboBox<String> comboBox = new ComboBox<>();
            return new TableCell<>() {
                {
                    comboBox.getItems().addAll("PENDING", "APPROVED", "DECLINED");
                    comboBox.setOnAction(e -> {
                        StockOutDisplayRow row = getTableView().getItems().get(getIndex());
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

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button detailButton = new Button("Chi tiết");

            {
                detailButton.setOnAction(event -> {
                    StockOutDisplayRow row = getTableView().getItems().get(getIndex());
                    showDetailDialog(row.getRequestId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailButton);
                }
            }
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

    private void showDetailDialog(Long requestId) {
        Task<StockOutResponse> loadTask = new Task<>() {
            @Override
            protected StockOutResponse call() {
                List<StockOutResponse> list = inventoryService.getStockOutRequestById(requestId);
                return list.isEmpty() ? null : list.get(0);
            }
        };

        loadTask.setOnSucceeded(event -> {
            StockOutResponse stockOut = loadTask.getValue();
            if (stockOut == null) return;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/ChiTietXuatKhoGUI.fxml"));
                DialogPane dialogPane = loader.load();

                IssueDetailController controller = loader.getController();
                controller.initData(stockOut, inventoryService);

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Chi tiết xuất kho");
                dialog.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        loadTask.setOnFailed(e -> loadTask.getException().printStackTrace());

        new Thread(loadTask).start();
    }

    private void updateStatus(Long requestId, String newStatus) {
        try {
            HttpSession session = FXSessionManager.getSession();

            switch (newStatus) {
                case "APPROVED" -> inventoryService.approveStockOutRequest(requestId, session);
                case "DECLINED" -> inventoryService.declinedStockOutRequest(requestId, session);
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
        Task<List<StockOutDisplayRow>> task = new Task<>() {
            @Override
            protected List<StockOutDisplayRow> call() {
                List<StockOutDisplayRow> rows = new ArrayList<>();
                List<StockOutResponse> requests = inventoryService.getStockOutRequests();

                for (StockOutResponse req : requests) {
                    rows.add(StockOutDisplayRow.builder()
                            .requestId(req.getRequest_id())
                            .employeeId(req.getEmployee_id())
                            .createdAt(req.getCreated_at() != null ? req.getCreated_at().toString() : "")
                            .approvedBy(req.getApproved_by() != null ? req.getApproved_by().toString() : "Chưa duyệt")
                            .approvedAt(req.getApproved_at() != null ? req.getApproved_at().toString() : "")
                            .status(req.getStatus())
                            .build());
                }
                return rows;
            }
        };

        task.setOnSucceeded(e -> stockOutTable.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockOutDisplayRow {
        Long requestId;
        Long employeeId;
        String createdAt;
        String approvedBy;
        String approvedAt;
        String status;
    }

    // Navigation methods using handleViewSwitch
    @FXML
    private void handleDashboard(ActionEvent event) {
        handleViewSwitch(DASHBOARD_FXML, "Dashboard");
    }

    @FXML
    private void handleKiemKe(ActionEvent event) {
        handleViewSwitch(KIEM_KE_FXML, "Kiểm kê");
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_NHAP_KHO_FXML, "Yêu cầu nhập kho");
    }

    @FXML
    private void handleYeuCauXuatKho(ActionEvent event) {
        handleViewSwitch(YEU_CAU_XUAT_KHO_FXML, "Yêu cầu xuất kho");
    }

    @FXML
    public void handleGoToProductManagerController(ActionEvent event) {
        handleViewSwitch(PRODUCT_MANAGER_FXML, "Quản lý sản phẩm");
    }
}