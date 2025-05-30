package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.puzzle.dto.request.ProductRequest;
import com.puzzle.dto.request.StockOutRequest;
import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.exception.AppException;
import com.puzzle.service.InventoryService;
import com.puzzle.utils.AlertUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Controller
public class IssueFormController {
    @FXML
    private VBox productList;

    @FXML
    private Button addProductButton;

    @FXML
    private DialogPane dialogPane;

    @Autowired
    private InventoryService inventoryService;
    private UserResponse user;
    private Map<String, Long> productToId = new HashMap<>();

    private boolean submitted = false;

    public boolean isSubmitted() {
        return submitted;
    }

    @FXML
    public void initialize() {
        if (dialogPane == null) {
            System.err.println("DialogPane is null in initialize");
            return;
        }
        
        Button okButton = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(0));
        Button cancelButton = (Button) dialogPane.lookupButton(dialogPane.getButtonTypes().get(1));
        okButton.setOnAction(event -> {
            System.out.println("OK button clicked");
            handleSubmit();
        });
        cancelButton.setOnAction(event -> {
            System.out.println("Cancel button clicked");
            handleCancel();
        });

        addProductButton.setOnAction(event -> handleAddProductRow());
    }


    public void initData(UserResponse user, InventoryService inventoryService) {
        this.user = user;
        this.inventoryService = inventoryService;
        initializeForm();
    }

    private void initializeForm() {
        List<InventoryResponse> inventories = inventoryService.getInventory();
        productToId.clear();
        for (InventoryResponse item : inventories) {
            productToId.put(item.getProduct_name(), item.getProduct_id());
        }
        System.out.println("Product map initialized: " + productToId);
    }

    @FXML
    private void handleAddProductRow() {
        HBox row = new HBox(10);

        ChoiceBox<String> productChoiceBox = new ChoiceBox<>();
        productChoiceBox.setItems(FXCollections.observableArrayList(productToId.keySet()));
        productChoiceBox.setPrefWidth(400);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Nhập số lượng");
        quantityField.setPrefWidth(300);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> productList.getChildren().remove(row));

        row.getChildren().addAll(productChoiceBox, quantityField, deleteButton);
        productList.getChildren().add(row);
    }

    private void handleSubmit() {
        List<ProductRequest> productRequests = new ArrayList<>();

        for (Node node : productList.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;

                // Lấy ChoiceBox và TextField từ HBox
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);

                String product = choiceBox.getValue();
                String quantityText = quantityField.getText();

                // Validate từng dòng
                if (product == null || product.isEmpty()) {
                    AlertUtil.showError("Vui lòng chọn một sản phẩm ở một dòng.");
                    return;
                }
                if (quantityText == null || quantityText.trim().isEmpty()) {
                    AlertUtil.showError("Vui lòng nhập số lượng ở một dòng.");
                    return;
                }

                Long chosenId = productToId.get(product);
                if (chosenId == null) {
                    AlertUtil.showError("Không tìm thấy id của sản phẩm đã chọn: " + product);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityText.trim());
                    if (quantity <= 0) {
                        AlertUtil.showError("Số lượng phải lớn hơn 0 ở dòng sản phẩm: " + product);
                        return;
                    }
                    productRequests.add(new ProductRequest(chosenId, quantity));
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Số lượng phải là số nguyên hợp lệ ở dòng sản phẩm: " + product);
                    return;
                }
            }
        }

        if (productRequests.isEmpty()) {
            AlertUtil.showError("Vui lòng thêm ít nhất một sản phẩm.");
            return;
        }

        try {
            StockOutRequest stockOutRequest = new StockOutRequest();
            stockOutRequest.setProducts(productRequests);

            Map<String, Object> result = inventoryService.createStockOut(stockOutRequest, user.getId());
            if ((Boolean) result.get("success")) {
                System.out.println("Stock-out request created for products: " + productRequests.size());
                AlertUtil.showInfo("Tạo lệnh xuất kho thành công!");
                submitted = true;
                closeDialog();
            } else {
                System.out.println("Error: " + result.get("message"));
                AlertUtil.showError("Tạo lệnh xuất kho thất bại!", (String) result.get("message"));
            }
        } catch (AppException | IOException e) {
            AlertUtil.showError("Lỗi khi tạo lệnh xuất kho: " + e.getMessage());
        }
    }


    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogPane != null) {
            System.out.println("Closing dialog");
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("DialogPane is null in closeDialog");
        }
    }
}