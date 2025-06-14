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
import javafx.stage.Modality;
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
import java.util.Optional;


public class AddIngredientView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final IngredientsDb ingredientsDb = new IngredientsDb();
    private final Connection connection = databaseInitializer.connection();

    private final TextField nameField  = new TextField();
    private final TextField priceField = new TextField();
    private final TextField imageField = new TextField();
    private final Button    btnBrowse   = new Button("Browse...");
    private final Label     messageLbl  = new Label();

    private final TableView<Ingredient> table = new TableView<>();
    private final ObservableList<Ingredient> data = FXCollections.observableArrayList();

    public AddIngredientView() throws SQLException {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setCenter(buildContent());
        refreshTable();
    }

    private Label buildHeader() {
        Label header = new Label("Add New Ingredient");
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

        form.add(new Label("Name:"),      0, 0);
        form.add(nameField,               1, 0);

        form.add(new Label("Price:"),     0, 1);
        form.add(priceField,              1, 1);

        imageField.setDisable(true);
        HBox imageBox = new HBox(5, imageField, btnBrowse);
        form.add(new Label("Image:"),     0, 2);
        form.add(imageBox,                1, 2);

        btnBrowse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selected = fileChooser.showOpenDialog(getScene().getWindow());
            if (selected != null) {
                imageField.setText(selected.getAbsolutePath());
            }
        });

        Button btnSave  = new Button("Save");
        Button btnClear = new Button("Clear");
        HBox buttons = new HBox(10, btnSave, btnClear);
        form.add(buttons,                  1, 3);

        form.add(messageLbl,               1, 4);
        messageLbl.setTextFill(Color.FIREBRICK);

        btnSave.setOnAction(e -> saveIngredient());
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

        TableColumn<Ingredient, Void> actionCol = getDeleteColumn();

        table.getColumns().addAll(idCol, nameCol, priceCol, imgCol, actionCol);
        table.setItems(data);
        table.setPrefHeight(300);

        return table;
    }

    private TableColumn<Ingredient, Void> getDeleteColumn() {
        TableColumn<Ingredient, Void> col = new TableColumn<>("Action");
        col.setPrefWidth(90);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setOnAction(e -> {
                    Ingredient ing = getTableView().getItems().get(getIndex());
                    confirmAndDelete(ing);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return col;
    }

    private void saveIngredient() {
        messageLbl.setText("");
        messageLbl.setTextFill(Color.FIREBRICK);

        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String imagePath = imageField.getText().trim();

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


        String imageFileName = null;
        if (!imagePath.isEmpty()) {
            try {
                File src = new File(imagePath);
                Path saved = ImageStorage.saveImage(src);
                imageFileName = saved.getFileName().toString();
            } catch (IOException ex) {
                messageLbl.setText("Error saving image: " + ex.getMessage());
                return;
            }
        }

        Ingredient ing = new Ingredient();
        ing.setName(name);
        ing.setPrice(price);
        ing.setImage(imageFileName);

        try {
            ingredientsDb.createNewIngredientInDb(connection, ing);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Ingredient saved (id = " + ing.getId() + ").");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        imageField.clear();
        messageLbl.setText("");
    }

    private void refreshTable() {
        try {
            List<Ingredient> list = ingredientsDb.getAllAvailableIngredients(connection);
            data.setAll(list);
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("DB read error: " + ex.getMessage());
        }
    }

    private void confirmAndDelete(Ingredient ing) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete ingredient");
        alert.setHeaderText("Delete \"" + ing.getName() + "\"?");
        alert.setContentText("This action cannot be undone.");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no  = new ButtonType("No",  ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            deleteIngredient(ing);
        }
    }

    private void deleteIngredient(Ingredient ing) {
        try {
            ingredientsDb.deleteIngredientById(connection, ing.getId());
            refreshTable();
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Ingredient \"" + ing.getName() + "\" deleted.");
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }
}