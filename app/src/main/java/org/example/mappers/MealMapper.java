package org.example.mappers;

import lombok.AllArgsConstructor;
import org.example.domain.BasicProduct;
import org.example.domain.Meal;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MealMapper {

    public Meal mapSqlResponseToMeal(final ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet can not be null");
        }

        return Meal.builder()
                    .id(Long.parseLong(rs.getString("id")))
                    .name(rs.getString("name"))
                    .price(new BigDecimal(rs.getString("price")))
                    .image(rs.getString("image"))
                    .basicProduct(BasicProduct.builder()
                            .id(rs.getLong("basic_product_id"))
                            .build())
                    .build();

    }

    public List<Meal> mapSqlRequestToListOfMeals(final ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet can not be null");
        }

        List<Meal> meals = new ArrayList<>();

        while (rs.next()) {
            Meal meal = mapSqlResponseToMeal(rs);
            meals.add(meal);
        }
        return meals;
    }

}

