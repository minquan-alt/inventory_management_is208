package com.puzzle.controller.client_controller;

import com.puzzle.config.FXSessionManager;
import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.entity.StockRequests;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import jakarta.servlet.http.HttpSession;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Stage;
import lombok.*;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class KhoYCXKController implements Initializable {

    @FXML private TableView<StockOutDisplayRow> stockOutTable;
    @FXML private TableColumn<StockOutDisplayRow, Long> requestIdColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> productNameColumn;
    @FXML private TableColumn<StockOutDisplayRow, Integer> quantityColumn;
    @FXML private TableColumn<StockOutDisplayRow, String> statusColumn;
    @FXML private TableColumn<StockOutDisplayRow, Void> actionColumn;

    @FXML private Label userNameLabel;

    private InventoryService inventoryService;
    private ProductService productService;
    private UserResponse currentUser;


    public void initData(UserResponse user, InventoryService inventoryService, ProductService productService) {
        this.currentUser = user;
        this.inventoryService = inventoryService;
        this.productService = productService;

        userNameLabel.setText(user.getName());

        loadData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    private void showDetailDialog(Long requestId) {
        try {
            List<StockOutResponse> listStock = inventoryService.getStockOutRequestById(requestId);
            if (listStock.isEmpty()) {
                throw new RuntimeException("Không tìm thấy đơn hàng");
            }
            StockOutResponse stockOut = listStock.get(0);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/ChiTietXuatKhoGUI.fxml"));
            Parent root = loader.load();

            IssueDetailController controller = loader.getController();
            controller.initData(stockOut, inventoryService);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết xuất kho");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Lỗi");
            alert.setContentText("Không thể hiển thị chi tiết: " + e.getMessage());
            alert.showAndWait();
        }
    }


    private void updateStatus(Long requestId, String newStatus) {
        try {
            HttpSession session = FXSessionManager.getSession(); // lấy session từ FXSessionManager

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
        List<StockOutResponse> requests = inventoryService.getStockOutRequests();
        List<StockOutDisplayRow> rows = new ArrayList<>();

        for (StockOutResponse req : requests) {
            List<StockOutDetailsResponse> details = inventoryService.getStockOutDetailsResponses(req.getRequest_id());

            for (StockOutDetailsResponse detail : details) {
                String productName = productService.getProductNameById(detail.getProduct_id());
                rows.add(StockOutDisplayRow.builder()
                        .requestId(req.getRequest_id())
                        .productName(productName)
                        .quantity(detail.getQuantity())
                        .status(req.getStatus())
                        .build());
            }
        }

        stockOutTable.getItems().setAll(rows);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockOutDisplayRow {
        Long requestId;
        String productName;
        Integer quantity;
        String status;
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadScene("/views/GUI/DashBoardQLKGUI.fxml", event);
    }

    @FXML
    private void handleKiemKe(ActionEvent event) {
        loadScene("/views/GUI/KiemKeGUI.fxml", event);
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        loadScene("/views/GUI/KhoYCNKGUI.fxml", event);
    }

    @FXML
    private void handleYeuCauXuatKho(ActionEvent event) {
        loadScene("/views/GUI/KhoYCXKGUI.fxml", event);
    }

//    @FXML
//    private void handleSanPham(ActionEvent event) {
//        loadScene("/views/GUI/SanPhamGUI.fxml", event);
//    }

    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof KhoYCNKController) {
                ((KhoYCNKController) controller).initData(currentUser, inventoryService, productService);
            } else if (controller instanceof InventoryCheckController) {
                ((InventoryCheckController) controller).initData(currentUser, inventoryService, productService);
            } else if (controller instanceof DashBoardProductManagerController) {
                ((DashBoardProductManagerController)controller).initData(currentUser, inventoryService, productService);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
