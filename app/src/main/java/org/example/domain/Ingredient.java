package org.example.domain;

import java.math.BigDecimal;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class Ingredient {

    private Long id;
    private String name;
    private BigDecimal price;
    private String image;

}
