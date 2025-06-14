package org.example.domain;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.enumeration.Size;
import org.example.enumeration.Type;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class BasicProduct implements Product {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private Type type;
    private Size size;
    private int quantity;
    private List<Ingredient> ingredients;

    public String getIngredientInfo() {
        String info = "";
        for (Ingredient ingredient : ingredients) {
            info = info + ingredient.getName() +"\n";
        }
        return info;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal itp = BigDecimal.ZERO;
        for (Ingredient i : ingredients) {
            itp = itp.add(i.getPrice());
        }
        BigDecimal totalPrice = price.add(itp);
        return totalPrice;
    }

}
