package com.puzzle.controller.client_controller;

import com.puzzle.dto.request.CreateProductRequest;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class ProductAddController {
    @FXML private TextField txtName;
    @FXML private TextField txtDescription;
    @FXML private TextField txtUnit;
    @FXML private TextField txtCostPrice;
    @FXML private TextField txtSellingPrice;

    private CreateProductRequest addProduct;

    @FXML
    public void initialize() {
        addProduct = new CreateProductRequest();
    }

    public CreateProductRequest getAddedProduct() {
        addProduct.setName(txtName.getText());
        addProduct.setDescription(txtDescription.getText());
        addProduct.setUnit(txtUnit.getText());

        try {
            addProduct.setCost_price(new BigDecimal(txtCostPrice.getText()));
        } catch (NumberFormatException e) {
            addProduct.setCost_price(BigDecimal.ZERO); // hoặc xử lý lỗi khác
        }

        try {
            addProduct.setSelling_price(new BigDecimal(txtSellingPrice.getText()));
        } catch (NumberFormatException e) {
            addProduct.setSelling_price(BigDecimal.ZERO); // hoặc xử lý lỗi khác
        }

        return addProduct;
    }
}
