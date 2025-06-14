package org.example.mappers;

import org.example.domain.Ingredient;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredientMapper {

    public Ingredient mapSqlResponseTo(final ResultSet rs) throws SQLException {

        if (rs == null) {
            throw new IllegalArgumentException("ResultSet con not be null");
        }

        if (rs.next()) {
            Ingredient ingredient = Ingredient.builder()
                    .id(Long.parseLong(rs.getString(1)))
                    .name(rs.getString(2))
                    .price(new BigDecimal(rs.getString(3)))
                    .build();
            return ingredient;
        }

        return new Ingredient();
    }


    // DO TO REPAIR RS INPUT Same like in BasicProductMapper
    public List<Ingredient> mapSqlResponseToListOfIngredients(final ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet con not be null");
        }

        List<Ingredient> ingredients = new ArrayList<>();

        while (rs.next()) {
            Ingredient ingredient = Ingredient.builder()
                    .id(Long.parseLong(rs.getString(1)))
                    .name(rs.getString(2))
                    .image(rs.getString("image"))
                    .price(rs.getBigDecimal("price"))
                    .build();
            ingredients.add(ingredient);
        }

        return ingredients;
    }
}
