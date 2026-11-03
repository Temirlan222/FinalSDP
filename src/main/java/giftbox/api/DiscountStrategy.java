package giftbox.api;

/** Returns the final price after one discount, between zero and subtotal. */
public interface DiscountStrategy {
    int applyDiscount(int subtotal);
}
