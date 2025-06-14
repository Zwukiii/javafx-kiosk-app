package org.example.Ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.App;
import org.example.db.AdditionalProductDb;
import org.example.db.BasicProductDb;
import org.example.db.IngredientsDb;
import org.example.db.MealDb;
import org.example.domain.*;
import org.example.mappers.AdditionalProductMapper;
import org.example.mappers.IngredientMapper;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CustomizeView extends BorderPane {
    private BasicProductDb basicProductDb;
    private AdditionalProductDb additionalProductDb;
    private IngredientsDb ingredientsDb;
    private MealDb mealDb;
    private Connection connection;
    private Cart cart = App.cartView.getCart();

    private BasicProduct bp;
    private Meal meal;
    private List<Ingredient> ingredientList;
    private List<Ingredient> allIngredientsList;
    private List<AdditionalProduct> additionalProductList;

    private final ListView<Ingredient> listView4 = new ListView<>();
    private final ListView<AdditionalProduct> listView5 = new ListView<>();
    private final Label emptyLabel = new Label("NO Products");

    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(
                    CustomizeView.IngredientCell.class.getResource("/images/placeholder.jpeg")
            ).toExternalForm(), true
    );

    private static final Path IMAGE_BASE = Paths.get(System.getProperty("user.dir"), "image");


    public CustomizeView(Connection connection) throws SQLException {
    }

    public CustomizeView(Connection connection, BasicProduct bp) throws SQLException {
        this.connection = connection;
        this.basicProductDb = new BasicProductDb(
        );
        this.ingredientsDb = new IngredientsDb(
                new IngredientMapper()
        );
        this.bp = bp;
        this.ingredientList = basicProductDb.getIngredientsForProduct(connection, bp.getId());

        Timer.getInstance().registerNode(this);
        setMaxHeight(App.split.getHeight());
        setMinWidth(1152);
        setMaxWidth(1152);
        configureListView();
        loadDataAndShow();
    }

    public CustomizeView(Connection connection, Meal ml) throws SQLException {
        this.connection = connection;
        this.basicProductDb = new BasicProductDb(
        );
        this.mealDb = new MealDb(
        );
        this.additionalProductDb = new AdditionalProductDb(
                new AdditionalProductMapper()
        );
        this.ingredientsDb = new IngredientsDb(
                new IngredientMapper()
        );
        this.meal = ml;
        this.bp = meal.getBasicProduct();
        this.ingredientList = basicProductDb.getIngredientsForProduct(connection, bp.getId());
        this.allIngredientsList = ingredientsDb.getAllAvailableIngredients(connection);
        this.additionalProductList = mealDb.getAdditionalProductsForMeal(connection, meal.getId());

        setMaxHeight(App.split.getHeight());
        setMinWidth(1152);
        setMaxWidth(1152);
        configureListView2();
        loadDataAndShow2();
    }

    private void configureListView() {

        listView4.setPrefWidth(400);
        listView4.setPlaceholder(emptyLabel);
        listView4.setCellFactory(list -> new CustomizeView.IngredientCell());
        listView4.setStyle("-fx-background-color: transparent;");

        Button closeButton = new Button("Back");
        closeButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        Button confirmButton = new Button("Add to Cart");
        confirmButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        closeButton.setOnAction(event -> {
            bp.setIngredients(ingredientList);
            App.split.getItems().set(0, App.menuView);
            CustomizeView.isOpen = false;
        });

        confirmButton.setOnAction(event -> {

            BigDecimal initialPrice = ingredientList.stream()
                    .map(Ingredient::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal currentPrice = bp.getIngredients().stream()
                    .map(Ingredient::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal extraPrice = currentPrice.subtract(initialPrice);
            BigDecimal totalPrice = bp.getPrice().add(extraPrice);

            BasicProduct bpCopy = BasicProduct.builder()
                    .id(bp.getId())
                    .name(bp.getName())
                    .image(bp.getImage())
                    .price(totalPrice)
                    .type(bp.getType())
                    .size(bp.getSize())
                    .quantity(bp.getQuantity())
                    .ingredients(new ArrayList<>(bp.getIngredients()))
                    .build();

            cart.addBasicProduct(bpCopy);
            bp.setIngredients(ingredientList);

            App.split.getItems().set(0, App.menuView);
            App.replaceCartView(new CartView(App.cart));
            CustomizeView.isOpen = false;
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(closeButton, spacer, confirmButton);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");


        ImageView imageView = new ImageView(loadImageOrPlaceholder(bp.getImage()));
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(bp.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        BigDecimal initialPrice = ingredientList.stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentPrice = bp.getIngredients().stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal extraPrice = currentPrice.subtract(initialPrice);
        BigDecimal finalPrice = bp.getPrice().add(extraPrice);

        Label priceLabel = new Label(String.format("€%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        VBox headerBox = new VBox(10, imageView, nameLabel, priceLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label ingredientsTitle = new Label("Customize Ingredients");
        ingredientsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox ingredientsBox = new VBox(10, ingredientsTitle, listView4);
        ingredientsBox.setPadding(new Insets(20));
        ingredientsBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        ingredientsBox.setMinHeight(600);
        ingredientsBox.setMaxHeight(600);

        VBox mainLayout = new VBox(20, topBar, headerBox, ingredientsBox);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        setCenter(scrollPane);
    }

    private void loadDataAndShow() {
        try {
            List<Ingredient> ingredients = ingredientsDb.getAllAvailableIngredients(connection);
            listView4.setItems(FXCollections.observableArrayList(ingredients));
            listView4.setPrefHeight(600);

        } catch (SQLException ex) {
            showError("CustomizeView of BasicProduct - loadDataAndShow error", ex);
        }
    }

    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.CLOSE);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    //SINGLE CELL
    private final class IngredientCell extends ListCell<Ingredient> {

        private final ImageView icon = new ImageView();
        private final Label name = new Label();
        private final Label price = new Label();
        private final VBox textBox = new VBox(name, price);
        private final Button addButton = new Button("+");
        private final Button removeButton = new Button("-");

        private Label qty = new Label();
        private final HBox buttonBox = new HBox(5,
                removeButton,
                qty,
                addButton
        );
        private final HBox root = new HBox(10, icon, textBox, buttonBox);

        Ingredient ingredient;

        IngredientCell() {

            icon.setFitWidth(64);
            icon.setFitHeight(64);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            textBox.setAlignment(Pos.CENTER_LEFT);

            name.setFont(Font.font(15));
            price.setOpacity(0.7);

            double btnSize = 24;
            addButton.setPrefSize(btnSize, btnSize);
            addButton.setMinSize(btnSize, btnSize);
            addButton.setMaxSize(btnSize, btnSize);
            removeButton.setPrefSize(btnSize, btnSize);
            removeButton.setMinSize(btnSize, btnSize);
            removeButton.setMaxSize(btnSize, btnSize);

            buttonBox.setAlignment(Pos.CENTER);
            root.setAlignment(Pos.CENTER_LEFT);

            addButton.setFont(Font.font(10));
            addButton.setOnAction(event -> {
                bp.getIngredients().add(ingredient);
                if (meal != null) {
                    meal.setBasicProduct(bp);
                    configureListView2();
                    loadDataAndShow2();
                } else {
                    configureListView();
                    loadDataAndShow();
                }
            });

            removeButton.setOnAction(event -> {
                for (Ingredient i : bp.getIngredients()) {
                    if (i.getName().equals(ingredient.getName())) {
                        bp.getIngredients().remove(i);
                        if (meal != null) {
                            meal.setBasicProduct(bp);
                            configureListView2();
                            loadDataAndShow2();
                        } else {
                            configureListView();
                            loadDataAndShow();
                        }
                        break;
                    }
                }
            });
        }

        protected void updateItem(Ingredient i, boolean empty) {
            super.updateItem(i, empty);
            if (empty || i == null) {
                setGraphic(null);
            } else {
                icon.setImage(loadImageOrPlaceholder(i.getImage()));
                name.setText(i.getName());
                price.setText(String.format("€ %.2f", i.getPrice()));
                int q = 0;
                for (Ingredient j : bp.getIngredients()) {
                    if (j.getName().equals(i.getName())) {
                        q += 1;
                    }
                }
                qty.setText("  " + q);

                setGraphic(root);
                ingredient = i;
            }
        }

    }

    private Image loadImageOrPlaceholder(String fileName) {
        Path localPath = IMAGE_BASE.resolve(fileName);
        if (Files.exists(localPath)) {
            try {
                return new Image(localPath.toUri().toString(), true);
            } catch (Exception e) {
                System.err.println("Error loading local file '" + localPath + "': " + e.getMessage());
            }
        }

        try {
            URL url = getClass().getResource("/images/" + fileName);
            if (url != null) {
                return new Image(url.toExternalForm(), true);
            } else {
                // WE CAN FIND WITH ONE IMAGE WE NEED TO ADD TO DIRECTORY
                System.err.println("Resource not found: /images/" + fileName);
            }
        } catch (Exception e) {
            System.err.println("Error loading resource '/images/" + fileName + "': " + e.getMessage());
        }

        return PLACEHOLDER;
    }

    private void configureListView2() {
        listView4.setPrefWidth(400);
        listView4.setPlaceholder(emptyLabel);
        listView4.setCellFactory(list -> new CustomizeView.IngredientCell());
        listView4.setStyle("-fx-background-color: transparent;");

        listView5.setPrefWidth(400);
        listView5.setPlaceholder(emptyLabel);
        listView5.setCellFactory(list -> new CustomizeView.AdditionalProductCell());
        listView5.setStyle("-fx-background-color: transparent;");

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        Button confirmButton = new Button("Add to Cart");
        confirmButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        backButton.setOnAction(event -> {
            bp.setIngredients(ingredientList);
            meal.setAdditionalProducts(additionalProductList);
            App.split.getItems().set(0, App.menuView);
            CustomizeView.isOpen = false;
        });

        confirmButton.setOnAction(event -> {
            BigDecimal additionalProductsPrice = additionalProductList.stream().map(AdditionalProduct::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal initialIngredientsPrice = ingredientList.stream().map(Ingredient::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ingredientsPrice = bp.getIngredients().stream().map(Ingredient::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal extraIngredientsPrice = ingredientsPrice.subtract(initialIngredientsPrice);

            BasicProduct bpCopy = BasicProduct.builder()
                    .id(bp.getId()).name(bp.getName()).image(bp.getImage()).price(bp.getPrice())
                    .type(bp.getType()).size(bp.getSize()).quantity(bp.getQuantity())
                    .ingredients(new ArrayList<>(bp.getIngredients())).build();

            Meal mealCopy = Meal.builder()
                    .id(meal.getId()).name(meal.getName()).image(meal.getImage())
                    .price(meal.getTotalPrice().subtract(additionalProductsPrice).add(extraIngredientsPrice))
                    .basicProduct(bpCopy).quantity(meal.getQuantity())
                    .additionalProducts(new ArrayList<>(meal.getAdditionalProducts())).build();

            cart.addMeal(mealCopy);
            bp.setIngredients(ingredientList);
            meal.setAdditionalProducts(additionalProductList);

            App.split.getItems().set(0, App.menuView);
            App.replaceCartView(new CartView(App.cart));
            CustomizeView.isOpen = false;
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(backButton, spacer, confirmButton);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        ImageView mealImage = new ImageView(loadImageOrPlaceholder(meal.getImage()));
        mealImage.setFitHeight(120);
        mealImage.setFitWidth(120);
        mealImage.setPreserveRatio(true);

        Label nameLabel = new Label(meal.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        BigDecimal additionalProductsPrice = additionalProductList.stream().map(AdditionalProduct::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal initialIngredientsPrice = ingredientList.stream().map(Ingredient::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingredientsPrice = bp.getIngredients().stream().map(Ingredient::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal extraIngredientsPrice = ingredientsPrice.subtract(initialIngredientsPrice);
        BigDecimal finalPrice = meal.getTotalPrice().subtract(additionalProductsPrice).add(extraIngredientsPrice);

        Label priceLabel = new Label(String.format("€%.2f", finalPrice));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        VBox headerBox = new VBox(10, mealImage, nameLabel, priceLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        VBox ingredientsBox = new VBox(10, listView4);
        ingredientsBox.setPadding(new Insets(20));
        ingredientsBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        VBox additionalBox = new VBox(10, listView5);
        additionalBox.setPadding(new Insets(20));
        additionalBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        ingredientsBox.setMinHeight(290);
        ingredientsBox.setMaxHeight(290);

        additionalBox.setMinHeight(290);
        additionalBox.setMaxHeight(290);

        VBox layout = new VBox(20, topBar, headerBox, ingredientsBox, additionalBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        setCenter(scrollPane);
    }

    private void loadDataAndShow2() {
        try {
            List<Ingredient> ingredients = basicProductDb.getIngredientsForProduct(connection, bp.getId());
            listView4.setItems(FXCollections.observableArrayList(ingredients));
            List<AdditionalProduct> additionalProducts = additionalProductDb.getAllAdditionalProducts(connection);
            listView5.setItems(FXCollections.observableArrayList(additionalProducts));

            if (meal.getId() == 1L) {
                listView4.setItems(FXCollections.observableArrayList(allIngredientsList));
            }
        } catch (SQLException ex) {
            showError("CustomizeView of Meal - loadDataAndShow error", ex);
        }
    }

    private final class AdditionalProductCell extends ListCell<AdditionalProduct> {
        private final ImageView icon = new ImageView();
        private final Label name = new Label();
        private final Label price = new Label();
        private final VBox textBox = new VBox(name, price);
        private final Button addButton = new Button("+");
        private final Button removeButton = new Button("-");
        private final Label qty = new Label();
        private final HBox buttonBox = new HBox(5, removeButton, qty, addButton);
        private final HBox root = new HBox(10, icon, textBox, buttonBox);

        AdditionalProduct additionalProduct;

        AdditionalProductCell() {
            icon.setFitWidth(64);
            icon.setFitHeight(64);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            textBox.setAlignment(Pos.CENTER_LEFT);
            name.setFont(Font.font(15));
            price.setOpacity(0.7);
            addButton.setFont(Font.font(10));
            removeButton.setFont(Font.font(10));
            addButton.setPrefSize(24, 24);
            removeButton.setPrefSize(24, 24);
            buttonBox.setAlignment(Pos.CENTER);
            root.setAlignment(Pos.CENTER_LEFT);

            addButton.setOnAction(event -> {
                meal.getAdditionalProducts().add(additionalProduct);
                configureListView2();
                CustomizeView.this.loadDataAndShow2();
            });

            removeButton.setOnAction(event -> {
                for (AdditionalProduct ap : meal.getAdditionalProducts()) {
                    if (ap.getName().equals(additionalProduct.getName())) {
                        meal.getAdditionalProducts().remove(ap);
                        configureListView2();
                        CustomizeView.this.loadDataAndShow2();
                        break;
                    }
                }
            });
        }

        @Override
        protected void updateItem(AdditionalProduct ap, boolean empty) {
            super.updateItem(ap, empty);
            if (empty || ap == null) {
                setGraphic(null);
            } else {
                icon.setImage(loadImageOrPlaceholder(ap.getImage()));
                name.setText(ap.getName());
                price.setText(String.format("€ %.2f", ap.getPrice()));
                int q = 0;
                for (AdditionalProduct k : meal.getAdditionalProducts()) {
                    if (k.getName().equals(ap.getName())) {
                        q += 1;
                    }
                }
                qty.setText("  " + q);
                additionalProduct = ap;
                setGraphic(root);
            }
        }
    }

    protected static boolean isOpen = false;

}