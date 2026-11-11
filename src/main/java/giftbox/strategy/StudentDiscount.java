package giftbox.strategy;

import giftbox.api.DiscountStrategy;

public final class StudentDiscount implements DiscountStrategy {
    @Override
    public int applyDiscount(int subtotal) {
        return DiscountMath.priceAfterDiscount(subtotal, 10);
    }
}
