package giftbox.api;

public interface DiscountStrategy {
    int applyDiscount(int subtotal);
}
