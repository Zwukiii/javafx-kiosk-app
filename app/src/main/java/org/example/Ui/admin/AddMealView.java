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
 * View for adding new meals with image and product selections.
 */
public class AddMealView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final Connection connection = databaseInitializer.connection();

    private final MealDb mealDb = new MealDb();
    private final BasicProductDb basicProductDb = new BasicProductDb();
    private final AdditionalProductDb additionalProductDb = new AdditionalProductDb();

    // ---------------------- Form Controls ------------------------------
    private final TextField nameField = new TextField();
    private final TextField priceField = new TextField();

    private final Button btnChooseImage = new Button("Choose Image…");
    private final Label imageNameLbl = new Label("No file chosen");
    private File chosenImageFile = null;

    private final ComboBox<BasicProduct> basicProductCombo = new ComboBox<>();
    private final ComboBox<AdditionalProduct> additionalProductCombo = new ComboBox<>();

    private final ObservableList<AdditionalProduct> selectedAdditions = FXCollections.observableArrayList();
    private final ListView<AdditionalProduct> selectedList = new ListView<>(selectedAdditions);

    private final Label messageLbl = new Label();

    // ---------------------- Table --------------------------------------
    private final TableView<Meal> table = new TableView<>();
    private final ObservableList<Meal> tableData = FXCollections.observableArrayList();

    /**
     * Constructor: initializes UI and data.
     */
    public AddMealView() throws SQLException {
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
        BorderPane.setMargin(getCenter(), new Insets(20, 0, 0, 0));

        refreshTable();
    }

    /**
     * Build header label.
     */
    private Label buildHeader() {
        Label header = new Label("Add New Meal");
        header.getStyleClass().add("h2");
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0));
        return header;
    }

    /**
     * Build form for input fields and save/clear buttons.
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

        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(new Label("Price:"), 0, 1);
        form.add(priceField, 1, 1);

        // Image chooser
        form.add(new Label("Image:"), 0, 2);
        HBox imageBox = new HBox(10, btnChooseImage, imageNameLbl);
        form.add(imageBox, 1, 2);
        btnChooseImage.setOnAction(e -> openFileChooser());

        form.add(new Label("Basic Product:"), 0, 3);
        basicProductCombo.setPrefWidth(200);
        form.add(basicProductCombo, 1, 3);

        form.add(new Label("Add Addition:"), 0, 4);
        additionalProductCombo.setPrefWidth(200);
        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> addSelectedAdditional());
        HBox addBox = new HBox(5, additionalProductCombo, btnAdd);
        form.add(addBox, 1, 4);

        // Save / Clear buttons
        Button btnSave = new Button("Save");
        Button btnClear = new Button("Clear");
        btnSave.setOnAction(e -> saveMeal());
        btnClear.setOnAction(e -> clearForm());
        HBox buttonsBox = new HBox(10, btnSave, btnClear);
        form.add(buttonsBox, 1, 5);

        form.add(messageLbl, 1, 6);
        messageLbl.setTextFill(Color.FIREBRICK);

        return form;
    }

    /**
     * Build panel showing selected additional products.
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
     * Build meals table with delete action.
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

        TableColumn<Meal, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            { btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, priceCol, imgCol, actionCol);
        table.setItems(tableData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    /**
     * Add selected additional product to list.
     */
    private void addSelectedAdditional() {
        AdditionalProduct sel = additionalProductCombo.getValue();
        if (sel != null && !selectedAdditions.contains(sel)) {
            selectedAdditions.add(sel);
        }
    }

    /**
     * Open file chooser for meal image.
     */
    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Meal Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            chosenImageFile = file; imageNameLbl.setText(file.getName());
        }
    }

    /**
     * Refresh meals table from database.
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
     * Save new meal, including image file.
     */
    private void saveMeal() {
        messageLbl.setText(""); messageLbl.setTextFill(Color.FIREBRICK);
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        if (name.isEmpty() || priceStr.isEmpty() || basicProductCombo.getValue() == null || chosenImageFile == null) {
            messageLbl.setText("All fields (including image) are required."); return;
        }
        BigDecimal price;
        try { price = new BigDecimal(priceStr); if (price.signum() < 0) { messageLbl.setText("Price must be ≥ 0."); return; } }
        catch (NumberFormatException ex) { messageLbl.setText("Invalid price format."); return; }

        String imageName;
        try {
            Path saved = ImageStorage.saveImage(chosenImageFile);
            imageName = saved.getFileName().toString();
        } catch (IOException ex) {
            messageLbl.setText("Error saving image: " + ex.getMessage()); return;
        }

        Meal meal = Meal.builder()
                .name(name)
                .price(price)
                .image(imageName)
                .basicProduct(basicProductCombo.getValue())
                .additionalProducts(new ArrayList<>(selectedAdditions))
                .build();

        try {
            mealDb.createNewMealInDb(connection, meal);
            messageLbl.setTextFill(Color.GREEN); messageLbl.setText("Meal saved (id=" + meal.getId() + ").");
            clearForm(); refreshTable();
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK); messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    /**
     * Clear form inputs for new entry.
     */
    private void clearForm() {
        nameField.clear(); priceField.clear();
        chosenImageFile = null; imageNameLbl.setText("No file chosen");
        basicProductCombo.getSelectionModel().clearSelection();
        additionalProductCombo.getSelectionModel().clearSelection();
        selectedAdditions.clear(); messageLbl.setText("");
    }

    /**
     * Prompt user to confirm deletion.
     */
    private void confirmAndDelete(Meal meal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow()); alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete Meal");
        alert.setHeaderText("Delete \"" + meal.getName() + "\"?");
        alert.setContentText("This cannot be undone.");
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) deleteMeal(meal);
    }

    /**
     * Delete meal from database.
     */
    private void deleteMeal(Meal meal) {
        try {
            mealDb.deleteMealById(connection, meal.getId());
            refreshTable(); messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Deleted \"" + meal.getName() + "\".");
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }
}