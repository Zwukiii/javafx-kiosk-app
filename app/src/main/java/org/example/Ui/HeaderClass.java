package org.example.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class HeaderClass {
    public static HBox HeaderBox(){
        BrandIdentity logo = new BrandIdentity(86);


        HBox header = new HBox(logo);
        header.setAlignment(Pos.CENTER);
        header.setSpacing(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #ff3b30, #dc1c13);");
        header.setPrefHeight(100);
        header.setMaxWidth(Double.MAX_VALUE);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        header.setEffect(shadow);

        header.setPrefHeight(100);
        header.setPrefWidth(Double.MAX_VALUE);
        return header;
    }

}