package org.example.db;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.domain.Ingredient;
import org.example.mappers.IngredientMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

@NoArgsConstructor
@AllArgsConstructor
public class IngredientsDb {
    IngredientMapper ingredientMapper = new IngredientMapper();


    public List<Ingredient> getAllAvailableIngredients(Connection connection) throws SQLException {
        List<Ingredient> finalList = new ArrayList<>();
        final String ING_QUERY = "SELECT * from ingredient";
        PreparedStatement stmt = connection.prepareStatement(ING_QUERY);
        try (ResultSet rs = stmt.executeQuery()) {
            finalList = ingredientMapper.mapSqlResponseToListOfIngredients(rs);
        }
        return finalList;
    }


    //
    public Ingredient createNewIngredientInDb(Connection connection, final Ingredient ingredient) throws SQLException {
        final String INSERT_ING =
                "INSERT INTO ingredient (name, price, image) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(
                INSERT_ING,
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ingredient.getName());
            stmt.setBigDecimal(2, ingredient.getPrice());
            stmt.setString(3, ingredient.getImage());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ingredient failed: no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {

                    long id = generatedKeys.getLong(1);
                    //long id = generatedKeys.getLong("id");

                    ingredient.setId(id);
                } else {
                    throw new SQLException("Creating ingredient failed: no ID obtained.");
                }
            }
        }
        return ingredient;
    }

    public void deleteIngredientById(Connection connection, long id) throws SQLException {
        final String DEL = "DELETE FROM ingredient WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(DEL)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public Ingredient UpdateIngredientInDb(Connection connection, final Ingredient ingredient) throws SQLException {
        final String UPDATE_SQL =
                "UPDATE ingredient SET name = ?, price = ?, image = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, ingredient.getName());
            stmt.setBigDecimal(2, ingredient.getPrice());
            stmt.setString(3, ingredient.getImage());
            stmt.setLong(4, ingredient.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating ingredient failed: no rows affected.");
            }
        }

        return ingredient;
    }
}
