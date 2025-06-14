package org.example.Ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.App;


public final class Timer {
    // Singleton
    private static final Timer timer = new Timer();
    public static Timer getInstance() { return timer; }
    private Timer() { configureCoreTimer(); }

    private final PauseTransition coreTimer = new PauseTransition(Duration.seconds(30));
    private Timeline warningCountdown;
    private Stage warningDialogue;
    private final IntegerProperty secondsLeft = new SimpleIntegerProperty(10);
    private boolean running = false;


    // Call once in every view root so that any user input resets the timer
    public void registerNode(Node root) {
        root.addEventFilter(InputEvent.ANY, e -> reset());
    }

    // Resume counting
    public void resume() {
        running = true;
        reset();
    }

    // Stop counting completely
    public void pause() {
        running = false;
        coreTimer.stop();
        closeWarning();
    }

    // Manually invoke
    public void reset() {
        if (running) coreTimer.playFromStart();
    }

    private void configureCoreTimer() {
        coreTimer.setOnFinished(e -> Platform.runLater(this::showWarningDialogue));
    }

    private void showWarningDialogue() {
        if (!running) return; // remain in welcome view

        // If its already shown, make sure there is no repetitive dialog window
        if (warningDialogue != null && warningDialogue.isShowing()) return;

        secondsLeft.set(10);
        Label msg = new Label();
        msg.textProperty().bind(secondsLeft.asString(
                "No activity detected. Returning to Welcome Page in %d s"));
        Button cont = new Button("Continue");
        cont.setOnAction(e -> closeWarning());

        VBox box = new VBox(10, msg, cont);
        box.setPrefSize(400, 150);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        warningDialogue = new Stage(StageStyle.DECORATED);
        warningDialogue.initModality(Modality.WINDOW_MODAL);

        // Try to centre on whatever window is active
        Window activeWindow = Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
        if (activeWindow != null) {
            warningDialogue.initOwner(activeWindow);
        }
        warningDialogue.setScene(new Scene(box));
        warningDialogue.show();

        warningCountdown = new Timeline(
                new KeyFrame(Duration.seconds(1), ev -> {
                    int s = secondsLeft.get() - 1;
                    secondsLeft.set(s);
                    if (s <= 0) {
                        pause();
                        closeWarning();
                        Platform.runLater(App::resetToWelcome);
                    }
                })
        );
        warningCountdown.setCycleCount(Animation.INDEFINITE);
        warningCountdown.play();
    }

    private void closeWarning() {
        if (warningCountdown != null) warningCountdown.stop();
        if (warningDialogue != null) warningDialogue.close();
        warningCountdown = null;
        warningDialogue = null;
        reset();
    }
}
