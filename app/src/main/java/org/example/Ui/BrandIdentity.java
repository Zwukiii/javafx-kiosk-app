package org.example.Ui;
import java.util.Objects;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Getter;


@Getter
public class BrandIdentity  extends Pane {
    public static final Image LOGO;

    public static final Image PLACEHOLDER;

    static {
        LOGO = new Image(
                Objects.requireNonNull(Objects.requireNonNull(BrandIdentity.class.getResource("/images/logo.png")).toExternalForm()),
                true
        );
        PLACEHOLDER = new Image(
                Objects.requireNonNull(Objects.requireNonNull(BrandIdentity.class.getResource("/images/logo.png")).toExternalForm()),
                true
        );
    }


    private final ImageView displayLogo;


    public BrandIdentity(double size) {
        displayLogo = new ImageView(LOGO);

        displayLogo.setFitWidth(size);
        displayLogo.setFitHeight(size);
        displayLogo.setPreserveRatio(true);

        // makes the logo into a circle.
        Circle clip = new Circle(size / 2, size / 2, size / 2 );
        displayLogo.setClip(clip);

        // drop shadow around the logo.
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(4);
        dropShadow.setColor(Color.rgb(0,0,0,0.15));
        StackPane logoContainer = new StackPane(displayLogo);
        logoContainer.setEffect(dropShadow);
        getChildren().add(logoContainer);


    }
}
