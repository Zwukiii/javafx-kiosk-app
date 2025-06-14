package org.example.domain;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderedProduct {
    private Long idProduct;
    private String name;
    private BigDecimal price;
    private String type;
    private String size;
    private int quantity;
}