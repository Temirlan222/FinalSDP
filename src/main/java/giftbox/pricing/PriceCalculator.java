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
        int sum = 0;

        for (Gift gift : gifts) {
            int cost = Objects.requireNonNull(gift, "gift").getPrice();
            if (cost < 0) {
                throw new IllegalArgumentException("Gift price cannot be negative");
            }
            sum = Math.addExact(sum, cost);
        }
        return sum;
    }

    public int calculateTotal(List<? extends Gift> gifts) {
        int subtotal = calculateSubtotal(gifts);
        int result = discountStrategy.applyDiscount(subtotal);
        if (result < 0 || result > subtotal) {
            throw new IllegalArgumentException("Discount result must be between zero and subtotal");
        }
        return result;
    }
}
