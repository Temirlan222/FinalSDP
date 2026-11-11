package giftbox.demo;

import giftbox.api.Gift;
import giftbox.decorator.GiftWrap;
import giftbox.decorator.Ribbon;
import giftbox.pricing.PriceCalculator;
import giftbox.strategy.HolidayDiscount;
import giftbox.strategy.NoDiscount;
import giftbox.strategy.StudentDiscount;
import java.util.Arrays;
import java.util.List;

public final class Demo {
    public static void main(String[] args) {
        Gift book = new ExampleGift("Book", 1500);
        Gift wrappedBook = new Ribbon(new GiftWrap(book));
        Gift candle = new ExampleGift("Candle", 800);
        List<Gift> gifts = Arrays.asList(
                wrappedBook, candle);

        System.out.println("Decorator:");
        for (Gift gift : gifts) {
            System.out.println(gift.getDescription() + ": " + gift.getPrice() + " KZT");
        }

        PriceCalculator prices = new PriceCalculator(new NoDiscount());
        System.out.println("Subtotal: " + prices.calculateSubtotal(gifts) + " KZT");
        System.out.println("\nStrategy (the same gifts, a different rule):");
        System.out.println("No discount: " + prices.calculateTotal(gifts) + " KZT");
        prices.setDiscountStrategy(new StudentDiscount());
        System.out.println("Student discount 10%: " + prices.calculateTotal(gifts) + " KZT");
        prices.setDiscountStrategy(new HolidayDiscount());
        System.out.println("Holiday discount 20%: " + prices.calculateTotal(gifts) + " KZT");
        System.out.println("Original book is unchanged: " + book.getPrice() + " KZT");
    }

    private static final class ExampleGift implements Gift {
        private final String description;
        private final int price;

        private ExampleGift(String description, int price) {
            this.description = description;
            this.price = price;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getPrice() {
            return price;
        }
    }
}
