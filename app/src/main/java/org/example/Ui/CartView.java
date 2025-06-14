package org.example.Ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.example.App;
import org.example.config.DatabaseInitializer;
import org.example.db.BasicProductDb;
import org.example.db.MealDb;
import org.example.db.OrderDb;
import org.example.db.RabatCodeDb;
import org.example.domain.*;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Getter
@Setter
public class CartView extends VBox {
    private final Label cartTitle = new Label("Your order");
    private final VBox itemsBox = new VBox(10);
    private final Label totalLabel = new Label("Total: 0.00");
    private final Button checkoutButton = new Button("Checkout");
    private final Cart cart;
    private final Button discountPrice = new Button("Discount button");
    private final Button cancelButton = new Button("Back to Start");
    private final DatabaseInitializer dbpromo = new DatabaseInitializer();
    private final MealDb mealDb = new MealDb();
    private final BasicProductDb basicProductDb = new BasicProductDb();
    private PauseTransition delay = new PauseTransition(Duration.seconds(3));

    // So that warningDialog is able to call it
    private final CartView cartView = this;

    private ScrollPane scrollPane;
    private static final Path IMAGE_BASE = Paths.get(System.getProperty("user.dir"), "image");


    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(
                    CartView.class.getResource("/images/placeholder.jpeg")
            ).toExternalForm(), true
    );


    public CartView(Cart cart) {
        this.cart = cart;
        Timer.getInstance().registerNode(this);
        setSpacing(20);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_CENTER);
        discountButton();
        updateTotal();
        styleButtons();

        BrandIdentity logo = new BrandIdentity(150);


        logo.setMaxWidth(150);
        logo.setMaxHeight(150);
        getChildren().add(logo);
        setSpacing(20);
        setPadding(new Insets(15));
        setAlignment(Pos.TOP_CENTER);

        setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.14), 16, 0, 0, 2);"
        );

        // Stylish cart title with black text, blurry background, and rounded edges
        styleCartTitle();

        // Adds the cart title
        getChildren().add(cartTitle);

        itemsBox.setAlignment(Pos.TOP_CENTER);

        loadCartItems();
        configureBottom();
    }


    private void styleButtons(){
        String buttonStyle =
                "-fx-background-color: linear-gradient(to bottom, #ff3b30, #dc1c13); " +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);" +
                        "-fx-text-fill: white;" + "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-min-width: 100px";



        String buttonStyleDiscount =
                "-fx-background-color: #2196F3FF;"  +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);" +
                        "-fx-text-fill: white;" + "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-min-width: 100px";

        String checkOutStyleButton =
                "-fx-background-color: #FFA726FF;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);" +
                        "-fx-text-fill: white;" + "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-min-width: 100px";


        checkoutButton.setStyle(checkOutStyleButton);
        cancelButton.setStyle(buttonStyle);
        discountPrice.setStyle(buttonStyleDiscount);

    }

    private void clearCartItems() {
        cart.clearCart();
        App.replaceCartView(new CartView(cart));
        refresh();

    }

    private void showReceipt() {
        // Start building the receipt text as a string
        StringBuilder receipt = new StringBuilder("THANK YOU FOR YOUR PURCHASE! Enjoy your meal!\n\n");

        // Add a randomly generated order number (8 characters, uppercase)
        String orderNumber = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        receipt.append("Order Number: ").append(orderNumber).append("\n");
        receipt.append("Dining Type: ").append(cart.getDineType()).append("\n\n");

        // List all meals in the cart
        for (Meal meal : cart.getMeals()) {
            receipt.append("- Meal: ").append(meal.getName()).append(" - ").append(meal.getPrice()).append(" €\n");

        }

        // List all basic products in the cart
        for (BasicProduct bp : cart.getBasicProducts()) {
            receipt.append("- Basic product: ").append(bp.getName()).append(" - ").append(bp.getPrice()).append(" €\n");

        }

        // List all additional products in the cart
        for (AdditionalProduct ap : cart.getAdditionalProducts()) {
            receipt.append("- Additional product: ").append(ap.getName());

            if (ap.getQuantity() > 1) {
                receipt.append(" x").append(ap.getQuantity());
            }
            receipt.append(" - ").append(ap.getPrice()).append(" €\n");

        }

        // Add a total price section
        receipt.append("\n-----------------------------\n");
        receipt.append("Total: ");
        receipt.append(cart.getTotalPrice().setScale(2));
        receipt.append(" €\n");
        receipt.append("-----------------------------\n");
        receipt.append("Date: ");
        receipt.append(LocalDateTime.now());
        receipt.append("\n");

        // Add the current date and time

        // Create a non-editable text area to show the receipt content
        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);                      // Prevent editing
        receiptArea.setWrapText(true);                       // Wrap lines
        receiptArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-background-color: white;");
        receiptArea.setPrefSize(380, 580);

        // Place the text area inside a scroll pane for better viewing
        ScrollPane scrollPane = new ScrollPane(receiptArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Button confrimButton = new Button("Confirm");
        Button cancelBUtton = new Button("Cancel");

        String buttonStyleConfirm =
                "-fx-background-color: linear-gradient(to bottom, #ff3b30, #dc1c13); " +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);" +
                        "-fx-text-fill: white;" + "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-min-width: 100px";


        String buttonStyleCancel =
                "-fx-background-color: linear-gradient(to bottom, #dadada, #a9a8a8); " +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);" +
                        "-fx-text-fill: white;" + "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-min-width: 100px";

        confrimButton.setStyle(buttonStyleConfirm);
        cancelBUtton.setStyle(buttonStyleCancel);



        HBox buttonBox = new HBox(20, confrimButton, cancelBUtton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Logo for the Receipt
        BrandIdentity logorecp = new BrandIdentity(100);
        HBox logorecipet = new HBox(logorecp);
        logorecipet.setMinHeight(100);
        logorecipet.setPrefHeight(100);
        logorecipet.setPadding(new Insets(0,0,10,0));
        logorecipet.setAlignment(Pos.TOP_CENTER);


        // Set up the layout container for the receipt window
        VBox layout = new VBox(10,logorecipet, scrollPane, buttonBox);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);


        // Create and show the new window (Stage) for the receipt
        Stage receiptStage = new Stage();
        receiptStage.setTitle("Receipt");
        Scene scene = new Scene(layout, 400, 600);
        receiptStage.setScene(scene);
        receiptStage.setResizable(false);

        confrimButton.setOnAction(e -> {
            receiptStage.close();
            orderConformation();
            // Go back to the welcome view 3 seconds later
            delay.setOnFinished(ev2 -> App.resetToWelcome());
            delay.play();
        });

        cancelBUtton.setOnAction(e -> receiptStage.close());

        // ORDER
        Order order = new Order(cart, orderNumber);
        OrderDb orderDb = new OrderDb();
        try {
            // new order in DB
            if (cart.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
                orderDb.createOrder(dbpromo.connection(), order);
                receiptStage.show();
            }

            //Clear CARD AND REFRESH
            clearCartItems();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        receiptStage.show();
    }

    private void styleCartTitle() {
        // Set font size, weight, and color
        cartTitle.setFont(Font.font("Arial", 24));
        cartTitle.setTextFill(Color.BLACK);  // Set text to black

        // Set padding around the label
        cartTitle.setPadding(new Insets(10));


        cartTitle.setStyle(
                "-fx-background-radius: 15; " + // Rounded corners
                        "-fx-padding: 10; " +           // Padding around the text
                        "-fx-border-width: 0;"         // No border around the label
        );

        // Apply Gaussian blur effect on the background behind the text
        GaussianBlur blur = new GaussianBlur();
        blur.setRadius(10);  // Adjust blur radius to control the blurriness
        cartTitle.setEffect(blur);

        // Add drop shadow for a modern effect
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(3);
        shadow.setOffsetY(3);
        shadow.setColor(Color.GRAY);
        shadow.setRadius(5);
        cartTitle.setEffect(shadow);

        // Add a semi-transparent background color behind the text
        BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(0, 0, 0, 0.3), new CornerRadii(15), Insets.EMPTY);
        Background background = new Background(backgroundFill);
        cartTitle.setBackground(background);
    }


    public void loadCartItems() {
        itemsBox.getChildren().clear();

        List<Object> allItems = new ArrayList<>();
        if (cart.getMeals() != null) {
            allItems.addAll(cart.getMeals());
        }
        if (cart.getBasicProducts() != null) {
            allItems.addAll(cart.getBasicProducts());
        }
        if (cart.getAdditionalProducts() != null) {
            allItems.addAll(cart.getAdditionalProducts());
        }

        for (Object item : allItems) {
            itemsBox.getChildren().add(createCartItem(item));
        }

        if (scrollPane == null) {
            scrollPane = new ScrollPane(itemsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(this.getHeight() * 0.60);
            scrollPane.setMaxHeight(400);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            getChildren().add(scrollPane);
        } else {
            scrollPane.setContent(itemsBox);
        }
    }


    private final java.util.Map<Object, List<String>> itemOptionsMap = new java.util.HashMap<>();


    private VBox createCartItem(Object item) {
        Label nameLabel = new Label();
        Label priceLabel = new Label();
        Label ingredientsLabel = new Label();
        Label additionalLabel = new Label();
        Button optionsButton = new Button("Customize");
        Button decreaseButton = new Button("-");
        Button increaseButton = new Button("+");
        Button removeButton = new Button("Remove");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);

        String optionsButtonStyle =
                "-fx-background-color: linear-gradient(to bottom, #ffa726, #ffa726); "+
                "-fx-background-radius: 8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"+
                "-fx-cursor: hand;" +
                "-fx-padding: 4 10 4 10;"+
                "-fx-font-size: 11px";


        String quantityButtonStyle =
                "-fx-background-color: linear-gradient(to bottom, #FF3B30, #DC143C); "+
                "-fx-background-radius: 8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"+
                "-fx-padding: 2 8 2 8;"+
                "-fx-min-width: 25px;"+
                "-fx-font-size: 12px;";



        String removeButtonStyle =
                "-fx-background-color: linear-gradient(to  bottom, #FF3B30, #DC143C); " +
                "-fx-background-radius:  8;" +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 4 8 4 8; " +
                "-fx-font-size: 11px;";

        optionsButton.setStyle(optionsButtonStyle);
        decreaseButton.setStyle(quantityButtonStyle);
        increaseButton.setStyle(quantityButtonStyle);
        removeButton.setStyle(removeButtonStyle);




        int initialQuantity = 1;
        AdditionalProduct ap = null;
        if (item instanceof Meal meal) {
            nameLabel.setText(meal.getName());
            priceLabel.setText(String.format("€ %.2f", meal.getPrice()));
            imageView.setImage(loadImageOrPlaceholder(meal.getImage()));
            if (meal.getBasicProduct() != null && meal.getBasicProduct().getIngredients() != null) {
                String ingredients = meal.getBasicProduct().getIngredients().stream()
                        .collect(Collectors.groupingBy(
                                Ingredient::getName,  Collectors.counting()
                        ))
                        .entrySet().stream()
                        .map(e -> e.getKey() + (e.getValue() > 1 ? " x" + e.getValue(): ""))
                        .collect(Collectors.joining(", "));
                ingredientsLabel.setText("Ingredients: " + ingredients);
            }

            if (!meal.getAdditionalProducts().isEmpty()) {
                String additions = meal.getAdditionalProducts().stream()
                        .collect(Collectors.groupingBy(
                                AdditionalProduct::getName, Collectors.counting()
                        ))
                        .entrySet().stream()
                        .map(e -> e.getKey() + (e.getValue() > 1 ? " x" + e.getValue(): ""))
                        .collect(Collectors.joining(", "));
                additionalLabel.setText("Extras: " + additions);
            }
            optionsButton.setOnAction(e -> {
                try{
                    if (CustomizeView.isOpen) return;
                    cart.getMeals().remove(meal);
                    Meal initialMeal = mealDb.getBasicMealByIdFromDb(App.conn, meal.getId());
                    BigDecimal differentPrice = meal.getPrice().subtract(initialMeal.getPrice());
                    meal.setPrice(meal.getPrice().subtract(differentPrice));
                    CustomizeView customizeView = new CustomizeView(dbpromo.connection(), meal);
                    App.split.getItems().set(0,customizeView);
                    refresh();
                    CustomizeView.isOpen = true;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

        } else if (item instanceof BasicProduct bp) {
            nameLabel.setText(bp.getName());
            priceLabel.setText(String.format("€ %.2f", bp.getPrice()));
            imageView.setImage(loadImageOrPlaceholder(bp.getImage()));

            if (bp.getIngredients() != null) {
                String ingredients = bp.getIngredients().stream()
                        .collect(Collectors.groupingBy(
                                Ingredient::getName, Collectors.counting()
                        ))
                        .entrySet().stream()
                        .map(e -> e.getKey() + (e.getValue() > 1 ? " x" + e.getValue(): ""))
                        .collect(Collectors.joining(", "));
                ingredientsLabel.setText("Ingredients: " + ingredients);
            }

            optionsButton.setOnAction(e -> {
                try {
                    if (CustomizeView.isOpen) return;
                    cart.getBasicProducts().remove(bp);
                    BasicProduct initialBp = basicProductDb.getBasicProductByIdFromDb(App.conn, bp.getId());
                    BigDecimal differentPrice = bp.getPrice().subtract(initialBp.getPrice());
                    bp.setPrice(bp.getPrice().subtract(differentPrice));
                    CustomizeView customizeView = new CustomizeView(dbpromo.connection(), bp);
                    App.split.getItems().set(0, customizeView);
                    refresh();
                    CustomizeView.isOpen = true;
                } catch (SQLException error) {
                    throw  new RuntimeException(error);
                }

            });

        } else if (item instanceof AdditionalProduct additionalProduct) {
            ap = additionalProduct;
            initialQuantity = ap.getQuantity();
            nameLabel.setText(ap.getName());
            priceLabel.setText(String.format("€ %.2f", ap.getPrice()));
            imageView.setImage(loadImageOrPlaceholder(ap.getImage()));

        }


        nameLabel.setFont(Font.font(15));
        priceLabel.setFont(Font.font(12));
        priceLabel.setOpacity(0.7);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);



        HBox itemBox = new HBox(10, imageView, nameLabel, priceLabel, spacer);


        //Makes so "Customize" button only shows in meal or BP
        if (item instanceof Meal || item instanceof  BasicProduct) {
            itemBox.getChildren().add(optionsButton);
        }

        itemBox.getChildren().add(removeButton);
        HBox.setMargin(removeButton, new Insets(0, 10, 0, 0));
        itemBox.setAlignment(Pos.CENTER_LEFT);

        VBox nitemBox = new VBox(2, itemBox);


        removeButton.setOnAction(e -> {
            if (item instanceof Meal meal) {
                cart.removeMeal(meal);
            } else if (item instanceof BasicProduct bp) {
                cart.removeBasicProduct(bp);
            } else if (item instanceof AdditionalProduct additionalProduct) {
                cart.removeAdditionalProduct(additionalProduct);
            }
            loadCartItems();
            updateTotal();
        });



        if (item instanceof AdditionalProduct) {
            Label quantityLabel = new Label("Quantity " + initialQuantity);
            final AdditionalProduct Ap = ap;
            increaseButton.setOnAction(e -> {
                Ap.setQuantity(Ap.getQuantity() + 1);
                quantityLabel.setText("Quantity " + Ap.getQuantity());
                updateTotal();
                loadCartItems();
            });
            decreaseButton.setOnAction(e -> {
                if (Ap.getQuantity() > 1) {
                    Ap.setQuantity(Ap.getQuantity() - 1);
                    quantityLabel.setText("Quantity " + cart.getItemQuantity(item));
                    updateTotal();
                    loadCartItems();
                }

            });

            HBox quantityBox = new HBox(4, quantityLabel, decreaseButton, increaseButton);
            quantityBox.setAlignment(Pos.CENTER_LEFT);
            nitemBox.getChildren().add(quantityBox);
        }

        List<String> options = itemOptionsMap.get(item);
        if (options != null && !options.isEmpty()) {
            Label optionsLabel = new Label("Options: " + String.join(", ", options));
            optionsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray; ");
            nitemBox.getChildren().add(optionsLabel);
        }

        if (!ingredientsLabel.getText().strip().equals("Ingredients:")) {
            ingredientsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: black; ");
            nitemBox.getChildren().add(ingredientsLabel);
        }

        if (!additionalLabel.getText().isEmpty()) {
            additionalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: black; ");
            nitemBox.getChildren().add(additionalLabel);
        }
        return nitemBox;
    }

    private void configureBottom() {
        updateTotal();

        Button clearCartButton = new Button("Clear Cart");
        // Apply the same button styling
        String buttonStyle =
                "-fx-background-color: linear-gradient(to bottom, #ff3b30, #dc1c13); " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-min-width: 80px;";



        // Style the clear cart button
        clearCartButton.setStyle(buttonStyle);




        clearCartButton.setOnAction(e -> clearCartItems());

        // Action of cancel all
        cancelButton.setOnAction(e -> cancelPurchasing());

        VBox bottomBox = new VBox(10,
                totalLabel,
                clearCartButton,
                checkoutButton,
                discountPrice,
                cancelButton
        );
        HBox cancelTopLeftBox = new HBox(cancelButton);
        cancelTopLeftBox.setAlignment(Pos.TOP_CENTER);
        cancelTopLeftBox.setPadding(new Insets(0, 0, 10, 10));
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        checkoutButton.setOnAction(e -> handleCheckout());
        getChildren().add(cancelTopLeftBox);
        getChildren().add(bottomBox);

    }

    private void updateTotal() {
        BigDecimal total = cart.getTotalPrice();
        totalLabel.setText(String.format("Total: € %.2f", total));

        totalLabel.setStyle("-fx-font-weight: bold;");


        boolean isCartEmpty = cart.getMeals().isEmpty()&&
                              cart.getBasicProducts().isEmpty() &&
                              cart.getAdditionalProducts().isEmpty();
        checkoutButton.setDisable(isCartEmpty);

    }

    private void handleCheckout() {
        if (cart.getDineType().isEmpty()) {
            // If a user did not choose dine type by some bugs
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Please select Eat Here or Take Away on the welcome screen first.");
            alert.setHeaderText(null);
            alert.show();
            delay.setOnFinished(ev2 -> alert.close());
            delay.play();
        }
        showReceipt();
    }

    private final RabatCodeDb rabatCodeDb = new RabatCodeDb();

    // Function for refreshing items and price in the cart
    public void refresh() {
        loadCartItems();
        updateTotal();
    }

    private void adjustQuantity(Object item, int delta) {
        if (item instanceof Meal meal) {
            if (delta > 0) {
                meal.setQuantity(meal.getQuantity() + delta);
            } else {
                int currentQuantity = meal.getQuantity();
                if (currentQuantity > 1) {
                    meal.setQuantity(currentQuantity - 1);
                }
            }
        }

        if (item instanceof BasicProduct bp) {
            if (delta > 0) {
                bp.setQuantity(bp.getQuantity() + delta);
            } else {
                int currentQuantity = bp.getQuantity();
                if (currentQuantity > 1) {
                    bp.setQuantity(currentQuantity - 1);
                }
            }
        }

        if (item instanceof AdditionalProduct ap) {
            if (delta > 0) {
                ap.setQuantity(ap.getQuantity() + delta);
            } else {
                int currentQuantity = ap.getQuantity();
                if (currentQuantity > 1) {
                    ap.setQuantity(currentQuantity - 1);
                }
            }
        }
        updateTotal();
        if (cart.isPromoApplied()) {
            cart.applyPromoCode();
        }
    }


    // Handles the discount button and applies the discount if the code is valid.
    private void handleDiscount() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Enter your promo code");
        dialog.setContentText("Promo code: ");
        Platform.runLater(() ->  {
            TextField dcField = dialog.getEditor();
            if (dcField != null) {
                dcField.requestFocus();
            }
        });

        dialog.showAndWait().ifPresent(code -> {
            double discountValue = 0.0;
            try (Connection conn = dbpromo.connection()) {
                RabatCode rabatCode = rabatCodeDb.discountDB(conn, code);
                if (rabatCode != null) {
                    discountValue = rabatCode.getRabat();
                }

            } catch (SQLException error) {
                error.printStackTrace();
            }

            if (discountValue > 0 && discountValue <= 100) {
                if (cart.countPrice().compareTo(BigDecimal.ZERO) == 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setContentText("You can't apply a discount on an empty cart!");
                    alert.showAndWait();
                    return;
                }
                cart.setPromoCode(discountValue);
                cart.applyPromoCode();
                updateTotal();
                Alert check = new Alert(Alert.AlertType.INFORMATION);
                check.setHeaderText(null);
                check.setContentText("Promo code applied successfully! " + (int) discountValue + " % off ");
                check.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(null);
                alert.setContentText("Invalid promo code! Please try again.");
                alert.showAndWait();

            }
        });
    }
    
    private void discountButton() {
        updateTotal();
        VBox bottomBox = new VBox(10, totalLabel, discountPrice, checkoutButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));
        discountPrice.setOnAction(e -> handleDiscount());
        checkoutButton.setOnAction(e -> handleCheckout());
        getChildren().add(bottomBox);
    }

    private void orderConformation() {

        Label confirmation = new Label("Thank you for your order!");
        confirmation.setFont(Font.font("Arial", 15));
        confirmation.setTextFill(Color.BLACK);
        VBox layout = new VBox(confirmation);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("""
                       -fx-background-color: white;
                       -fx-background-radius: 15;
                        -fx-border-radius: 15;
                        -fx-border-width: 1;
                        -fx-font-weight: bold;
                       """);

        Scene scene = new Scene(layout);
        Stage confirmationStage = new Stage();
        confirmationStage.setScene(scene);
        confirmationStage.setWidth(300);
        confirmationStage.setHeight(150);
        confirmationStage.centerOnScreen();

        layout.setOnMouseClicked(e -> confirmationStage.close());

        layout.setEffect(new DropShadow(10, Color.BLACK));
        // Wait 3 seconds and close automatically
        confirmationStage.show();
        // If I am not new a delay here, it won't close
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(ev2 -> confirmationStage.close());
        delay.play();

    }

    // Clean and back to welcome view
    private void cancelPurchasing() {
        cart.clearCart();
        cart.setDineType("");
        App.resetToWelcome();
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

}
