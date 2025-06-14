package org.example.domain;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Meal {
    private Long id;
    private String name;
    private String image;
    private BigDecimal price;
    private BasicProduct basicProduct;
    private List<AdditionalProduct> additionalProducts;
    private int quantity;

    public String getBasicProductInfo() {
        String basicProductInfo = "";
        if (basicProduct != null) {
            basicProductInfo = basicProduct.getName() + "\n";
        }
        return basicProductInfo;
    }

    public String getAdditionalProductsInfo() {
        String addotionalProductsInfo = "";
        if (additionalProducts != null) {
            for (AdditionalProduct additionalProduct : additionalProducts) {
                addotionalProductsInfo = addotionalProductsInfo + additionalProduct.getName() + "\n";
            }
        }
        return addotionalProductsInfo;
    }

    public String getMealInfo() {
        return getAdditionalProductsInfo() + getBasicProductInfo();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal aptp = BigDecimal.ZERO;
        for (AdditionalProduct ap : additionalProducts) {
            aptp = aptp.add(ap.getPrice());
        }
        BigDecimal totalPrice = price.add(aptp);
        return totalPrice;
    }
}
