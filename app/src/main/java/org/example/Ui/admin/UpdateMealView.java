package org.example.Ui.admin;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.example.config.DatabaseInitializer;
import org.example.config.ImageStorage;
import org.example.db.AdditionalProductDb;
import org.example.db.BasicProductDb;
import org.example.db.MealDb;
import org.example.domain.AdditionalProduct;
import org.example.domain.BasicProduct;
import org.example.domain.Meal;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * View for updating existing meals, including image management.
 */
public class UpdateMealView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final Connection connection = databaseInitializer.connection();

    private final MealDb mealDb = new MealDb();
    private final BasicProductDb basicProductDb = new BasicProductDb();
    private final AdditionalProductDb additionalProductDb = new AdditionalProductDb();

    // Form Controls
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();

    private final Button btnChooseImage = new Button("Choose Image…");
    private final Label imageNameLbl = new Label("No file chosen");
    private File chosenImageFile = null;
    private String originalImageName = null;

    private final ComboBox<BasicProduct> basicProductCombo = new ComboBox<>();
    private final ComboBox<AdditionalProduct> additionalProductCombo = new ComboBox<>();

    private final ObservableList<AdditionalProduct> selectedAdditions = FXCollections.observableArrayList();
    private final ListView<AdditionalProduct> selectedList = new ListView<>(selectedAdditions);

    private final Label messageLbl = new Label();

    // Table of existing meals
    private final TableView<Meal> table = new TableView<>();
    private final ObservableList<Meal> tableData = FXCollections.observableArrayList();

    /**
     * Constructor: initializes UI and loads data.
     */
    public UpdateMealView() throws SQLException {
        setPadding(new Insets(20));

        // Load combo box data
        basicProductCombo.setItems(FXCollections.observableArrayList(
                basicProductDb.getAllBasicProducts(connection)
        ));
        additionalProductCombo.setItems(FXCollections.observableArrayList(
                additionalProductDb.getAllAdditionalProducts(connection)
        ));

        // Build layout
        setTop(buildHeader());
        setLeft(buildForm());
        setRight(buildSelectedAdditionsPanel());
        setCenter(buildTable());
        BorderPane.setMargin(table, new Insets(20, 0, 0, 0));

        refreshTable();
    }

    /**
     * Create header label.
     */
    private Label buildHeader() {
        Label header = new Label("Update Meal");
        header.getStyleClass().add("h2");
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0));
        return header;
    }

    /**
     * Build form grid for editing meal details.
     */
    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setPadding(new Insets(0, 20, 0, 0));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHalignment(HPos.RIGHT);
        c1.setMinWidth(100);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        c2.setMinWidth(200);
        form.getColumnConstraints().addAll(c1, c2);

        // ID field (read-only)
        idField.setDisable(true);
        form.add(new Label("ID:"), 0, 0);
        form.add(idField, 1, 0);

        // Name field
        form.add(new Label("Name:"), 0, 1);
        form.add(nameField, 1, 1);

        // Price field
        form.add(new Label("Price:"), 0, 2);
        form.add(priceField, 1, 2);

        // Image chooser
        form.add(new Label("Image:"), 0, 3);
        HBox imgBox = new HBox(10, btnChooseImage, imageNameLbl);
        form.add(imgBox, 1, 3);
        btnChooseImage.setOnAction(e -> openFileChooser());

        // Basic product dropdown
        form.add(new Label("Basic Product:"), 0, 4);
        basicProductCombo.setPrefWidth(200);
        form.add(basicProductCombo, 1, 4);

        // Additional products picker
        form.add(new Label("Add Addition:"), 0, 5);
        additionalProductCombo.setPrefWidth(200);
        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> addSelectedAdditional());
        form.add(new HBox(5, additionalProductCombo, btnAdd), 1, 5);

        // Update / Clear buttons
        Button btnUpdate = new Button("Update");
        Button btnClear = new Button("Clear");
        btnUpdate.setOnAction(e -> updateMeal());
        btnClear.setOnAction(e -> clearForm());
        form.add(new HBox(10, btnUpdate, btnClear), 1, 6);

        // Message label
        form.add(messageLbl, 1, 7);
        messageLbl.setTextFill(Color.FIREBRICK);

        return form;
    }

    /**
     * Build panel for displaying selected additional products.
     */
    private VBox buildSelectedAdditionsPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0, 0, 0, 20));
        Label lbl = new Label("Selected Additions");
        selectedList.setPrefSize(200, 200);
        Button btnRemove = new Button("Remove Selected");
        btnRemove.setOnAction(e -> {
            AdditionalProduct sel = selectedList.getSelectionModel().getSelectedItem();
            if (sel != null) selectedAdditions.remove(sel);
        });
        box.getChildren().addAll(lbl, selectedList, btnRemove);
        VBox.setVgrow(selectedList, Priority.ALWAYS);
        return box;
    }

    /**
     * Helper to add selected item to the list.
     */
    private void addSelectedAdditional() {
        AdditionalProduct ap = additionalProductCombo.getValue();
        if (ap != null && !selectedAdditions.contains(ap)) {
            selectedAdditions.add(ap);
        }
    }

    /**
     * Build table of meals with edit and delete actions.
     */
    private TableView<Meal> buildTable() {
        table.setPrefHeight(400);

        TableColumn<Meal, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Meal, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(160);

        TableColumn<Meal, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<Meal, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imgCol.setPrefWidth(200);

        TableColumn<Meal, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(90);
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            { btn.setOnAction(e -> loadMealIntoForm(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<Meal, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(90);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            { btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, priceCol, imgCol, editCol, deleteCol);
        table.setItems(tableData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    /**
     * Populate form fields with selected meal data.
     */
    private void loadMealIntoForm(Meal meal) {
        idField.setText(String.valueOf(meal.getId()));
        nameField.setText(meal.getName());
        priceField.setText(meal.getPrice().toPlainString());
        basicProductCombo.setValue(meal.getBasicProduct());

        originalImageName = meal.getImage();
        chosenImageFile = null;
        imageNameLbl.setText(originalImageName != null ? originalImageName : "No file chosen");

        selectedAdditions.setAll(meal.getAdditionalProducts());
        messageLbl.setText("");
    }

    /**
     * Open file chooser to select a new meal image.
     */
    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Meal Image");
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
     * Refresh table data from database.
     */
    private void refreshTable() {
        try {
            List<Meal> meals = mealDb.getAllMeals(connection);
            tableData.setAll(meals);
        } catch (SQLException ex) {
            messageLbl.setText("Cannot load meals: " + ex.getMessage());
        }
    }

    /**
     * Update meal record, saving image if changed.
     */
    private void updateMeal() {
        messageLbl.setText("");
        messageLbl.setTextFill(Color.FIREBRICK);

        if (idField.getText().isEmpty()) {
            messageLbl.setText("Click Edit on a meal first.");
            return;
        }
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty() || basicProductCombo.getValue() == null) {
            messageLbl.setText("Name, price and basic product are required.");
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.signum() < 0) {
                messageLbl.setText("Price must be ≥ 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            messageLbl.setText("Invalid price format.");
            return;
        }

        // Decide which image name to use
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

        Meal meal = Meal.builder()
                .id(Long.parseLong(idField.getText()))
                .name(name)
                .price(price)
                .image(imageName)
                .basicProduct(basicProductCombo.getValue())
                .additionalProducts(new ArrayList<>(selectedAdditions))
                .build();

        try {
            mealDb.updateMealInDb(connection, meal);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Meal updated (id=" + meal.getId() + ").");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    /**
     * Clear form fields for new update.
     */
    private void clearForm() {
        idField.clear();
        nameField.clear();
        priceField.clear();
        basicProductCombo.getSelectionModel().clearSelection();
        additionalProductCombo.getSelectionModel().clearSelection();
        selectedAdditions.clear();
        chosenImageFile = null;
        originalImageName = null;
        imageNameLbl.setText("No file chosen");
        messageLbl.setText("");
    }

    /**
     * Prompt confirmation before deleting.
     */
    private void confirmAndDelete(Meal meal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete Meal");
        alert.setHeaderText("Delete \"" + meal.getName() + "\"?");
        alert.setContentText("This cannot be undone.");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            deleteMeal(meal);
        }
    }

    /**
     * Delete meal record from database.
     */
    private void deleteMeal(Meal meal) {
        try {
            mealDb.deleteMealById(connection, meal.getId());
            refreshTable();
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Deleted \"" + meal.getName() + "\".");
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }
}
