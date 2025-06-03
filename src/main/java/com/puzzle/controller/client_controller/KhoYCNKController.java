package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.StockInDetailsResponse;
import com.puzzle.dto.response.StockInResponse;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class KhoYCNKController implements Initializable {

    @FXML private TableView<StockInDisplayRow> stockInTable;
    @FXML private TableColumn<StockInDisplayRow, Long> requestIdColumn;
    @FXML private TableColumn<StockInDisplayRow, String> productNameColumn;
    @FXML private TableColumn<StockInDisplayRow, Integer> quantityColumn;
    @FXML private TableColumn<StockInDisplayRow, String> statusColumn;
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
        List<StockInResponse> requests = inventoryService.getStockInRequests(null);
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

    @FXML
    private void handleGoToKiemKe(ActionEvent event) {
        switchScene("/views/GUI/KiemKeGUI.fxml", event, InventoryCheckController.class);
    }

    @FXML
    private void handleGoToDashboard(ActionEvent event) {
        switchScene("/views/GUI/DashBoardQLKGUI.fxml", event, DashBoardProductManagerController.class);
    }

//    @FXML
//    private void handleGoToSanPham(ActionEvent event) {
//        switchScene("/views/GUI/SanPhamGUI.fxml", event, SanPhamController.class);
//    }

    @FXML
    private void handleGoToYeuCauXuatKho(ActionEvent event) {
        switchScene("/views/GUI/KhoYCXKGUI.fxml", event, KhoYCXKController.class);
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        switchScene("/views/GUI/KhoYCNKGUI.fxml", event, KhoYCNKController.class);
    }


    private void switchScene(String fxmlPath, ActionEvent event, Class<?> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controllerClass.isInstance(controller)) {
                if (controller instanceof InventoryCheckController inv) {
                    inv.initData(currentUser, inventoryService, productService);
                }
                else if (controller instanceof DashBoardProductManagerController dash) {
                    dash.initData(currentUser, inventoryService, productService);
                    }
//                } else if (controller instanceof SanPhamController sp) {
//                    sp.initData(currentUser, inventoryService, productService);
                else if (controller instanceof KhoYCXKController xk) {
                    xk.initData(currentUser, inventoryService, productService);
                }else if (controller instanceof KhoYCNKController nk) {
                    nk.initData(currentUser, inventoryService, productService);
                }
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
