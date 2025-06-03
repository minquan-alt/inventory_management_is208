package com.puzzle.controller.client_controller;

import com.puzzle.dto.fx.InventoryCheckRow;
import com.puzzle.dto.request.InventoryCheckDetailRequest;
import com.puzzle.dto.request.InventoryCheckRequest;
import com.puzzle.dto.response.InventoryCheckResponse;
import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class InventoryCheckController implements Initializable {

    @FXML private TableView<InventoryCheckResponse> inventoryCheckTable;
    @FXML private TableColumn<InventoryCheckResponse, Long> idColumn;
    @FXML private TableColumn<InventoryCheckResponse, String> startDateColumn;
    @FXML private TableColumn<InventoryCheckResponse, String> noteColumn;

    @FXML private Label userNameLabel;

    @FXML private TableView<InventoryCheckRow> productTable;
    @FXML private TableColumn<InventoryCheckRow, String> productNameColumn;
    @FXML private TableColumn<InventoryCheckRow, Integer> systemQuantityColumn;
    @FXML private TableColumn<InventoryCheckRow, Integer> actualQuantityColumn;

    @FXML private TextField noteField;

    private InventoryService inventoryService;
    private UserResponse currentUser;
    private ProductService productService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("checkId"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    public void initData(UserResponse user, InventoryService inventoryService, ProductService productService) {
        this.currentUser = user;
        this.inventoryService = inventoryService;
        this.productService = productService;

        userNameLabel.setText(user.getName());

        loadInventoryChecks();
    }

    private void loadInventoryChecks() {
        if (currentUser == null || inventoryService == null) return;
        List<InventoryCheckResponse> data = inventoryService.getAllInventoryCheck(currentUser.getId());
        inventoryCheckTable.getItems().clear();
        inventoryCheckTable.getItems().addAll(data);
    }

    @FXML
    private void handleKiemKe(ActionEvent event) {
        loadScene("/views/GUI/KiemKeGUI.fxml", event);
    }

    @FXML
    private void handleYeuCauXuatKho(ActionEvent event) {
        loadScene("/views/GUI/KhoYCXKGUI.fxml", event);
    }

    @FXML
    private void handleYeuCauNhapKho(ActionEvent event) {
        loadScene("/views/GUI/KhoYCNKGUI.fxml", event);
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadScene("/views/GUI/DashBoardQLKGUI.fxml", event);
    }

    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof KhoYCNKController) {
                ((KhoYCNKController) controller).initData(currentUser, inventoryService, productService);
            } else if (controller instanceof DashBoardProductManagerController dash) {
                dash.initData(currentUser, inventoryService, productService);
            } else if (controller instanceof KhoYCXKController xk) {
                xk.initData(currentUser, inventoryService, productService);
            } else if (controller instanceof InventoryCheckController) {
                ((InventoryCheckController) controller).initData(currentUser, inventoryService, productService);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTaoKiemKe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Form/KiemKeForm.fxml"));
            loader.setController(this);
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Tạo kiểm kê");

            // Setup table columns
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
            systemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("systemQuantity"));
            actualQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("actualQuantity"));
            actualQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            actualQuantityColumn.setOnEditCommit(eventCommit -> {
                InventoryCheckRow row = eventCommit.getRowValue();
                row.setActualQuantity(eventCommit.getNewValue());
            });

            productTable.setEditable(true);
            List<InventoryResponse> inventoryList = inventoryService.getInventory();
            List<InventoryCheckRow> checkRows = inventoryList.stream()
                    .map(inv -> new InventoryCheckRow(inv.getProduct_id(), inv.getProduct_name(), inv.getQuantity(), inv.getQuantity()))
                    .collect(Collectors.toList());
            productTable.setItems(FXCollections.observableArrayList(checkRows));

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                String noteText = noteField.getText().trim();

                List<InventoryCheckDetailRequest> detailRequests = new ArrayList<>();
                for (InventoryCheckRow row : productTable.getItems()) {
                    InventoryCheckDetailRequest detail = new InventoryCheckDetailRequest();
                    detail.setProductId(row.getProductId());
                    detail.setActualQuantity(row.getActualQuantity());
                    detail.setNote("");
                    detailRequests.add(detail);
                }

                InventoryCheckRequest request = new InventoryCheckRequest();
                request.setNote(noteText.isEmpty() ? "Kiểm kê định kỳ" : noteText);
                request.setInventoryCheckDetailRequests(detailRequests);

                inventoryService.createCheck(currentUser.getId(), request);
                loadInventoryChecks();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Tạo phiếu kiểm kê thành công!");
                alert.showAndWait();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
