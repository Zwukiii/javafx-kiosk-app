package org.example.Ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.example.config.DatabaseInitializer;
import org.example.config.ImageStorage;
import org.example.db.BasicProductDb;
import org.example.db.IngredientsDb;
import org.example.domain.BasicProduct;
import org.example.domain.Ingredient;
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

public class AddBasicProductView extends BorderPane {

    private final DatabaseInitializer    databaseInitializer    = new DatabaseInitializer();
    private final IngredientsDb          ingredientsDb          = new IngredientsDb();
    private final BasicProductDb         basicProductDb         = new BasicProductDb();
    private final Connection             connection             = databaseInitializer.connection();

    private final List<Ingredient>       availableIngredients   = ingredientsDb.getAllAvailableIngredients(connection);

    // Form fields
    private final TextField              nameField              = new TextField();
    private final TextField              priceField             = new TextField();

    private final Button                 btnChooseImage         = new Button("Choose Image…");
    private final Label                  imageNameLbl           = new Label("No file chosen");
    private File                          chosenImageFile        = null;

    private final ComboBox<Type>         typeCombo              = new ComboBox<>();
    private final ComboBox<Size>         sizeCombo              = new ComboBox<>();
    private final ComboBox<Ingredient>   ingredientCombo        = new ComboBox<>();

    private final ObservableList<Ingredient> selectedIngredients = FXCollections.observableArrayList();
    private final ListView<Ingredient>       selectedList        = new ListView<>(selectedIngredients);

    private final TableView<BasicProduct>    table               = new TableView<>();
    private final ObservableList<BasicProduct> tableData         = FXCollections.observableArrayList();

    private final Label messageLbl = new Label();

    public AddBasicProductView() throws SQLException {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setLeft(buildForm());
        setRight(buildSelectedIngredientsPanel());
        setBottom(buildTable());
        refreshTable();
    }

    private Label buildHeader() {
        Label header = new Label("Add New Basic Product");
        header.getStyleClass().add("h2");
        return header;
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Name:"),        0, 0);
        form.add(nameField,                 1, 0);

        form.add(new Label("Price:"),       0, 1);
        form.add(priceField,                1, 1);

        form.add(new Label("Image:"),       0, 2);
        HBox imageBox = new HBox(10, btnChooseImage, imageNameLbl);
        form.add(imageBox,                  1, 2);
        btnChooseImage.setOnAction(e -> openFileChooser());

        form.add(new Label("Type:"),        0, 3);
        typeCombo.setItems(FXCollections.observableArrayList(Type.values()));
        form.add(typeCombo,                 1, 3);

        form.add(new Label("Size:"),        0, 4);
        sizeCombo.setItems(FXCollections.observableArrayList(Size.values()));
        form.add(sizeCombo,                 1, 4);

        form.add(new Label("Add Ingredient:"), 0, 5);
        ingredientCombo.setItems(FXCollections.observableArrayList(availableIngredients));
        ingredientCombo.setPrefWidth(180);
        Button btnAddIng = new Button("Add");
        btnAddIng.setOnAction(e -> {
            Ingredient sel = ingredientCombo.getValue();
            if (sel != null && !selectedIngredients.contains(sel)) {
                selectedIngredients.add(sel);
            }
        });
        form.add(new HBox(5, ingredientCombo, btnAddIng), 1, 5);

        Button btnSave  = new Button("Save");
        Button btnClear = new Button("Clear");
        btnSave.setOnAction(e -> saveProduct());
        btnClear.setOnAction(e -> clearForm());
        form.add(new HBox(10, btnSave, btnClear), 1, 6);

        form.add(messageLbl,                1, 7);
        messageLbl.setTextFill(Color.FIREBRICK);

        return form;
    }

    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Product Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            chosenImageFile = file;
            imageNameLbl.setText(file.getName());
        }
    }

    private VBox buildSelectedIngredientsPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(0,0,0,20));
        Label lbl = new Label("Selected Ingredients");
        selectedList.setPrefSize(200, 180);
        Button btnRemove = new Button("Remove Selected");
        btnRemove.setOnAction(e -> {
            Ingredient sel = selectedList.getSelectionModel().getSelectedItem();
            if (sel != null) selectedIngredients.remove(sel);
        });
        box.getChildren().addAll(lbl, selectedList, btnRemove);
        return box;
    }

    private TableView<BasicProduct> buildTable() {
        TableColumn<BasicProduct, Long> idCol   = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); idCol.setPrefWidth(60);
        TableColumn<BasicProduct, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); nameCol.setPrefWidth(180);
        TableColumn<BasicProduct, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price")); priceCol.setPrefWidth(100);
        TableColumn<BasicProduct, Type> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type")); typeCol.setPrefWidth(100);
        TableColumn<BasicProduct, Size> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size")); sizeCol.setPrefWidth(100);
        TableColumn<BasicProduct, String> imgCol = new TableColumn<>("Image");
        imgCol.setCellValueFactory(new PropertyValueFactory<>("image")); imgCol.setPrefWidth(200);
        TableColumn<BasicProduct, Void> actionCol = new TableColumn<>("Action"); actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            { btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        table.getColumns().addAll(idCol, nameCol, priceCol, typeCol, sizeCol, imgCol, actionCol);
        table.setItems(tableData); table.setPrefHeight(300);
        return table;
    }

    private void refreshTable() {
        try {
            List<BasicProduct> list = basicProductDb.getAllBasicProducts(connection);
            tableData.setAll(list);
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Cannot load products: " + ex.getMessage());
        }
    }

    private void saveProduct() {
        messageLbl.setText(""); messageLbl.setTextFill(Color.FIREBRICK);

        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty()
                || typeCombo.getValue()==null
                || sizeCombo.getValue()==null
                || chosenImageFile==null) {
            messageLbl.setText("All fields—including selecting an image—are required.");
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


        String imageFileName;
        try {
            Path saved = ImageStorage.saveImage(chosenImageFile);
            imageFileName = saved.getFileName().toString();
        } catch (IOException ex) {
            messageLbl.setText("Error saving image: " + ex.getMessage());
            return;
        }


        BasicProduct bp = BasicProduct.builder()
                .name(name)
                .price(price)
                .image(imageFileName)
                .type(typeCombo.getValue())
                .size(sizeCombo.getValue())
                .ingredients(List.copyOf(selectedIngredients))
                .build();

        try {
            basicProductDb.createNewBasicProductInDb(connection, bp);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Product saved (id=" + bp.getId() + ").");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear(); priceField.clear();
        chosenImageFile = null; imageNameLbl.setText("No file chosen");
        typeCombo.getSelectionModel().clearSelection();
        sizeCombo.getSelectionModel().clearSelection();
        selectedIngredients.clear(); ingredientCombo.getSelectionModel().clearSelection();
        messageLbl.setText("");
    }

    private void confirmAndDelete(BasicProduct bp) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete \"" + bp.getName() + "\"?");
        alert.setContentText("This cannot be undone.");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no  = new ButtonType("No",  ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            deleteProduct(bp);
        }
    }

    private void deleteProduct(BasicProduct bp) {
        try {
            basicProductDb.deleteBasicProductById(connection, bp.getId());
            refreshTable();
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Deleted \"" + bp.getName() + "\".");
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }
}