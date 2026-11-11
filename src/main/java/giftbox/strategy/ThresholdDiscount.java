package giftbox.strategy;

import giftbox.api.DiscountStrategy;

public final class ThresholdDiscount implements DiscountStrategy {
    public static final int MINIMUM_SUBTOTAL = 5000;
    public static final int SAVING = 500;

    @Override
    public int applyDiscount(int subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        return subtotal >= MINIMUM_SUBTOTAL ? subtotal - SAVING : subtotal;
    }
}
