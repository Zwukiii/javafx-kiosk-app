package org.example.domain;

import java.math.BigDecimal;
import java.util.List;

import org.example.enumeration.Size;
import org.example.enumeration.Type;

public abstract class ProductDecorator implements Product {
    protected final Product decoratedProduct;

    public ProductDecorator(Product decoratedProduct) {
        this.decoratedProduct = decoratedProduct;
    }

    @Override
    public Long getId() {
        return decoratedProduct.getId();
    }

    @Override
    public String getName() {
        return decoratedProduct.getName();
    }

    @Override
    public String getImage() {

        return decoratedProduct.getImage();
    }

    @Override
    public BigDecimal getPrice() {
        return decoratedProduct.getPrice();
    }

    @Override
    public Type getType() {
        return decoratedProduct.getType();
    }

    @Override
    public Size getSize() {
        return decoratedProduct.getSize();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return decoratedProduct.getIngredients();
    }
}
