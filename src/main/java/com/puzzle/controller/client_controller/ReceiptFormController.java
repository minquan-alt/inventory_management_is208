package com.puzzle.controller.client_controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.puzzle.dto.request.ProductRequestIn;
import com.puzzle.dto.request.StockInRequest;
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
public class ReceiptFormController {
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
        okButton.setOnAction(event -> handleSubmit());
        cancelButton.setOnAction(event -> handleCancel());

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
    }

    @FXML
    private void handleAddProductRow() {
        HBox row = new HBox(10);

        ChoiceBox<String> productChoiceBox = new ChoiceBox<>();
        productChoiceBox.setItems(FXCollections.observableArrayList(productToId.keySet()));
        productChoiceBox.setPrefWidth(300);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Số lượng");
        quantityField.setPrefWidth(100);

        TextField priceField = new TextField();
        priceField.setPromptText("Đơn giá");
        priceField.setPrefWidth(150);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> productList.getChildren().remove(row));

        row.getChildren().addAll(productChoiceBox, quantityField, priceField, deleteButton);
        productList.getChildren().add(row);
    }

    private void handleSubmit() {
        List<ProductRequestIn> productRequests = new ArrayList<>();

        for (Node node : productList.getChildren()) {
            if (node instanceof HBox row) {
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);
                TextField priceField = (TextField) row.getChildren().get(2);

                String product = choiceBox.getValue();
                String quantityText = quantityField.getText();
                String priceText = priceField.getText();

                if (product == null || product.isEmpty()) {
                    AlertUtil.showError("Vui lòng chọn sản phẩm.");
                    return;
                }
                if (quantityText == null || quantityText.trim().isEmpty()) {
                    AlertUtil.showError("Vui lòng nhập số lượng cho sản phẩm: " + product);
                    return;
                }
                if (priceText == null || priceText.trim().isEmpty()) {
                    AlertUtil.showError("Vui lòng nhập đơn giá cho sản phẩm: " + product);
                    return;
                }

                Long productId = productToId.get(product);
                if (productId == null) {
                    AlertUtil.showError("Không tìm thấy ID của sản phẩm: " + product);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityText.trim());
                    if (quantity <= 0) {
                        AlertUtil.showError("Số lượng phải lớn hơn 0 cho sản phẩm: " + product);
                        return;
                    }

                    BigDecimal price = new BigDecimal(priceText.trim());
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        AlertUtil.showError("Đơn giá phải lớn hơn 0 cho sản phẩm: " + product);
                        return;
                    }

                    productRequests.add(new ProductRequestIn(productId, quantity, price));
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Số lượng hoặc đơn giá không hợp lệ cho sản phẩm: " + product);
                    return;
                }
            }
        }

        if (productRequests.isEmpty()) {
            AlertUtil.showError("Vui lòng thêm ít nhất một sản phẩm.");
            return;
        }

        try {
            StockInRequest request = new StockInRequest();
            request.setProducts(productRequests);

            Map<String, Object> result = inventoryService.createStockIn(request, user.getId());

            if ((Boolean) result.get("success")) {
                AlertUtil.showInfo("Tạo phiếu nhập kho thành công!");
                submitted = true;
                closeDialog();
            } else {
                AlertUtil.showError("Tạo phiếu nhập kho thất bại!", (String) result.get("message"));
            }
        } catch (AppException | IOException e) {
            AlertUtil.showError("Lỗi khi tạo phiếu nhập kho: " + e.getMessage());
        }
    }

    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogPane != null) {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("DialogPane is null in closeDialog");
        }
    }
}
