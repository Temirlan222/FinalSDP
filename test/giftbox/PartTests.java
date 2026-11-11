package giftbox;

import giftbox.api.DiscountStrategy;
import giftbox.api.Gift;
import giftbox.decorator.GiftWrap;
import giftbox.decorator.Ribbon;
import giftbox.pricing.PriceCalculator;
import giftbox.strategy.HolidayDiscount;
import giftbox.strategy.NoDiscount;
import giftbox.strategy.StudentDiscount;
import java.util.Arrays;
import java.util.Collections;

public final class PartTests {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        run("decorators compose without changing the base gift", PartTests::decoratorsCompose);
        run("calculator accepts a teammate's independent Gift", PartTests::independentGift);
        run("strategy changes at runtime", PartTests::strategyChanges);
        run("one discount applies after the entire sum", PartTests::discountOnWholeSum);
        run("percentage totals round down", PartTests::rounding);
        run("empty and free orders cost zero", PartTests::emptyOrders);
        run("negative amounts are rejected", PartTests::negativePrices);
        run("null inputs are rejected", PartTests::nullInputs);
        run("decoration overflow is rejected", PartTests::decorationOverflow);
        run("subtotal overflow is rejected", PartTests::subtotalOverflow);
        run("large discounts do not overflow", PartTests::largeDiscounts);
        run("invalid discount results are rejected", PartTests::invalidDiscounts);
        System.out.println("Tests: " + passed + " passed, " + failed + " failed.");
        if (failed != 0) {
            throw new AssertionError("Student part checks failed");
        }
    }

    private static void decoratorsCompose() {
        Gift base = new OtherGift("Book", 1500);
        Gift wrapped = new Ribbon(new GiftWrap(base));
        equal(1800, wrapped.getPrice());
        equal(1500, base.getPrice());
        String description = wrapped.getDescription();
        check(description.contains("Book") && description.contains("gift wrap")
                && description.contains("ribbon"), "Both additions must appear in the description");
        check("Book".equals(base.getDescription()), "Decorator must not change the original");
        equal(1800, new GiftWrap(new Ribbon(base)).getPrice());
    }

    private static void independentGift() {
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        OtherGift candle = new OtherGift("Candle", 800);
        equal(800, calculator.calculateTotal(Collections.singletonList(candle)));
        equal(1100, calculator.calculateTotal(Collections.singletonList(new Ribbon(new GiftWrap(candle)))));
    }

    private static void strategyChanges() {
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        java.util.List<Gift> gifts = Arrays.asList(
                new Ribbon(new GiftWrap(new OtherGift("Book", 1500))), new OtherGift("Candle", 800));
        equal(2600, calculator.calculateSubtotal(gifts));
        equal(2600, calculator.calculateTotal(gifts));
        calculator.setDiscountStrategy(new StudentDiscount());
        equal(2340, calculator.calculateTotal(gifts));
        calculator.setDiscountStrategy(new HolidayDiscount());
        equal(2080, calculator.calculateTotal(gifts));
        equal(2600, calculator.calculateSubtotal(gifts));
        equal(2, gifts.size());
    }

    private static void discountOnWholeSum() {
        PriceCalculator calculator = new PriceCalculator(new StudentDiscount());
        equal(9, calculator.calculateTotal(Arrays.asList(new OtherGift("A", 1), new OtherGift("B", 9))));
    }

    private static void rounding() {
        equal(899, new StudentDiscount().applyDiscount(999));
        equal(799, new HolidayDiscount().applyDiscount(999));
        equal(0, new StudentDiscount().applyDiscount(1));
        equal(999, new NoDiscount().applyDiscount(999));
    }

    private static void emptyOrders() {
        PriceCalculator calculator = new PriceCalculator(new StudentDiscount());
        equal(0, calculator.calculateTotal(Collections.emptyList()));
        equal(0, calculator.calculateSubtotal(Collections.emptyList()));
        equal(0, calculator.calculateTotal(Collections.singletonList(new OtherGift("Free", 0))));
    }

    private static void negativePrices() {
        expect(IllegalArgumentException.class, () -> new NoDiscount().applyDiscount(-1));
        expect(IllegalArgumentException.class, () -> new StudentDiscount().applyDiscount(-1));
        expect(IllegalArgumentException.class, () -> new HolidayDiscount().applyDiscount(-1));
        Gift bad = new OtherGift("Invalid", -1);
        expect(IllegalArgumentException.class, () -> new GiftWrap(bad).getPrice());
        expect(IllegalArgumentException.class, () -> new Ribbon(bad).getPrice());
        expect(IllegalArgumentException.class, () -> new PriceCalculator(new NoDiscount())
                .calculateTotal(Collections.singletonList(bad)));
    }

    private static void nullInputs() {
        expect(NullPointerException.class, () -> new GiftWrap(null));
        expect(NullPointerException.class, () -> new Ribbon(null));
        expect(NullPointerException.class, () -> new PriceCalculator(null));
        PriceCalculator calculator = new PriceCalculator(new StudentDiscount());
        expect(NullPointerException.class, () -> calculator.setDiscountStrategy(null));
        equal(90, calculator.calculateTotal(Collections.singletonList(new OtherGift("A", 100))));
        expect(NullPointerException.class, () -> calculator.calculateTotal(null));
        expect(NullPointerException.class, () -> calculator.calculateTotal(Collections.singletonList(null)));
    }

    private static void decorationOverflow() {
        Gift expensive = new OtherGift("Expensive", Integer.MAX_VALUE);
        expect(ArithmeticException.class, () -> new GiftWrap(expensive).getPrice());
        expect(ArithmeticException.class, () -> new Ribbon(expensive).getPrice());
    }

    private static void subtotalOverflow() {
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        expect(ArithmeticException.class, () -> calculator.calculateSubtotal(Arrays.asList(
                new OtherGift("A", Integer.MAX_VALUE), new OtherGift("B", 1))));
    }

    private static void largeDiscounts() {
        equal(1932735282, new StudentDiscount().applyDiscount(Integer.MAX_VALUE));
        equal(1717986917, new HolidayDiscount().applyDiscount(Integer.MAX_VALUE));
    }

    private static void invalidDiscounts() {
        Gift gift = new OtherGift("Book", 100);
        DiscountStrategy negative = subtotal -> -1;
        DiscountStrategy increase = subtotal -> subtotal + 1;
        expect(IllegalArgumentException.class, () -> new PriceCalculator(negative)
                .calculateTotal(Collections.singletonList(gift)));
        expect(IllegalArgumentException.class, () -> new PriceCalculator(increase)
                .calculateTotal(Collections.singletonList(gift)));
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (Throwable failure) {
            failed++;
            System.out.println("FAIL: " + name + " -> " + failure);
        }
    }

    private static void equal(int expected, int actual) {
        check(expected == actual, "Expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            check(type.isInstance(actual), "Expected " + type.getSimpleName() + ", got " + actual);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static final class OtherGift implements Gift {
        private final String description;
        private final int price;

        private OtherGift(String description, int price) {
            this.description = description;
            this.price = price;
        }

        @Override
        public String getDescription() { return description; }

        @Override
        public int getPrice() { return price; }
    }
}
