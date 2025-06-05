package com.puzzle.controller.client_controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.puzzle.dto.response.StockInDetailsResponse;
import com.puzzle.dto.response.StockInResponse;
import com.puzzle.service.InventoryService;

import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@Controller
public class ReceiptDetailController {

    @FXML private Label lblMaNK;
    @FXML private Label lblMaNV;
    @FXML private Label lblNgayTao;
    @FXML private Label lblNguoiDuyet;
    @FXML private Label lblNgayDuyet;
    @FXML private Label lblTrangThai;

    @FXML private TableView<StockInDetailsResponse> productTable;
    @FXML private TableColumn<StockInDetailsResponse, String> colProductName;
    @FXML private TableColumn<StockInDetailsResponse, Integer> colQuantity;
    @FXML private TableColumn<StockInDetailsResponse, String> colUnit;
    @FXML private TableColumn<StockInDetailsResponse, BigDecimal> colPrice;

    private InventoryService inventoryService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupProductTable();
    }

    private void setupProductTable() {
        colProductName.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unit_cost"));

        colPrice.setCellFactory(column -> new TableCell<StockInDetailsResponse, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText("");
                } else {
                    setText(String.format("%,.0f VNĐ", price));
                }
            }
        });
    }

    public void initData(StockInResponse stockIn, InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        loadStockInDetail(stockIn);
    }

    private void loadStockInDetail(StockInResponse stockIn) {
        lblMaNK.setText(String.valueOf(stockIn.getRequest_id()));
        lblMaNV.setText(String.valueOf(stockIn.getEmployee_id()));
        lblNgayTao.setText(stockIn.getCreated_at() != null ?
                stockIn.getCreated_at().format(dateFormatter) : "");
        lblNguoiDuyet.setText(stockIn.getApproved_by() != null ?
                String.valueOf(stockIn.getApproved_by()) : "Chưa duyệt");
        lblNgayDuyet.setText(stockIn.getApproved_at() != null ?
                stockIn.getApproved_at().format(dateFormatter) : "Chưa duyệt");

        String status = "";
        switch (stockIn.getStatus().toLowerCase()) {
            case "pending":
                status = "Chờ duyệt";
                lblTrangThai.setStyle("-fx-text-fill: orange;");
                break;
            case "approved":
                status = "Đã duyệt";
                lblTrangThai.setStyle("-fx-text-fill: green;");
                break;
            case "declined":
                status = "Từ chối";
                lblTrangThai.setStyle("-fx-text-fill: red;");
                break;
            default:
                status = stockIn.getStatus();
        }
        lblTrangThai.setText(status);

        loadProductDetails(stockIn.getRequest_id());
    }

    private void loadProductDetails(Long stockInId) {
        try {
            List<StockInDetailsResponse> products = inventoryService.getStockInDetailsResponses(stockInId);
            productTable.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            e.printStackTrace();
            productTable.setPlaceholder(new Label("Không thể tải chi tiết sản phẩm"));
        }
    }
}
