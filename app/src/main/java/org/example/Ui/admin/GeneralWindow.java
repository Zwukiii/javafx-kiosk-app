package org.example.Ui.admin;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;

public class GeneralWindow extends Stage {

    private static final String CORRECT_PIN = "0000";

    public GeneralWindow() {
        this.setTitle("PIN Verification");

        // Input field for PIN
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("Enter PIN");

        Button submitButton = new Button("Submit");
        Label messageLabel = new Label();

        submitButton.setOnAction(e -> {
            String enteredPin = pinField.getText();
            if (CORRECT_PIN.equals(enteredPin)) {
                openAdminPanel();
            } else {
                messageLabel.setText("Incorrect PIN. Access denied.");
                this.close();
            }
        });

        VBox pinLayout = new VBox(10, new Label("Enter administrator PIN:"), pinField, submitButton, messageLabel);
        pinLayout.setPadding(new Insets(20));
        Scene pinScene = new Scene(pinLayout, 300, 150);

        this.setScene(pinScene);
        this.initModality(Modality.APPLICATION_MODAL);
        this.show();
        this.centerOnScreen();
    }

    private void openAdminPanel() {
        this.setTitle("Admin Panel");


        VBox buttonBox = new VBox(12);
        buttonBox.setPadding(new Insets(20));

        Button btnAddProduct        = new Button("Add new Menu Product and delete");
        Button btnComposeMeal       = new Button("Compose new Meal and delete");
        Button btnAdditionalProduct      = new Button("Add new Menu Additional Product and delete");
        Button btnAddIngredient     = new Button("Add new Ingredient and delete");

        Button btnUpdateProduct     = new Button("Update in Menu Basic Product");
        Button btnUpdateAdditionalProduct    = new Button("Update in Menu additional product");
        Button btnUpdateMeal        = new Button("Update in menu Meals");
        Button btnUpdateIngredient  = new Button("Update Ingredients");

        Button btnOrderHistory      = new Button("View Order History");
        Button btnRabatCode         = new Button("Create new rabat code or delete");

        buttonBox.getChildren().addAll(
                btnAddProduct,
                btnComposeMeal,
                btnAdditionalProduct,
                btnAddIngredient,
                new Separator(),
                btnUpdateProduct,
                btnUpdateAdditionalProduct,
                btnUpdateMeal,
                btnUpdateIngredient,
                new Separator(),
                btnOrderHistory,
                btnRabatCode
        );

        StackPane mainArea = new StackPane();
        mainArea.setPadding(new Insets(20));


        SplitPane adminContent = new SplitPane(buttonBox, mainArea);
        adminContent.setDividerPositions(0.25);

        this.setScene(new Scene(adminContent, 1800, 1000));
        this.centerOnScreen();

        //ADD  BASIC PRODUCT
        btnAddProduct.setOnAction(e       -> {
            try {
                mainArea.getChildren()
                        .setAll(new AddBasicProductView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        //ADD MEAL
        btnComposeMeal.setOnAction(e      -> {
            try {
                mainArea.getChildren()
                        .setAll(new AddMealView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        //ADD Additional product
        btnAdditionalProduct.setOnAction(e      -> {
            try {
                mainArea.getChildren()
                        .setAll(new AddAdditionalProductView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        //ADD Ingredient
        btnAddIngredient.setOnAction(e    -> {
            try {
                mainArea.getChildren()
                        .setAll(new AddIngredientView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Update basic Product
        btnUpdateProduct.setOnAction(e    -> {
            try {
                mainArea.getChildren()
                        .setAll(new UpdateBasicProductView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Update Additional Product
        btnUpdateAdditionalProduct.setOnAction(e    -> {
            try {
                mainArea.getChildren()
                        .setAll(new UpdateAdditionalProductView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        // update Meal
        btnUpdateMeal.setOnAction(e       -> {
            try {
                mainArea.getChildren()
                        .setAll(new UpdateMealView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        // UpdateIngredient
        btnUpdateIngredient.setOnAction(e -> {
            try {
                mainArea.getChildren()
                        .setAll(new UpdateIngredientView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });


        // Order summary
        btnOrderHistory.setOnAction(e     -> {
            try {
                mainArea.getChildren()
                        .setAll(new OrderSummaryView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });


        // Rabat code
        btnRabatCode.setOnAction(e     -> {
            try {
                mainArea.getChildren()
                        .setAll(new RabatCodeView());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }



}
