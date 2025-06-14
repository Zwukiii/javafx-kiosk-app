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
import org.example.domain.BasicProduct;
import org.example.domain.Ingredient;
import org.example.enumeration.Type;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Objects;

public class ProductItemView extends HBox {

    private static final Path IMAGE_BASE = Paths.get(System.getProperty("user.dir"), "image");

    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(
                    ProductItemView.class.getResource("/images/placeholder.jpeg")
            ).toExternalForm(), true
    );

    private final Connection connection;

    public ProductItemView(Connection connection, BasicProduct product) {
        this.connection = connection;
        setupBasicProduct(product);
    }

    public ProductItemView(Connection connection, AdditionalProduct product) {
        this.connection = connection;
        setupAdditionalProduct(product);
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

    private void setupBasicProduct(BasicProduct product) {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;");
        setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = setupImageView(product.getImage());

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPrefWidth(250);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");


        Label priceLabel = new Label(String.format("€%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        VBox buttonBox = new VBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        buttonBox.getChildren().add(addToCartButton);

        if (product.getType() == Type.PIZZA || product.getType() == Type.APPETIZER) {
            VBox ingredientsBox = new VBox(5);
            ingredientsBox.setAlignment(Pos.CENTER_LEFT);
            ingredientsBox.setPrefWidth(200);

            Label ingredientsLabel = new Label(product.getIngredientInfo());
            ingredientsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            ingredientsLabel.setWrapText(true);

            ingredientsBox.getChildren().add(ingredientsLabel);

            Region space = new Region();
            space.setPrefHeight(10);
            buttonBox.getChildren().add(space);

            Button customizeButton = new Button(" Customize ");
            customizeButton.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");
            
            buttonBox.getChildren().add(customizeButton);

            customizeButton.setOnAction(e -> {
                try {
                    CustomizeView customizeView = new CustomizeView(connection, product);
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

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(imageView, infoBox, ingredientsBox, spacer, buttonBox);
        } else {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(imageView, infoBox, spacer, buttonBox);
        }

        addToCartButton.setOnAction(e -> {
            App.cart.addBasicProduct(product);
            CartView cartView = new CartView(App.cart);
            Node currentView = App.split.getItems().get(0);
            App.split.getItems().setAll(currentView, cartView);
        });
    }

    private void setupAdditionalProduct(AdditionalProduct product) {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;");
        setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = setupImageView(product.getImage());

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPrefWidth(250);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");


        Label priceLabel = new Label(String.format("€%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        infoBox.getChildren().addAll(nameLabel, priceLabel);

        VBox buttonBox = new VBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 4px;");

        buttonBox.getChildren().add(addToCartButton);

        addToCartButton.setOnAction(e -> {
            App.cart.addAdditionalProduct(product);
            CartView cartView = new CartView(App.cart);
            Node currentView = App.split.getItems().get(0);
            App.split.getItems().setAll(currentView, cartView);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(imageView, infoBox, spacer, buttonBox);
    }
}