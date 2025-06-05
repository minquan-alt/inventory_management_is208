package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.ProductResponse;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class ProductUpdateController {
    @FXML private Label lblProductId;
    @FXML private TextField txtName;
    @FXML private TextField txtDescription;
    @FXML private TextField txtUnit;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtSellingPrice;

    private ProductResponse editingProduct;

    @FXML
    public void initialize() {
        // Nếu cần xử lý khởi tạo gì thêm thì thêm vào đây
    }

    public void setEditingProduct(ProductResponse product) {
        this.editingProduct = product;

        lblProductId.setText(String.valueOf(product.getProduct_id()));
        txtName.setText(product.getName());
        txtDescription.setText(product.getDescription());
        txtUnit.setText(product.getUnit());
        txtCostPrice.setText(product.getCost_price().toString());
        txtSellingPrice.setText(product.getSelling_price().toString());
    }

    public ProductResponse getUpdatedProduct() {
        editingProduct.setName(txtName.getText());
        editingProduct.setDescription(txtDescription.getText());
        editingProduct.setUnit(txtUnit.getText());
        editingProduct.setCost_price(new BigDecimal(txtCostPrice.getText()));
        editingProduct.setSelling_price(new BigDecimal(txtSellingPrice.getText()));
        return editingProduct;
    }
}
