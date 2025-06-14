package org.example.Ui;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.App;

import java.net.URL;


public class WelcomeView extends StackPane {

    public interface ChoiceHandler {
        void onChoice(String choice);
    }

    public WelcomeView(ChoiceHandler choice) {
        this("/images/Welcome.png", choice);
    }

    public WelcomeView(String imagePath, ChoiceHandler choice)  {

        // Set image as a background
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            ImageView imageView = new ImageView(new Image(url.toExternalForm(), 0, 0, true, true));
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.fitHeightProperty().bind(heightProperty());
            getChildren().add(imageView);
        } else {
            // Default background
            setStyle("-fx-background-color: #eeeeee;");
        }

        // Create 2 Buttons
        Button eatInButton = new Button("Eat Here");
        Button takeAwayButton = new Button("Take Away");

        // Set the appearance of buttons
        String btnCss = "-fx-font-size: 28px; "
                + "-fx-pref-width: 420px; "
                + "-fx-pref-height: 80px; "
                + "-fx-background-radius: 12px; "
                + "-fx-background-color: rgba(255,255,255,0.85);"
                + "-fx-text-fill: #000;";

        eatInButton.setStyle(btnCss);
        takeAwayButton.setStyle(btnCss);

        setupOpacityEffect(eatInButton);
        setupOpacityEffect(takeAwayButton);

        HBox box = new HBox(60, eatInButton, takeAwayButton);
        box.setAlignment(Pos.CENTER);

        getChildren().add(box);
        box.setTranslateY(300);
        setAlignment(Pos.CENTER);

        // Actions (Close self first and then call back)
        eatInButton.setOnAction(e -> {
            ((StackPane) getParent()).getChildren().remove(this);
            App.cart.setDineType("Eat Here");
            if (choice != null) choice.onChoice("Eat Here");
        });
        takeAwayButton.setOnAction(e -> {
            ((StackPane) getParent()).getChildren().remove(this);
            App.cart.setDineType("Take Away");
            if (choice != null) choice.onChoice("Take away");
        });
    }

    private void setupOpacityEffect(Button button) {
        button.setOpacity(0.9);

        button.setOnMouseEntered(e -> button.setOpacity(1));
        button.setOnMouseExited(e -> button.setOpacity(0.9));
    }
}
