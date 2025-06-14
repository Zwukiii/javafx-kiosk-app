package org.example.domain;

import lombok.*;
import org.example.enumeration.Size;
import org.example.enumeration.Type;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class AdditionalProduct {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Type type;
    private Size size;
    private int quantity;
}
