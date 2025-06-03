package com.puzzle.controller.client_controller;

import com.puzzle.dto.response.InventoryCheckDetailResponse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class InventoryCheckDetailsController {

    @FXML
    private TableView<InventoryCheckDetailResponse> detailTable;

    @FXML
    private TableColumn<InventoryCheckDetailResponse, Long> productIdCol;

    @FXML
    private TableColumn<InventoryCheckDetailResponse, Integer> systemQtyCol;

    @FXML
    private TableColumn<InventoryCheckDetailResponse, Integer> actualQtyCol;

    @FXML
    private TableColumn<InventoryCheckDetailResponse, Integer> adjustmentCol;

    @FXML
    private TableColumn<InventoryCheckDetailResponse, String> noteCol;

    public void setDetails(List<InventoryCheckDetailResponse> details) {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        systemQtyCol.setCellValueFactory(new PropertyValueFactory<>("systemQuantity"));
        actualQtyCol.setCellValueFactory(new PropertyValueFactory<>("actualQuantity"));
        adjustmentCol.setCellValueFactory(new PropertyValueFactory<>("adjustment"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        detailTable.setItems(FXCollections.observableArrayList(details));
    }
}
