package org.example.Ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.example.config.DatabaseInitializer;
import org.example.config.ImageStorage;
import org.example.db.AdditionalProductDb;
import org.example.domain.AdditionalProduct;
import org.example.enumeration.Size;
import org.example.enumeration.Type;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UpdateAdditionalProductView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final AdditionalProductDb additionalProductDb = new AdditionalProductDb();
    private final Connection connection = databaseInitializer.connection();

    // Form fields
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();
    private final Button btnChooseImage = new Button("Choose Image…");
    private final Label imageNameLbl = new Label("No file chosen");
    private File chosenImageFile = null;
    private String originalImageName = null;

    private final ComboBox<Type> typeCombo = new ComboBox<>();
    private final ComboBox<Size> sizeCombo = new ComboBox<>();
    private final Label messageLbl = new Label();

    // Table
    private final TableView<AdditionalProduct> table = new TableView<>();
    private final ObservableList<AdditionalProduct> tableData = FXCollections.observableArrayList();

    public UpdateAdditionalProductView() throws SQLException {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setLeft(buildForm());
        setBottom(buildTable());
        refreshTable();
    }

    private Label buildHeader() {
        Label header = new Label("Update Additional Product");
        header.getStyleClass().add("h2");
        return header;
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);

        // ID (read-only)
        idField.setDisable(true);
        form.add(new Label("ID:"), 0, 0);
        form.add(idField, 1, 0);

        // Name
        form.add(new Label("Name:"), 0, 1);
        form.add(nameField, 1, 1);

        // Price
        form.add(new Label("Price:"), 0, 2);
        form.add(priceField, 1, 2);

        // Image chooser
        form.add(new Label("Image:"), 0, 3);
        HBox imgBox = new HBox(10, btnChooseImage, imageNameLbl);
        form.add(imgBox, 1, 3);
        btnChooseImage.setOnAction(e -> openFileChooser());

        // Type
        form.add(new Label("Type:"), 0, 4);
        typeCombo.setItems(FXCollections.observableArrayList(Type.values()));
        form.add(typeCombo, 1, 4);

        // Size
        form.add(new Label("Size:"), 0, 5);
        sizeCombo.setItems(FXCollections.observableArrayList(Size.values()));
        form.add(sizeCombo, 1, 5);

        // Buttons
        Button btnUpdate = new Button("Update");
        Button btnClear = new Button("Clear");
        btnUpdate.setOnAction(e -> updateProduct());
        btnClear.setOnAction(e -> clearForm());
        form.add(new HBox(10, btnUpdate, btnClear), 1, 6);

        form.add(messageLbl, 1, 7);
        messageLbl.setTextFill(Color.FIREBRICK);

        return form;
    }

    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Product Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            chosenImageFile = file;
            imageNameLbl.setText(file.getName());
        }
    }

    private TableView<AdditionalProduct> buildTable() {
        TableColumn<AdditionalProduct, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); idCol.setPrefWidth(60);
        TableColumn<AdditionalProduct, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); nameCol.setPrefWidth(180);
        TableColumn<AdditionalProduct, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price")); priceCol.setPrefWidth(100);
        TableColumn<AdditionalProduct, Type> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type")); typeCol.setPrefWidth(100);
        TableColumn<AdditionalProduct, Size> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size")); sizeCol.setPrefWidth(100);
        TableColumn<AdditionalProduct, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("image")); imgCol.setPrefWidth(200);
        TableColumn<AdditionalProduct, Void> editCol = new TableColumn<>("Action"); editCol.setPrefWidth(90);
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            { btn.setOnAction(e -> loadProductIntoForm(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        TableColumn<AdditionalProduct, Void> deleteCol = new TableColumn<>("Delete"); deleteCol.setPrefWidth(90);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            { btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        table.getColumns().addAll(idCol, nameCol, priceCol, typeCol, sizeCol, imgCol, editCol, deleteCol);
        table.setItems(tableData); table.setPrefHeight(300);
        return table;
    }

    private void loadProductIntoForm(AdditionalProduct ap) {
        idField.setText(String.valueOf(ap.getId()));
        nameField.setText(ap.getName());
        priceField.setText(ap.getPrice().toPlainString());
        typeCombo.setValue(ap.getType());
        sizeCombo.setValue(ap.getSize());
        originalImageName = ap.getImage();
        chosenImageFile = null;
        imageNameLbl.setText(originalImageName != null ? originalImageName : "No file chosen");
        messageLbl.setText("");
    }

    private void updateProduct() {
        messageLbl.setText(""); messageLbl.setTextFill(Color.FIREBRICK);
        if (idField.getText().isEmpty()) {
            messageLbl.setText("Click Edit on a product first."); return;
        }
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty() || typeCombo.getValue() == null || sizeCombo.getValue() == null) {
            messageLbl.setText("All fields except image are required."); return;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.signum() < 0) { messageLbl.setText("Price must be ≥ 0."); return; }
        } catch (NumberFormatException ex) { messageLbl.setText("Invalid price format."); return; }

        String imageName = originalImageName;
        if (chosenImageFile != null) {
            try {
                Path saved = ImageStorage.saveImage(chosenImageFile);
                imageName = saved.getFileName().toString();
            } catch (IOException ex) {
                messageLbl.setText("Error saving image: " + ex.getMessage()); return;
            }
        }

        AdditionalProduct ap = AdditionalProduct.builder()
                .id(Long.parseLong(idField.getText()))
                .name(name)
                .price(price)
                .image(imageName)
                .type(typeCombo.getValue())
                .size(sizeCombo.getValue())
                .build();
        try {
            additionalProductDb.updateAdditionalProductInDb(connection, ap);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Product updated (id=" + ap.getId() + ").");
            clearForm(); refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        idField.clear(); nameField.clear(); priceField.clear();
        typeCombo.getSelectionModel().clearSelection(); sizeCombo.getSelectionModel().clearSelection();
        chosenImageFile = null; originalImageName = null;
        imageNameLbl.setText("No file chosen"); messageLbl.setText("");
    }

    private void confirmAndDelete(AdditionalProduct ap) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete \"" + ap.getName() + "\"?");
        alert.setContentText("This cannot be undone.");
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) { deleteProduct(ap); }
    }

    private void deleteProduct(AdditionalProduct ap) {
        try {
            additionalProductDb.deleteAdditionalProductFromDb(connection, ap.getId());
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Deleted \"" + ap.getName() + "\".");
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        try {
            List<AdditionalProduct> list = additionalProductDb.getAllAdditionalProducts(connection);
            tableData.setAll(list);
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Cannot load products: " + ex.getMessage());
        }
    }
}
