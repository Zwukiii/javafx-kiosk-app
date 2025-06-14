package org.example.Ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.db.AdditionalProductDb;
import org.example.domain.AdditionalProduct;
import org.example.domain.BasicProduct;
import org.example.domain.Meal;
import org.example.enumeration.Type;
import org.example.mappers.AdditionalProductMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CategoryMenuView extends VBox {
    private final HBox buttonBox;
    private final StackPane contentArea;
    private final Connection connection;
    private Button selectedButton = null;

    public CategoryMenuView(Connection connection, List<BasicProduct> products, List<Meal> meals) throws SQLException {
        this.connection = connection;

        setPrefHeight(800);
        setStyle("-fx-background-color: white;");
        setPadding(new Insets(20));

        AdditionalProductDb additionalProductDb = new AdditionalProductDb(new AdditionalProductMapper());
        List<AdditionalProduct> additionalProducts = additionalProductDb.getAllAdditionalProducts(connection);

        buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(60, 0, 25, 0));
        buttonBox.setMinHeight(120);
        buttonBox.setMaxHeight(120);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(50, 0, 0, 0));

        createCategoryButtons(products, additionalProducts, meals);

        getChildren().addAll(buttonBox, contentArea);
    }

    private void createCategoryButtons(List<BasicProduct> products, List<AdditionalProduct> additionalProducts, List<Meal> meals) {
        createCategoryButton("Pizza", createTypeContent(products, Type.PIZZA));
        createCategoryButton("Appetizer", createTypeContent(products, Type.APPETIZER));
        createCategoryButton("Drinks", createContentForAdditional(additionalProducts, Type.DRINK));
        createCategoryButton("Fries", createContentForAdditional(additionalProducts, Type.FRIES));
        createCategoryButton("Meals", createMealContent(meals));

        if (!buttonBox.getChildren().isEmpty()) {
            Button firstButton = (Button) ((StackPane) buttonBox.getChildren().getFirst()).getChildren().get(1);
            firstButton.fire();
            selectedButton = firstButton;
            firstButton.setStyle(getSelectedStyle());
        }
    }

    private void createCategoryButton(String text, Node content) {
        Button button = new Button();
        button.setStyle(getNormalStyle());
        button.setOpacity(0.5);
        button.setBackground(Background.EMPTY);
        button.setBorder(Border.EMPTY);

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");
        textLabel.setOpacity(1.0);
        StackPane.setAlignment(textLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(textLabel, new Insets(0, 0, -15, 0));

        button.setOnMouseEntered(e -> {
            if (button != selectedButton) {
                button.setOpacity(0.25);
            }
        });

        button.setOnMouseExited(e -> {
            if (button != selectedButton) {
                button.setOpacity(0.5);
            }
        });

        button.setOnAction(e -> {
            if (selectedButton != null) {
                selectedButton.setStyle(getNormalStyle());
                selectedButton.setOpacity(0.5);
            }
            button.setStyle(getSelectedStyle());
            button.setOpacity(0.0);
            selectedButton = button;

            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        });

        String imagePath = "/images/margarita.jpg";
        switch (text.toLowerCase()) {
            case "pizza": imagePath = "/images/pizzas.png"; break;
            case "appetizer": imagePath = "/images/appetizers.png"; break;
            case "drinks": imagePath = "/images/drinks.jpg"; break;
            case "fries": imagePath = "/images/fries.jpg"; break;
            case "meals": imagePath = "/images/meals.jpg"; break;
        }

        ImageView bg = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        bg.setFitWidth(180);
        bg.setFitHeight(110);
        bg.setPreserveRatio(true);

        StackPane stack = new StackPane(bg, button, textLabel);
        stack.setPrefSize(180, 120);
        buttonBox.getChildren().add(stack);
    }

    private ScrollPane createTypeContent(List<BasicProduct> products, Type type) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white;");

        boolean hasProducts = false;
        for (BasicProduct product : products) {
            if (product.getType() == type) {
                content.getChildren().add(new ProductItemView(connection, product));
                hasProducts = true;
            }
        }

        if (!hasProducts) {
            addEmptyLabel(content);
        }

        return wrapInScrollPane(content);
    }

    private ScrollPane createContentForAdditional(List<AdditionalProduct> products, Type type) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white;");

        boolean hasProducts = false;
        for (AdditionalProduct product : products) {
            if (product.getType() == type) {
                content.getChildren().add(new ProductItemView(connection, product));
                hasProducts = true;
            }
        }

        if (!hasProducts) {
            addEmptyLabel(content);
        }

        return wrapInScrollPane(content);
    }

    private ScrollPane createMealContent(List<Meal> meals) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white;");

        if (meals != null && !meals.isEmpty()) {
            for (Meal meal : meals) {
                content.getChildren().add(new MealItemView(connection, meal));
            }
        } else {
            addEmptyLabel(content);
        }

        return wrapInScrollPane(content);
    }

    private String getNormalStyle() {
        return "-fx-background-color: rgba(255,255,255,1);" +
                "-fx-text-fill: black; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-min-width: 180; " +
                "-fx-min-height: 120; " +
                "-fx-max-width: 180; " +
                "-fx-max-height: 120;" +
                "-fx-padding: 80 0 0 0;";
    }

    private String getHoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.75);" +
                "-fx-text-fill: black; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-min-width: 180; " +
                "-fx-min-height: 120; " +
                "-fx-max-width: 180; " +
                "-fx-max-height: 120;" +
                "-fx-padding: 80 0 0 0;";
    }

    private String getSelectedStyle() {
        return getHoverStyle();
    }

    private void addEmptyLabel(VBox content) {
        Label emptyLabel = new Label("No products available");
        emptyLabel.setStyle("-fx-font-size: 14px;");
        content.getChildren().add(emptyLabel);
        content.setAlignment(Pos.CENTER);
    }

    private ScrollPane wrapInScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        return scrollPane;
    }
}
