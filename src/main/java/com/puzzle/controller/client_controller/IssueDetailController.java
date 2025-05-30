package com.puzzle.controller.client_controller;

import java.math.BigDecimal;
/* 
    long id;
    long request_id;
    long product_id;
    int quantity; 
    BigDecimal unit_cost;
*/   
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.puzzle.dto.response.StockOutDetailsResponse;
import com.puzzle.dto.response.StockOutResponse;
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
public class IssueDetailController {
    
    @FXML private Label lblMaXK;
    @FXML private Label lblMaNV;
    @FXML private Label lblNgayTao;
    @FXML private Label lblNguoiDuyet;
    @FXML private Label lblNgayDuyet;
    @FXML private Label lblTrangThai;
    
    @FXML private TableView<StockOutDetailsResponse> productTable;
    @FXML private TableColumn<StockOutDetailsResponse, String> colProductName;
    @FXML private TableColumn<StockOutDetailsResponse, Integer> colQuantity;
    @FXML private TableColumn<StockOutDetailsResponse, String> colUnit;
    @FXML private TableColumn<StockOutDetailsResponse, BigDecimal> colPrice;
    
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
        
        // Format price column
        colPrice.setCellFactory(column -> new TableCell<StockOutDetailsResponse, BigDecimal>() {
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
    
    public void initData(StockOutResponse stockOut, InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        loadStockOutDetail(stockOut);
    }
    
    private void loadStockOutDetail(StockOutResponse stockOut) {
        // Hiển thị thông tin chung
        lblMaXK.setText(String.valueOf(stockOut.getRequest_id()));
        lblMaNV.setText(String.valueOf(stockOut.getEmployee_id()));
        lblNgayTao.setText(stockOut.getCreated_at() != null ? 
            stockOut.getCreated_at().format(dateFormatter) : "");
        lblNguoiDuyet.setText(stockOut.getApproved_by() != null ? 
            String.valueOf(stockOut.getApproved_by()) : "Chưa duyệt");
        lblNgayDuyet.setText(stockOut.getApproved_at() != null ? 
            stockOut.getApproved_at().format(dateFormatter) : "Chưa duyệt");
        
        // Format trạng thái
        String status = "";
        switch (stockOut.getStatus().toLowerCase()) {
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
                status = stockOut.getStatus();
        }
        lblTrangThai.setText(status);
        
        // Load chi tiết sản phẩm
        loadProductDetails(stockOut.getRequest_id());
    }
    
    private void loadProductDetails(Long stockOutId) {
        try {
            // Giả sử bạn có method để lấy chi tiết sản phẩm theo stockOutId
            List<StockOutDetailsResponse> products = inventoryService.getStockOutDetailsResponses(stockOutId);
            productTable.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu không có API chi tiết, hiển thị thông báo
            productTable.setPlaceholder(new Label("Không thể tải chi tiết sản phẩm"));
        }
    }
}