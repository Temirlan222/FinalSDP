package giftbox.pricing;

import giftbox.api.DiscountStrategy;
import giftbox.api.Gift;
import java.util.List;
import java.util.Objects;

public final class PriceCalculator {
    private DiscountStrategy discountStrategy;

    public PriceCalculator(DiscountStrategy strategy) {
        setDiscountStrategy(strategy);
    }

    public void setDiscountStrategy(DiscountStrategy strategy) {
        discountStrategy = Objects.requireNonNull(strategy, "discount strategy");
    }

    public int calculateSubtotal(List<? extends Gift> gifts) {
        Objects.requireNonNull(gifts, "gifts");
        int subtotal = 0;
        for (Gift gift : gifts) {
            int price = Objects.requireNonNull(gift, "gift").getPrice();
            if (price < 0) {
                throw new IllegalArgumentException("Gift price cannot be negative");
            }
            subtotal = Math.addExact(subtotal, price);
        }
        return subtotal;
    }

    public int calculateTotal(List<? extends Gift> gifts) {
        int subtotal = calculateSubtotal(gifts);
        int total = discountStrategy.applyDiscount(subtotal);
        if (total < 0 || total > subtotal) {
            throw new IllegalArgumentException("Discount result must be between zero and subtotal");
        }
        return total;
    }
}
