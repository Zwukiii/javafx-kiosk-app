package org.example.Ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.example.config.DatabaseInitializer;
import org.example.config.ImageStorage;
import org.example.db.IngredientsDb;
import org.example.domain.Ingredient;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * View for updating ingredients, including image management.
 */
public class UpdateIngredientView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final IngredientsDb ingredientsDb = new IngredientsDb();
    private final Connection connection = databaseInitializer.connection();

    // Form fields
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();

    // Image chooser
    private final Button btnChooseImage = new Button("Choose Image…");
    private final Label imageNameLbl = new Label("No file chosen");
    private File chosenImageFile = null;
    private String originalImageName = null;

    private final Label messageLbl = new Label();

    // Table of ingredients
    private final TableView<Ingredient> table = new TableView<>();
    private final ObservableList<Ingredient> data = FXCollections.observableArrayList();

    public UpdateIngredientView() throws SQLException {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setCenter(buildContent());
        refreshTable();
    }

    private Label buildHeader() {
        Label header = new Label("Update Ingredient");
        header.getStyleClass().add("h2");
        return header;
    }

    private VBox buildContent() {
        VBox content = new VBox(15);
        content.getChildren().addAll(buildForm(), buildTable());
        return content;
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

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

        // Buttons
        Button btnUpdate = new Button("Update");
        Button btnClear = new Button("Clear");
        HBox buttons = new HBox(10, btnUpdate, btnClear);
        form.add(buttons, 1, 4);

        // Message label
        form.add(messageLbl, 1, 5);
        messageLbl.setTextFill(Color.FIREBRICK);

        btnUpdate.setOnAction(e -> updateIngredient());
        btnClear.setOnAction(e -> clearForm());

        return form;
    }

    private TableView<Ingredient> buildTable() {
        TableColumn<Ingredient, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Ingredient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Ingredient, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<Ingredient, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imgCol.setPrefWidth(200);

        TableColumn<Ingredient, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction(e -> loadIngredientIntoForm(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, priceCol, imgCol, actionCol);
        table.setItems(data);
        table.setPrefHeight(300);
        return table;
    }

    /**
     * Open file chooser and set chosenImageFile and label.
     */
    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            chosenImageFile = file;
            imageNameLbl.setText(file.getName());
        }
    }

    /**
     * Load selected ingredient into form, preserving original image.
     */
    private void loadIngredientIntoForm(Ingredient ing) {
        idField.setText(String.valueOf(ing.getId()));
        nameField.setText(ing.getName());
        priceField.setText(ing.getPrice().toPlainString());
        originalImageName = ing.getImage();
        chosenImageFile = null;
        imageNameLbl.setText(originalImageName != null ? originalImageName : "No file chosen");
        messageLbl.setText("");
    }

    /**
     * Update ingredient record, saving new image if selected.
     */
    private void updateIngredient() {
        messageLbl.setText("");
        messageLbl.setTextFill(Color.FIREBRICK);

        if (idField.getText().isEmpty()) {
            messageLbl.setText("Select an ingredient first (click Edit).");
            return;
        }

        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty()) {
            messageLbl.setText("Name and price are required.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.signum() < 0) {
                messageLbl.setText("Price must be non-negative.");
                return;
            }
        } catch (NumberFormatException ex) {
            messageLbl.setText("Invalid price format.");
            return;
        }

        // Determine image to use: save new one or keep original
        String imageName = originalImageName;
        if (chosenImageFile != null) {
            try {
                Path saved = ImageStorage.saveImage(chosenImageFile);
                imageName = saved.getFileName().toString();
            } catch (IOException ex) {
                messageLbl.setText("Error saving image: " + ex.getMessage());
                return;
            }
        }

        Ingredient ing = new Ingredient();
        ing.setId(Long.parseLong(idField.getText()));
        ing.setName(name);
        ing.setPrice(price);
        ing.setImage(imageName);

        try {
            ingredientsDb.UpdateIngredientInDb(connection, ing);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Ingredient updated (id = " + ing.getId() + ").");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    /**
     * Clear form fields and reset image state.
     */
    private void clearForm() {
        idField.clear();
        nameField.clear();
        priceField.clear();
        chosenImageFile = null;
        originalImageName = null;
        imageNameLbl.setText("No file chosen");
        messageLbl.setText("");
    }

    /**
     * Refresh ingredient table from database.
     */
    private void refreshTable() {
        try {
            List<Ingredient> list = ingredientsDb.getAllAvailableIngredients(connection);
            data.setAll(list);
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("DB read error: " + ex.getMessage());
        }
    }
}