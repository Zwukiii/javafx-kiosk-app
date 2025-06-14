package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private String orderId;
    private BigDecimal totalPrice;
    private double promoCode;
    private LocalDateTime createdAt;
    private String dineType;


    @Builder.Default
    private List<OrderedProduct> orderedProducts = new ArrayList<>();


    public Order(Cart cart, String orderId) {
        this.orderId = orderId;
        this.totalPrice = cart.getTotalPrice();
        this.promoCode = cart.getPromoCode();
        this.createdAt = LocalDateTime.now();
        this.orderedProducts = new ArrayList<>();
        this.dineType = cart.getDineType();

        if (cart.getMeals() != null) {
            cart.getMeals().forEach(meal ->
                    orderedProducts.add(
                            OrderedProduct.builder()
                                    .idProduct(meal.getId())
                                    .name(meal.getName())
                                    .price(meal.getPrice())
                                    .type("MEAL")
                                    .size("ONE SIZE")
                                    .quantity(1)
                                    .build()
                    )
            );
        }

        if (cart.getBasicProducts() != null) {
            cart.getBasicProducts().forEach(bp ->
                    orderedProducts.add(
                            OrderedProduct.builder()
                                    .idProduct(bp.getId())
                                    .name(bp.getName())
                                    .price(bp.getPrice())
                                    .type("BASIC")
                                    .size(bp.getSize().toString())
                                    .quantity(1)
                                    .build()
                    )
            );
        }

        if (cart.getAdditionalProducts() != null) {
            cart.getAdditionalProducts().forEach(ap ->
                    orderedProducts.add(
                            OrderedProduct.builder()
                                    .idProduct(ap.getId())
                                    .name(ap.getName())
                                    .price(ap.getPrice())
                                    .type("ADDITIONAL")
                                    .size(ap.getSize().toString())
                                    .quantity(ap.getQuantity())
                                    .build()
                    )
            );
        }
    }


    public String getOrderedProductsSummary() {
        return orderedProducts.stream()
                .map(op -> String.format("%s: %s (%.2f €)",
                        op.getType(),
                        op.getName(),
                        op.getPrice().doubleValue()))
                .collect(Collectors.joining("; "));
    }
}