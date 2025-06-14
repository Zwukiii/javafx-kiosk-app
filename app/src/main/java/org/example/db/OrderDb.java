package org.example.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.Order;
import org.example.domain.OrderedProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDb {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void createOrder(Connection connection, Order order) throws SQLException {
        final String INSERT_QUERY =
                "INSERT INTO orders (" +
                        "order_id, total_price, promo_code, created_at, dine_type, ordered_products_summary" +
                        ") VALUES (?, ?, ?, ?, ?, ?::jsonb)";

        try (PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY)) {
            stmt.setString(1, order.getOrderId());
            stmt.setBigDecimal(2, order.getTotalPrice());
            stmt.setDouble(3, order.getPromoCode());
            stmt.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
            stmt.setString(5, order.getDineType());

            String productsJson;
            try {
                productsJson = MAPPER.writeValueAsString(order.getOrderedProducts());
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to serialize orderedProducts to JSON", e);
            }
            stmt.setString(6, productsJson);

            stmt.executeUpdate();
        }
    }

    public List<Order> getAllOrders(Connection connection) throws SQLException {
        final String SELECT_QUERY =
                "SELECT id, order_id, total_price, promo_code, created_at, dine_type, ordered_products_summary " +
                        "FROM orders";

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_QUERY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String productsJson = rs.getString("ordered_products_summary");
                List<OrderedProduct> productsList = new ArrayList<>();
                if (productsJson != null && !productsJson.isEmpty()) {
                    try {
                        productsList = MAPPER.readValue(
                                productsJson,
                                new TypeReference<List<OrderedProduct>>() {
                                }
                        );
                    } catch (JsonProcessingException e) {
                        throw new SQLException("Failed to deserialize orderedProducts from JSON", e);
                    }
                }

                Order order = Order.builder()
                        .id(rs.getLong("id"))
                        .orderId(rs.getString("order_id"))
                        .totalPrice(rs.getBigDecimal("total_price"))
                        .promoCode(rs.getDouble("promo_code"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .dineType(rs.getString("dine_type"))
                        .orderedProducts(productsList)
                        .build();

                orders.add(order);
            }
        }

        return orders;
    }

    public void deleteOrderById(Connection connection, Long orderId) throws SQLException {
        final String DELETE_QUERY = "DELETE FROM orders WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(DELETE_QUERY)) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }
}