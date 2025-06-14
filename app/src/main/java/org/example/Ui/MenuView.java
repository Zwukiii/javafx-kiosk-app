package org.example.Ui;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.App;
import org.example.db.BasicProductDb;
import org.example.db.AdditionalProductDb;
import org.example.db.MealDb;
import org.example.domain.*;
import org.example.mappers.AdditionalProductMapper;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class MenuView extends BorderPane {

    private final BasicProductDb basicProductDb;
    private final AdditionalProductDb additionalProductDb;
    private final MealDb mealDb;
    private final Connection connection;
    private CustomizeView customizeView;


    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(
                    ProductCell.class.getResource("/images/placeholder.jpeg")
            ).toExternalForm(), true
    );


    private final ListView<Object> listView = new ListView<>();

    private final Label emptyLabel = new Label("NO Products");

    public MenuView(Connection connection) throws SQLException {
        setMinWidth(1152);
        setMaxWidth(1152);
        this.connection = connection;
        this.basicProductDb = new BasicProductDb();
        this.additionalProductDb = new AdditionalProductDb(
                new AdditionalProductMapper()
        );
        this.mealDb = new MealDb();

        Timer.getInstance().registerNode(this);

        try {
            List<BasicProduct> basics = basicProductDb.getAllBasicProducts(connection);
            List<Meal> meals = mealDb.getAllMeals(connection);
            CategoryMenuView categoryMenuView = new CategoryMenuView(connection, basics, meals);
            setCenter(categoryMenuView);
        } catch (SQLException ex) {
            showError("MenuView - Failed to load category menu", ex);
        }
    }


    private void configureListView() {
        listView.setPrefWidth(500);
        listView.setPlaceholder(emptyLabel);

        listView.setCellFactory(lv -> new ListCell<Object>() {
            private final ProductCell basicCell       = new ProductCell();
            private final ProductCell2 additionalCell = new ProductCell2();
            private final MealCell mealCell           = new MealCell();

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item instanceof BasicProduct bp) {
                    basicCell.updateItem(bp, empty);
                    setGraphic(basicCell.getGraphic());
                } else if (item instanceof AdditionalProduct ap) {
                    additionalCell.updateItem(ap, empty);
                    setGraphic(additionalCell.getGraphic());
                } else if (item instanceof Meal m) {
                    mealCell.updateItem(m, empty);
                    setGraphic(mealCell.getGraphic());
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane(listView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        listView.prefWidthProperty().bind(scrollPane.widthProperty());
        setCenter(scrollPane);
    }


    private void loadDataAndShow() {
        try {
            List<BasicProduct> basics       = basicProductDb.getAllBasicProducts(connection);
            List<AdditionalProduct> adds    = additionalProductDb.getAllAdditionalProducts(connection);
            List<Meal> meals                = mealDb.getAllMeals(connection);

            List<Object> all = new ArrayList<>();
            all.addAll(basics);
            all.addAll(adds);
            all.addAll(meals);

            listView.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException ex) {
            showError("MenuView - loadDataAndShow error", ex);
        }
    }


    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.CLOSE);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    //SINGLE CELL
    private final class ProductCell extends ListCell<BasicProduct> {

        private final ImageView icon = new ImageView();
        private final Label name = new Label();
        private final Label price = new Label();
        private final Label description = new Label();
        private final VBox textBox = new VBox(name, price);
        private final VBox descriptionBox = new VBox(description);
        private final Button addButton = new Button("Add");
        private final HBox root = new HBox(10, icon, textBox, descriptionBox, addButton);

        BasicProduct product;

        ProductCell() {
            icon.setFitWidth(64);
            icon.setFitHeight(64);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            textBox.setAlignment(Pos.CENTER_LEFT);

            name.setFont(Font.font(15));
            price.setOpacity(0.7);
            description.setFont(Font.font(10));
            description.setOpacity(0.7);
            addButton.setFont(Font.font(10));
            addButton.setOnAction(event -> {
                try {
                    customizeView = new CustomizeView(connection, product);
                    App.split.getItems().set(0, customizeView);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                App.cartView.refresh();

            });

        }


        @Override
        protected void updateItem(BasicProduct p, boolean empty) {
            super.updateItem(p, empty);
            if (empty || p == null) {
                setGraphic(null);
            } else {
                icon.setImage(loadImageOrPlaceholder(p.getImage()));
                name.setText(p.getName());
                description.setText(p.getIngredientInfo());

                // Get extra price of this product
                BigDecimal extra = p.getIngredients().stream()
                                .map(Ingredient::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPrice = p.getPrice().add(extra);
                price.setText(String.format("€ %.2f", totalPrice));
                setGraphic(root);
                product = p;
            }
        }


        private Image loadImageOrPlaceholder(String fileName) {
            URL url = getClass().getResource("/images/" + fileName);
            return (url != null)
                    ? new Image(url.toExternalForm(), true)  // backgroundLoading = true
                    : PLACEHOLDER;
        }


    }


    private final class ProductCell2 extends ListCell<AdditionalProduct> {

        private final ImageView icon = new ImageView();
        private final Label name = new Label();
        private final Label price = new Label();
        private final VBox textBox = new VBox(name, price);
        private final Button addButton = new Button("Add");
        private final HBox root = new HBox(10, icon, textBox, addButton);

        AdditionalProduct product;

        ProductCell2() {
            icon.setFitWidth(50);
            icon.setFitHeight(50);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            textBox.setAlignment(Pos.CENTER_LEFT);

            name.setFont(Font.font(15));
            price.setOpacity(0.7);
            addButton.setFont(Font.font(10));
            addButton.setOnAction(event -> {
                App.cart.addAdditionalProduct(product);
                App.replaceCartView(new CartView(App.cart));
            });

        }



        protected void updateItem(AdditionalProduct p, boolean empty) {
            super.updateItem(p, empty);
            if (empty || p == null) {
                setGraphic(null);
            } else {
                icon.setImage(loadImageOrPlaceholder(p.getImage()));
                name.setText(p.getName());
                price.setText(String.format("€ %.2f", p.getPrice()));
                setGraphic(root);
                product = p;
            }
        }

        private Image loadImageOrPlaceholder(String fileName) {
            URL url = getClass().getResource("/images/" + fileName);
            return (url != null)
                    ? new Image(url.toExternalForm(), true)  // backgroundLoading = true
                    : PLACEHOLDER;
        }
    }

    private final class MealCell extends ListCell<Meal> {

        private final ImageView icon = new ImageView();
        private final Label name = new Label();
        private final Label price = new Label();
        private final Label description = new Label();
        private final VBox descriptionBox = new VBox(description);
        private final VBox textBox = new VBox(name, price);
        private final Button addButton = new Button("Add");
        private final HBox root = new HBox(10, icon, textBox, descriptionBox, addButton);

        Meal meal;


        MealCell() {
            icon.setFitWidth(50);
            icon.setFitHeight(50);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            textBox.setAlignment(Pos.CENTER_LEFT);

            name.setFont(Font.font(10));
            price.setOpacity(0.5);
            description.setOpacity(0.5);
            description.setFont(Font.font(8));
            descriptionBox.setAlignment(Pos.CENTER);
            addButton.setFont(Font.font(10));
            addButton.setOnAction(event -> {
                try {
                    customizeView = new CustomizeView(connection, meal);
                    App.split.getItems().set(0, customizeView);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                App.cartView.refresh();

            });
        }

        protected void updateItem(Meal m, boolean empty) {
            super.updateItem(m, empty);
            if (empty || m == null) {
                setGraphic(null);
            } else {
                icon.setImage(loadImageOrPlaceholder(m.getImage()));
                name.setText(m.getName());
                price.setText(String.format("€ %.2f", m.getPrice()));

                // Load default ingredients price from DB (come with the basic product by default)
                List<Ingredient> defaultIngredients =
                        null;
                    try {
                        defaultIngredients = basicProductDb.getIngredientsForProduct(connection, m.getBasicProduct().getId());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                // Get sum of default ingredient’s price
                BigDecimal defaultTotal = defaultIngredients.stream()
                        .map(Ingredient::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Compute current ingredients price (including additions/removals)
                BigDecimal currentTotal = m.getBasicProduct().getIngredients().stream()
                        .map(Ingredient::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Difference
                BigDecimal extraIngredientCost = currentTotal.subtract(defaultTotal);

                // Sum price of any additional products
                BigDecimal additionalCost = m.getAdditionalProducts().stream()
                        .map(AdditionalProduct::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // total price = Basic meal + extra ingredient + additional
                BigDecimal totalPrice = m.getPrice()
                        .add(extraIngredientCost)
                        .add(additionalCost);

                // Display total price
                price.setText(String.format("€ %.2f", totalPrice));
                description.setText(m.getMealInfo());
                setGraphic(root);
                meal = m;
            }
        }

        private Image loadImageOrPlaceholder(String fileName) {
            URL url = getClass().getResource("/images/" + fileName);
            return (url != null)
                    ? new Image(url.toExternalForm(), true)  // backgroundLoading = true
                    : PLACEHOLDER;
        }
    }
}