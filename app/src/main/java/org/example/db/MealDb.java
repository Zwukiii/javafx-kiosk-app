package org.example.db;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.domain.AdditionalProduct;
import org.example.domain.BasicProduct;
import org.example.domain.Meal;
import org.example.mappers.AdditionalProductMapper;
import org.example.mappers.BasicProductMapper;
import org.example.mappers.MealMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class MealDb {
    private BasicProductDb basicProductDb = new BasicProductDb();
    private BasicProductMapper basicProductMapper = new BasicProductMapper();
    private MealMapper mealMapper = new MealMapper();
    private AdditionalProductDb additionalProductDb = new AdditionalProductDb();
    private AdditionalProductMapper additionalProductMapper = new AdditionalProductMapper();

    public Meal getBasicMealByIdFromDb(Connection connection, long id) throws SQLException {
        String sql = "SELECT * FROM meal WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Meal meal = mealMapper.mapSqlResponseToMeal(rs);
                integrateProduct(connection, meal);
                return meal;
            }
        }
    }

    public List<Meal> getAllMeals(Connection connection) throws SQLException {
        String query = "SELECT * FROM meal";

        List<Meal> meals = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Meal meal = mealMapper.mapSqlResponseToMeal(rs);
                // fetch basic product by its foreign key
                long bpId = rs.getLong("basic_product_id");
                BasicProduct bp = basicProductDb.getBasicProductByIdFromDb(connection, bpId);
                meal.setBasicProduct(bp);
                meal.setAdditionalProducts(getAdditionalProductsForMeal(connection, meal.getId()));
                meals.add(meal);
            }
        }
        return meals;
    }

    public List<AdditionalProduct> getAdditionalProductsForMeal(Connection connection, long mealId) throws SQLException {
        final String ING_QUERY =
                "SELECT ap.* FROM additional_product ap " +
                        "JOIN meal_additional_product map ON ap.id = map.additional_product_id " +
                        "WHERE map.meal_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(ING_QUERY)) {
            stmt.setLong(1, mealId);
            try (ResultSet rs = stmt.executeQuery()) {
                return additionalProductMapper.mapSqlResponseToListOfAdditionalProducts(rs);
            }
        }
    }

    private void integrateProduct(Connection connection, Meal meal) throws SQLException {
        // Already handled in getAllMeals; could be used for single fetch
        long basicProductId = meal.getBasicProduct().getId();
        BasicProduct basicProduct = basicProductDb.getBasicProductByIdFromDb(connection, basicProductId);
        meal.setBasicProduct(basicProduct);

        meal.setAdditionalProducts(getAdditionalProductsForMeal(connection, meal.getId()));
    }

    public Meal createNewMealInDb(Connection connection, Meal meal) throws SQLException {
        String insertMealSQL = "INSERT INTO meal (name, price, basic_product_id, image) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertMealSQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, meal.getName());
            stmt.setBigDecimal(2, meal.getPrice());
            stmt.setLong(3, meal.getBasicProduct().getId());
            stmt.setString(4, meal.getImage()); // image filename

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating meal failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    meal.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating meal failed, no ID obtained.");
                }
            }
        }

        // Insert additional product associations
        List<AdditionalProduct> additionalProducts = meal.getAdditionalProducts();
        if (additionalProducts != null && !additionalProducts.isEmpty()) {
            String insertJoinSQL = "INSERT INTO meal_additional_product (meal_id, additional_product_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertJoinSQL)) {
                for (AdditionalProduct ap : additionalProducts) {
                    stmt.setLong(1, meal.getId());
                    stmt.setLong(2, ap.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        return meal;
    }

    public Meal updateMealInDb(Connection connection, final Meal meal) throws SQLException {
        String updateMealSQL =
                "UPDATE meal SET name = ?, price = ?, basic_product_id = ?, image = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateMealSQL)) {
            stmt.setString(1, meal.getName());
            stmt.setBigDecimal(2, meal.getPrice());
            stmt.setLong(3, meal.getBasicProduct().getId());
            stmt.setString(4, meal.getImage()); // image filename
            stmt.setLong(5, meal.getId());
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Updating meal failed: no rows affected.");
            }
        }

        // Delete and reinsert additional‐product links
        String deleteLinksSQL = "DELETE FROM meal_additional_product WHERE meal_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteLinksSQL)) {
            stmt.setLong(1, meal.getId());
            stmt.executeUpdate();
        }

        List<AdditionalProduct> aps = meal.getAdditionalProducts();
        if (aps != null && !aps.isEmpty()) {
            String insertLinkSQL = "INSERT INTO meal_additional_product (meal_id, additional_product_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertLinkSQL)) {
                for (AdditionalProduct ap : aps) {
                    stmt.setLong(1, meal.getId());
                    stmt.setLong(2, ap.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        return meal;
    }

    public void deleteMealById(Connection connection, long id) throws SQLException {
        final String DEL = "DELETE FROM meal WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(DEL)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
