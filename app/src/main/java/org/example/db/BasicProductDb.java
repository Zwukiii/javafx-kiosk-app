package org.example.db;

import lombok.AllArgsConstructor;
import org.example.domain.BasicProduct;
import org.example.domain.Ingredient;
import org.example.mappers.BasicProductMapper;
import org.example.mappers.IngredientMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

@AllArgsConstructor
public class BasicProductDb {
    private final BasicProductMapper basicProductMapper = new BasicProductMapper();
    private final IngredientMapper ingredientMapper = new IngredientMapper();


    public BasicProduct getBasicProductByIdFromDb(Connection connection, long id) throws SQLException {
        String query = "SELECT * FROM basic_product WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                BasicProduct basicProduct = basicProductMapper.mapSqlResponseToBasicProduct(rs);
                basicProduct.setIngredients(getIngredientsForProduct(connection, id));
                return basicProduct;
            }
        }
    }


    public List<BasicProduct> getAllBasicProducts(Connection connection) throws SQLException {
        final String PRODUCT_QUERY = "SELECT * FROM basic_product";
        List<BasicProduct> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(PRODUCT_QUERY);
             ResultSet rs = stmt.executeQuery()) {

            BasicProduct product;
            while ((product = basicProductMapper.mapSqlResponseToBasicProduct(rs)) != null) {

                product.setIngredients(getIngredientsForProduct(connection, product.getId()));
                products.add(product);
            }
        }
        return products;
    }


    public List<Ingredient> getIngredientsForProduct(Connection connection, long productId) throws SQLException {
        final String ING_QUERY =
                "SELECT i.id, i.name, i.image, i.price " +
                        "FROM basic_product_ingredient bpi " +
                        "JOIN ingredient i ON i.id = bpi.ingredient_id " +
                        "WHERE bpi.basic_product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(ING_QUERY)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return ingredientMapper.mapSqlResponseToListOfIngredients(rs);
            }
        }
    }


    public BasicProduct createNewBasicProductInDb(Connection connection, BasicProduct basicProduct) throws SQLException {
        final String INSERT_PRODUCT_SQL =
                "INSERT INTO basic_product (name, price, image, type, size) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(
                INSERT_PRODUCT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, basicProduct.getName());
            stmt.setBigDecimal(2, basicProduct.getPrice());
            stmt.setString(3, basicProduct.getImage());
            stmt.setString(4, basicProduct.getType().name());
            stmt.setString(5, basicProduct.getSize().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating BasicProduct failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    basicProduct.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating BasicProduct failed, no ID obtained.");
                }
            }
        }

        List<Ingredient> ingredients = basicProduct.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            final String INSERT_BPI_SQL =
                    "INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_BPI_SQL)) {
                for (Ingredient ing : ingredients) {
                    stmt.setLong(1, basicProduct.getId());
                    stmt.setLong(2, ing.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        return basicProduct;
    }


    public void deleteBasicProductById(Connection connection, long id) throws SQLException {
        final String DEL = "DELETE FROM basic_product WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(DEL)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }


    public BasicProduct updateBasicProductInDb(Connection connection, BasicProduct basicProduct) throws SQLException {
        // 1) Update the basic_product row
        final String UPDATE_PRODUCT_SQL =
                "UPDATE basic_product " +
                        "SET name = ?, price = ?, image = ?, type = ?, size = ? " +
                        "WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PRODUCT_SQL)) {
            stmt.setString(1, basicProduct.getName());
            stmt.setBigDecimal(2, basicProduct.getPrice());
            stmt.setString(3, basicProduct.getImage());
            stmt.setString(4, basicProduct.getType().name());
            stmt.setString(5, basicProduct.getSize().name());
            stmt.setLong(6, basicProduct.getId());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Updating BasicProduct failed: no rows affected.");
            }
        }

        // 2) Remove all existing ingredient links
        final String DELETE_LINKS_SQL =
                "DELETE FROM basic_product_ingredient WHERE basic_product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_LINKS_SQL)) {
            stmt.setLong(1, basicProduct.getId());
            stmt.executeUpdate();
        }

        // 3) Re‐insert links for current ingredient list
        List<Ingredient> ingredients = basicProduct.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            final String INSERT_LINK_SQL =
                    "INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_LINK_SQL)) {
                for (Ingredient ing : ingredients) {
                    stmt.setLong(1, basicProduct.getId());
                    stmt.setLong(2, ing.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        return basicProduct;
    }
}