package com.puzzle.controller.client_controller;

import com.puzzle.dto.fx.InventoryCheckRow;
import com.puzzle.dto.request.InventoryCheckDetailRequest;
import com.puzzle.dto.request.InventoryCheckRequest;
import com.puzzle.dto.response.InventoryCheckDetailResponse;
import com.puzzle.dto.response.InventoryCheckResponse;
import com.puzzle.dto.response.InventoryResponse;
import com.puzzle.dto.response.UserResponse;
import com.puzzle.service.InventoryService;
import com.puzzle.service.ProductService;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
    @FXML private TableColumn<InventoryCheckRow, String> noteDetailColumn;

    @FXML private TextField noteField;

    @FXML private TableColumn<InventoryCheckResponse, Void> detailColumn;


    private InventoryService inventoryService;
    private UserResponse currentUser;
    private ProductService productService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("checkId"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        detailColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Chi tiết");

            {
                viewButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 10px;");
                viewButton.setOnAction(event -> {
                    InventoryCheckResponse response = getTableView().getItems().get(getIndex());
                    showDetailDialog(response);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        inventoryCheckTable.setRowFactory(tv -> {
            TableRow<InventoryCheckResponse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    InventoryCheckResponse selectedCheck = row.getItem();
                    showDetailDialog(selectedCheck);
                }
            });
            return row;
        });
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

        Task<List<InventoryCheckResponse>> task = new Task<>() {
            @Override
            protected List<InventoryCheckResponse> call() {
                return inventoryService.getAllInventoryCheck(currentUser.getId());
            }
        };

        task.setOnSucceeded(event -> {
            List<InventoryCheckResponse> data = task.getValue();
            inventoryCheckTable.getItems().clear();
            inventoryCheckTable.getItems().addAll(data);
        });

        task.setOnFailed(event -> {
            System.err.println("Lỗi khi tải kiểm kê: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }


    private void showDetailDialog(InventoryCheckResponse checkResponse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/ChiTietKiemKeGUI.fxml"));
            DialogPane dialogPane = loader.load();

            InventoryCheckDetailsController controller = loader.getController();
            controller.setDetails(checkResponse.getDetails());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Chi tiết kiểm kê");


            Stage ownerStage = (Stage) inventoryCheckTable.getScene().getWindow();
            dialog.initOwner(ownerStage);

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Task<List<InventoryCheckRow>> task = new Task<>() {
            @Override
            protected List<InventoryCheckRow> call() {
                List<InventoryResponse> inventoryList = inventoryService.getInventory();
                return inventoryList.stream()
                        .map(inv -> new InventoryCheckRow(
                                inv.getProduct_id(),
                                inv.getProduct_name(),
                                inv.getQuantity(),
                                inv.getQuantity(),
                                ""
                        ))
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Form/KiemKeForm.fxml"));
                loader.setController(this);
                DialogPane dialogPane = loader.load();

                // Cài đặt dialog
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Tạo kiểm kê");

                // Cài đặt bảng
                productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
                systemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("systemQuantity"));
                actualQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("actualQuantity"));
                actualQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                actualQuantityColumn.setOnEditCommit(eventCommit -> {
                    InventoryCheckRow row = eventCommit.getRowValue();
                    row.setActualQuantity(eventCommit.getNewValue());
                });

                noteDetailColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
                noteDetailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                noteDetailColumn.setOnEditCommit(e2 -> {
                    InventoryCheckRow row = e2.getRowValue();
                    row.setNote(e2.getNewValue());
                });

                productTable.setEditable(true);
                productTable.setItems(FXCollections.observableArrayList(task.getValue()));

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String noteText = noteField.getText().trim();

                    List<InventoryCheckDetailRequest> detailRequests = productTable.getItems().stream()
                            .map(row -> new InventoryCheckDetailRequest(
                                    row.getProductId(),
                                    row.getActualQuantity(),
                                    row.getNote()))
                            .collect(Collectors.toList());

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

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            System.err.println("Lỗi khi load inventory cho kiểm kê: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }
    public void handleGoToProductManagerController(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/GUI/SanPhamGUI.fxml"));
            Parent root = loader.load();

            ProductManagerController controller = loader.getController();

            controller.initData(currentUser, inventoryService, productService);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý sản phẩm");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
