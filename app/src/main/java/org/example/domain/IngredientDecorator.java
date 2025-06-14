package org.example.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IngredientDecorator extends ProductDecorator{
  private final Ingredient ingredient;

  public IngredientDecorator(Product decoratedProduct, Ingredient ingredient) {
    super(decoratedProduct);
    this.ingredient = ingredient;
  }

  @Override
  public BigDecimal getPrice() {
    return super.getPrice().add(ingredient.getPrice());
  }

  @Override
  public List<Ingredient> getIngredients() {
    List<Ingredient> ingredients = new ArrayList<>(super.getIngredients());
    ingredients.add(ingredient);
    return ingredients;
  }

  @Override
  public String getName() {
    return super.getName() + " with " + ingredient.getName();
  }
}
