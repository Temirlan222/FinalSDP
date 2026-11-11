package giftbox.strategy;

final class DiscountMath {
    private DiscountMath() {
    }

    static int priceAfterDiscount(int subtotal, int discountPercent) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        // Multiply as long to avoid overflow; integer division rounds down.
        return (int) ((long) subtotal * (100 - discountPercent) / 100);
    }
}
