package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        loadScene("/views/GUI/KhoYCNKGUI.fxml", event);
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
