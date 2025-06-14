package org.example.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@ToString
public class Cart {
    private List<Meal> meals;
    private List<BasicProduct> basicProducts;
    private List<AdditionalProduct> additionalProducts;
    private BigDecimal totalPrice;
    private double promoCode;
    private boolean promoApplied = false;
    private String dineType = "";

    public Cart() {
        this.meals = new ArrayList<>();
        this.basicProducts = new ArrayList<>();
        this.additionalProducts = new ArrayList<>();
        this.totalPrice = BigDecimal.ZERO;
    }

    public void updateTotalPrice() {
        this.totalPrice = countPrice().setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal countPrice() {
        // Calculates the total price in the cart.
        BigDecimal total = new BigDecimal("0");
        if (this.meals != null) {
            for (Meal meal : meals) {
                total = total.add(meal.getPrice());
            }
        }

        // calculates the total price of basic products
        if (this.basicProducts != null) {
            for (BasicProduct bp : basicProducts) {
                total = total.add(bp.getPrice());
            }
        }
        // calculates the total price of additional products
        if (this.additionalProducts != null) {
            for (AdditionalProduct ap : additionalProducts) {
                BigDecimal itemPrice = ap.getPrice().multiply(new BigDecimal(ap.getQuantity()));
                total = total.add(itemPrice);
            }
        }
        return total;


    }

    private final List<Object> addedItems = new ArrayList<>();


    public void addMeal(Meal meal) {
        meals.add(meal);
        addedItems.add(meal);
        updateTotalPrice();
    }

    public void addBasicProduct(BasicProduct basicProduct) {
        basicProducts.add(basicProduct);
        addedItems.add(basicProduct);
        updateTotalPrice();
    }

    public void addAdditionalProduct(AdditionalProduct additionalProduct) {
        for (AdditionalProduct ad : additionalProducts) {
            if (Objects.equals(ad.getId(), additionalProduct.getId())) {
                ad.setQuantity(ad.getQuantity() + 1);
                updateTotalPrice();
                return;
            }
        }
        additionalProduct.setQuantity(1);
        additionalProducts.add(additionalProduct);
        addedItems.add(additionalProduct);
        updateTotalPrice();
    }

    public List<Object> getAddedItems() {
        return new ArrayList<>(addedItems);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal cartTotal = countPrice();
        if (promoApplied) {
            BigDecimal discount = cartTotal.multiply(BigDecimal.valueOf(promoCode / 100.0));
            this.totalPrice = cartTotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
            return totalPrice;
        } else {
            this.totalPrice = cartTotal.setScale(2, RoundingMode.HALF_UP);
            return totalPrice;
        }
    }

    public void applyPromoCode() {
        BigDecimal originalPrice = countPrice();

        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            this.totalPrice = originalPrice.setScale(2, RoundingMode.HALF_UP);
            this.promoApplied = false;
            return;
        }
        if (promoCode > 0 && promoCode <= 100) {
            BigDecimal discount = originalPrice
                    .multiply(BigDecimal.valueOf(promoCode).divide(BigDecimal.valueOf(100)));
            this.totalPrice = originalPrice.subtract(discount)
                    .setScale(2, RoundingMode.HALF_UP);
            this.promoApplied = true;
        } else{
            this.totalPrice = originalPrice.
                    setScale(2, RoundingMode.HALF_UP);
            this.promoApplied = false;
            System.out.println("Promo code is invalid");
        }
    }


    public int getItemQuantity(Object item) {

        if (item instanceof Meal) {
            for (Meal meal : meals) {
                if (meal.equals(item)) {
                    return Math.max(1, meal.getQuantity());
                }
            }
        } else if (item instanceof BasicProduct) {
            for (BasicProduct basicProduct : basicProducts) {
                if (basicProduct.equals(item)) {
                    return Math.max(1, basicProduct.getQuantity());
                }
            }
        } else if (item instanceof AdditionalProduct) {
            for (AdditionalProduct additionalProduct : additionalProducts) {
                if (additionalProduct.equals(item)) {
                    return Math.max(1, additionalProduct.getQuantity());
                }
            }
        }
        return 1;
    }


    public void increaseAdditionalProductQuantity(AdditionalProduct product) {
        for (AdditionalProduct ap : additionalProducts) {
            if (ap.equals(product)) {
                ap.setQuantity(ap.getQuantity() + 1);
                updateTotalPrice();
                return;
            }
        }
    }

    public void decreaseAdditionalProductQuantity(AdditionalProduct product) {
        for (AdditionalProduct ap : additionalProducts) {
            if (ap.equals(product)) {
                if (ap.getQuantity() > 1) {
                    ap.setQuantity(ap.getQuantity() - 1);
                } else {
                    additionalProducts.remove(ap);
                }
                updateTotalPrice();
                return;
            }
        }
    }

    public void clearCart() {
        meals = new ArrayList<>();
        basicProducts = new ArrayList<>();
        additionalProducts = new ArrayList<>();

        this.totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.promoCode = 0.0;

        //App.split.getItems().setAll(App.menuView, cartView);

    }

    public void removeMeal(Meal meal) {
        meals.remove(meal);
    }

    public void removeBasicProduct(BasicProduct basicProduct) {
        basicProducts.remove(basicProduct);
    }

    public void removeAdditionalProduct(AdditionalProduct additionalProduct) {
        additionalProducts.remove(additionalProduct);
    }
}
