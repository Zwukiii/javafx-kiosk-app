package org.example.Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.domain.AdditionalProduct;
import org.example.domain.Meal;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Objects;

public class MealItemView extends HBox {


    private static final Path IMAGE_BASE = Paths.get(System.getProperty("user.dir"), "image");

    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(
                    MealItemView.class.getResource("/images/placeholder.jpeg")
            ).toExternalForm(), true
    );

    private final Connection connection;

    public MealItemView(Connection connection, Meal meal) {
        this.connection = connection;
        setupMeal(meal);
    }

    private ImageView setupImageView(String imagePath) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        Image image;
        Path localPath = IMAGE_BASE.resolve(imagePath);
        if (Files.exists(localPath)) {
            image = new Image(localPath.toUri().toString(), true);
        } else {
            try {
                URL url = getClass().getResource("/images/" + imagePath);
                if (url != null) {
                    image = new Image(url.toExternalForm(), true);
                } else {
                    image = PLACEHOLDER;
                }
            } catch (Exception e) {
                image = PLACEHOLDER;
            }
        }

        imageView.setImage(image);
        return imageView;
    }

    private void setupMeal(Meal meal) {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;");
        setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = setupImageView(meal.getImage());

        // Main info box
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPrefWidth(250);

        Label nameLabel = new Label(meal.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("€%.2f", meal.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        // Details box for basic product and additional products
        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPrefWidth(200);

        // Basic Product info
        if (meal.getBasicProduct() != null) {
            Label bpLabel = new Label(meal.getBasicProduct().getName());
            bpLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            bpLabel.setWrapText(true);
            detailsBox.getChildren().add(bpLabel);
        }

        // Additional Products info
        if (meal.getAdditionalProducts() != null && !meal.getAdditionalProducts().isEmpty()) {
            StringBuilder addProductsText = new StringBuilder();
            for (AdditionalProduct ap : meal.getAdditionalProducts()) {
                addProductsText.append("- ").append(ap.getName()).append("\n");
            }
            Label apLabel = new Label(addProductsText.toString());
            apLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            apLabel.setWrapText(true);
            detailsBox.getChildren().add(apLabel);
        }

        // Button box
        VBox buttonBox = new VBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button customizeButton = new Button(" Customize ");
        customizeButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        Region space = new Region();
        space.setPrefHeight(10);

        buttonBox.getChildren().addAll(addToCartButton, space, customizeButton);

        customizeButton.setOnAction(e -> {
            try {
                CustomizeView customizeView = new CustomizeView(connection, meal);
                CustomizeView.isOpen = true;
                if (App.split.getItems().size() > 1) {
                    App.split.getItems().set(0, customizeView);
                } else {
                    App.split.getItems().setAll(customizeView, App.cartView);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        addToCartButton.setOnAction(e -> {
            App.cart.addMeal(meal);
            CartView cartView = new CartView(App.cart);

            if (App.split.getItems().size() > 1) {
                App.split.getItems().set(1, cartView);
            } else {
                Node currentView = App.split.getItems().get(0);
                App.split.getItems().setAll(currentView, cartView);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        getChildren().addAll(imageView, infoBox, detailsBox, spacer, buttonBox);
    }
}