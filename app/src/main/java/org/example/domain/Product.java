package org.example.domain;

import java.math.BigDecimal;
import java.util.List;

import org.example.enumeration.Size;
import org.example.enumeration.Type;

public interface Product {
    Long getId();

    String getName();

    String getImage();

    BigDecimal getPrice();

    Type getType();

    Size getSize();

    List<Ingredient> getIngredients();
}
