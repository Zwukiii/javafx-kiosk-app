package org.example.Ui.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.example.config.DatabaseInitializer;
import org.example.db.OrderDb;
import org.example.domain.Order;
import org.example.domain.OrderedProduct;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderSummaryView extends BorderPane {
    private final DatabaseInitializer dbInit = new DatabaseInitializer();
    private final Connection conn = dbInit.connection();
    private final OrderDb orderDb = new OrderDb();

    private final ObservableList<Order> masterData = FXCollections.observableArrayList();
    private final FilteredList<Order> filteredData = new FilteredList<>(masterData, o -> true);

    private final TableView<Order> table = new TableView<>(filteredData);
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final Button filterButton = new Button("Apply");
    private final Label profitLabel = new Label("Profit: 0.00 €");
    private final VBox detailsPane = new VBox(8);

    public OrderSummaryView() throws SQLException {
        setPadding(new Insets(20));

        setTop(buildHeader());
        setCenter(buildTable());
        setRight(buildDetailsPane());
        setBottom(buildFooter());

        loadData();
        configureSelectionListener();
        configureFilterAction();
    }

    private Node buildHeader() {
        Label title = new Label("Orders History");
        title.getStyleClass().add("h2");

        HBox filterBox = new HBox(10,
                new Label("From:"), fromDatePicker,
                new Label("To:"), toDatePicker,
                filterButton
        );
        filterBox.setPadding(new Insets(10, 0, 10, 0));

        VBox header = new VBox(5, title, filterBox);
        return header;
    }

    private Node buildTable() {
        TableColumn<Order, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order No.");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setPrefWidth(120);

        TableColumn<Order, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(cellData -> {
            String formatted = cellData.getValue().getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        dateCol.setPrefWidth(140);
        dateCol.setSortType(TableColumn.SortType.ASCENDING);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Price");
        totalCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTotalPrice().doubleValue())
        );
        totalCol.setPrefWidth(100);

        TableColumn<Order, Double> promoCol = new TableColumn<>("Promo Code");
        promoCol.setCellValueFactory(new PropertyValueFactory<>("promoCode"));
        promoCol.setPrefWidth(100);

        TableColumn<Order, String> dineTypeCol = new TableColumn<>("Dine Type");
        dineTypeCol.setCellValueFactory(new PropertyValueFactory<>("dineType"));
        dineTypeCol.setPrefWidth(100);

        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    confirmAndDelete(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        table.getColumns().setAll(idCol, orderIdCol, dateCol, totalCol, promoCol, dineTypeCol, actionCol);
        table.getSortOrder().add(dateCol);
        table.setPrefHeight(350);
        return table;
    }

    private Node buildDetailsPane() {
        detailsPane.setPadding(new Insets(0, 0, 0, 20));
        detailsPane.getChildren().add(new Label("Select an order to see details"));
        return detailsPane;
    }

    private Node buildFooter() {
        HBox box = new HBox();
        box.setPadding(new Insets(10, 0, 0, 0));
        box.getChildren().add(profitLabel);
        return box;
    }

    private void loadData() throws SQLException {
        masterData.setAll(orderDb.getAllOrders(conn));
        updateProfit();
    }

    private void configureSelectionListener() {
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            detailsPane.getChildren().clear();
            if (sel != null) {
                detailsPane.getChildren().addAll(
                        new Label("Order ID: " + sel.getOrderId()),
                        new Label("Created:  " + sel.getCreatedAt()),
                        new Label("Total:    " + sel.getTotalPrice()),
                        new Label("Promo:    " + sel.getPromoCode()),
                        new Label("Dine Type:" + sel.getDineType()),
                        new Separator(),
                        new Label("Items:")
                );


                VBox itemsBox = new VBox(4);
                for (OrderedProduct op : sel.getOrderedProducts()) {
                    String line = String.format(
                            "%s: %s (%.2f €) —  x: %d",
                            op.getType(),
                            op.getName(),
                            op.getPrice().doubleValue(),
                            op.getQuantity()
                    );
                    itemsBox.getChildren().add(new Label(line));
                }
                detailsPane.getChildren().add(itemsBox);

            } else {
                detailsPane.getChildren().add(new Label("Select an order to see details"));
            }
        });
    }

    private void configureFilterAction() {
        filterButton.setOnAction(e -> {
            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();
            filteredData.setPredicate(order -> {
                var date = order.getCreatedAt().toLocalDate();
                if (from != null && date.isBefore(from)) return false;
                if (to != null && date.isAfter(to)) return false;
                return true;
            });
            updateProfit();
        });
    }

    private void confirmAndDelete(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Delete Order");
        alert.setHeaderText("Delete order " + order.getOrderId() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                orderDb.deleteOrderById(conn, order.getId());
                masterData.remove(order);
                updateProfit();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete: " + ex.getMessage())
                        .showAndWait();
            }
        }
    }

    private void updateProfit() {
        double profit = filteredData.stream()
                .mapToDouble(o -> o.getTotalPrice().doubleValue() - o.getPromoCode())
                .sum();
        profitLabel.setText(String.format("Profit: %.2f €", profit));
    }
}