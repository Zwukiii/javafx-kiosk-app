package org.example.Ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import org.example.config.DatabaseInitializer;
import org.example.db.RabatCodeDb;
import org.example.domain.RabatCode;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class RabatCodeView extends BorderPane {

    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final RabatCodeDb rabatCodeDb = new RabatCodeDb();
    private final Connection connection = databaseInitializer.connection();

    private final TextField codeField = new TextField();
    private final TextField rabatField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 0);
    private final Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
    private final DatePicker endDatePicker = new DatePicker();
    private final Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 23);
    private final Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 59);
    private final Label messageLbl = new Label();

    private final TableView<RabatCode> table = new TableView<>();
    private final ObservableList<RabatCode> data = FXCollections.observableArrayList();

    public RabatCodeView() throws SQLException {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setCenter(buildContent());
        refreshTable();
    }

    private Label buildHeader() {
        Label header = new Label("Add new Rabat Code");
        header.getStyleClass().add("h2");
        return header;
    }

    private VBox buildContent() {
        VBox content = new VBox(15);
        content.getChildren().addAll(buildForm(), buildTable());
        return content;
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Code:"), 0, 0);
        form.add(codeField, 1, 0);

        form.add(new Label("Rabat (%):"), 0, 1);
        form.add(rabatField, 1, 1);

        // Start datetime controls
        form.add(new Label("Start Date:"), 0, 2);
        form.add(startDatePicker, 1, 2);
        HBox startTimeBox = new HBox(5, startHourSpinner, new Label(":"), startMinuteSpinner);
        form.add(new Label("Start Time (HH:mm):"), 0, 3);
        form.add(startTimeBox, 1, 3);

        // End datetime controls
        form.add(new Label("End Date:"), 0, 4);
        form.add(endDatePicker, 1, 4);
        HBox endTimeBox = new HBox(5, endHourSpinner, new Label(":"), endMinuteSpinner);
        form.add(new Label("End Time (HH:mm):"), 0, 5);
        form.add(endTimeBox, 1, 5);

        Button btnSave = new Button("Save");
        Button btnClear = new Button("Clear");
        HBox buttons = new HBox(10, btnSave, btnClear);
        form.add(buttons, 1, 6);

        form.add(messageLbl, 1, 7);
        messageLbl.setTextFill(Color.FIREBRICK);

        btnSave.setOnAction(e -> saveRabatCode());
        btnClear.setOnAction(e -> clearForm());

        return form;
    }

    private TableView<RabatCode> buildTable() {
        TableColumn<RabatCode, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<RabatCode, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(150);

        TableColumn<RabatCode, Double> rabatCol = new TableColumn<>("Rabat (%)");
        rabatCol.setCellValueFactory(new PropertyValueFactory<>("rabat"));
        rabatCol.setPrefWidth(100);

        TableColumn<RabatCode, LocalDateTime> startCol = new TableColumn<>("Start Time");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startCol.setPrefWidth(180);

        TableColumn<RabatCode, LocalDateTime> endCol = new TableColumn<>("End Time");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endCol.setPrefWidth(180);

        TableColumn<RabatCode, Void> actionCol = getActionColumn();

        table.getColumns().addAll(idCol, codeCol, rabatCol, startCol, endCol, actionCol);
        table.setItems(data);
        table.setPrefHeight(300);

        return table;
    }

    private TableColumn<RabatCode, Void> getActionColumn() {
        TableColumn<RabatCode, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setOnAction(e -> {
                    RabatCode code = getTableView().getItems().get(getIndex());
                    confirmAndDelete(code);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return actionCol;
    }

    private void saveRabatCode() {
        messageLbl.setText("");
        messageLbl.setTextFill(Color.FIREBRICK);

        String code = codeField.getText().trim();
        String rabatStr = rabatField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        int sh = startHourSpinner.getValue();
        int sm = startMinuteSpinner.getValue();
        int eh = endHourSpinner.getValue();
        int em = endMinuteSpinner.getValue();

        if (code.isEmpty() || rabatStr.isEmpty() || startDate == null || endDate == null) {
            messageLbl.setText("All fields are required.");
            return;
        }

        double rabat;
        LocalDateTime startDateTime, endDateTime;
        try {
            rabat = Double.parseDouble(rabatStr);
            startDateTime = LocalDateTime.of(startDate, LocalTime.of(sh, sm));
            endDateTime = LocalDateTime.of(endDate, LocalTime.of(eh, em));
        } catch (Exception e) {
            messageLbl.setText("Invalid input format.");
            return;
        }

        RabatCode rc = RabatCode.builder()
                .code(code)
                .rabat(rabat)
                .startTime(startDateTime)
                .endTime(endDateTime)
                .build();

        try {
            rabatCodeDb.createRabatCode(connection, rc);
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Code saved.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            messageLbl.setText("DB error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        codeField.clear();
        rabatField.clear();
        startDatePicker.setValue(null);
        startHourSpinner.getValueFactory().setValue(0);
        startMinuteSpinner.getValueFactory().setValue(0);
        endDatePicker.setValue(null);
        endHourSpinner.getValueFactory().setValue(23);
        endMinuteSpinner.getValueFactory().setValue(59);
    }

    private void refreshTable() {
        try {
            List<RabatCode> list = rabatCodeDb.getAllAvailableCodes(connection);
            data.setAll(list);
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("DB read error: " + ex.getMessage());
        }
    }

    private void confirmAndDelete(RabatCode rc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete Rabat Code");
        alert.setHeaderText("Are you sure you want to delete \"" + rc.getCode() + "\"?");
        alert.setContentText("This action cannot be undone.");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            deleteRabatCode(rc);
        }
    }

    private void deleteRabatCode(RabatCode rc) {
        try {
            rabatCodeDb.deleteRabatCodeById(connection, rc.getId());
            refreshTable();
            messageLbl.setTextFill(Color.GREEN);
            messageLbl.setText("Code \"" + rc.getCode() + "\" deleted.");
        } catch (SQLException ex) {
            messageLbl.setTextFill(Color.FIREBRICK);
            messageLbl.setText("Delete failed: " + ex.getMessage());
        }
    }
}
