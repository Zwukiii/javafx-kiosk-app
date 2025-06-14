package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Ui.*;
import org.example.Ui.admin.GeneralWindow;
import org.example.config.DatabaseInitializer;
import org.example.domain.AdditionalProduct;
import org.example.domain.Cart;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class App extends Application {
    public static MenuView menuView;
    public static CartView cartView;
    public static SplitPane split;
    public static String choice;
    private WelcomeView welcomeView;
    public static Cart cart = new Cart();
    public static List<AdditionalProduct> menuProducts = new ArrayList<>();
    public static StackPane cover;
    public static Connection conn;

    @Override
    public void start(Stage stage) throws SQLException {

        DatabaseInitializer dbInit = new DatabaseInitializer();
        dbInit.initialize();
        conn = dbInit.connection();

        cartView = new CartView(cart);
        menuView = new MenuView(conn);
        cartView = new CartView(new Cart());

        split = new SplitPane(menuView, cartView);
        Platform.runLater(() -> App.split.setDividerPositions(0.6));

        HeaderClass headerClass = new HeaderClass();
        VBox rootLayout = new VBox(headerClass.HeaderBox(), split);
        VBox.setVgrow(split, javafx.scene.layout.Priority.ALWAYS);

        cover = new StackPane(rootLayout);
        Timer.getInstance().pause();

        //Scene scene = new Scene(rootLayout, 1980, 1080);

//        Runnable rebuildViews = () -> {
//            MenuView menuView = null;
//            try {
//                menuView = new MenuView(conn);
//                cartView = new CartView(cart);
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//            CartView cartView = new CartView(new Cart());
//           split.getItems().setAll(menuView, cartView);
//        };
//        rebuildViews.run();

        rebuildViews();

        welcomeView = new WelcomeView(choice -> {
            this.choice = choice;
            cover.getChildren().remove(welcomeView);
            Timer.getInstance().resume();
        });
        cover.getChildren().add(welcomeView);

        Scene scene = new Scene(cover, 1980, 1080);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                Timer.getInstance().pause();
                GeneralWindow generalWindow = new GeneralWindow();
                generalWindow.setOnHidden(e -> {
                    rebuildViews();
                    Timer.getInstance().resume();
                });
                generalWindow.show();
            }
        });

        stage.setScene(scene);
        stage.setTitle("McKiosk Demo");
        stage.show();

    }

    // Wrote by Daniel, move to here
    public static void rebuildViews() {
        MenuView menuView = null;
        try {
            menuView = new MenuView(conn);
            cartView = new CartView(cart);
            split.getItems().setAll(menuView, cartView);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetToWelcome() {
        Timer.getInstance().pause();
        cart.clearCart();
        cart.setDineType("");
        rebuildViews();
        cover.getChildren().add(new WelcomeView(choice -> {
            cover.getChildren().removeIf(n -> n instanceof WelcomeView);
            Timer.getInstance().resume();
        }));
    }

    // Replace a new CartView and clean the old one
    public static void replaceCartView(CartView newView) {
        cartView = newView;
        split.getItems().set(1, newView);
    }
}


