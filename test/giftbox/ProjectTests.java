package giftbox;

import giftbox.api.Gift;
import giftbox.builder.GiftBox;
import giftbox.decorator.GiftWrap;
import giftbox.facade.GiftBoxFacade;
import giftbox.facade.GiftSelection;
import giftbox.facade.OrderReceipt;
import giftbox.facade.Quote;
import giftbox.factory.BookCreator;
import giftbox.factory.CandleCreator;
import giftbox.factory.GiftCreator;
import giftbox.factory.TeaCreator;
import giftbox.observer.CustomerNotifications;
import giftbox.observer.Order;
import giftbox.observer.OrderObserver;
import giftbox.observer.OrderStatus;
import giftbox.observer.PackingDesk;
import giftbox.strategy.NoDiscount;
import giftbox.strategy.StudentDiscount;
import giftbox.strategy.ThresholdDiscount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProjectTests {
    private static int passed;

    public static void main(String[] args) {
        run("factory subclasses supply distinct products", ProjectTests::factoryProducts);
        run("new creator integrates without facade changes", ProjectTests::newCreator);
        run("builder freezes its list and supports reuse", ProjectTests::builderSnapshot);
        run("builder rejects missing and oversized input", ProjectTests::builderValidation);
        run("threshold strategy below, at and above boundary", ProjectTests::threshold);
        run("observer publishes to two independent subscribers", ProjectTests::twoObservers);
        run("unsubscribe and duplicate subscription", ProjectTests::unsubscribe);
        run("invalid transitions do not publish events", ProjectTests::statusValidation);
        run("facade composes all six patterns", ProjectTests::integration);
        run("order snapshots are independent of request lists", ProjectTests::independentOrders);
        run("facade rejects unknown and empty selections", ProjectTests::badRequests);
        run("quotes do not create orders or notifications", ProjectTests::quoteOnly);
        System.out.println("Project tests: " + passed + " passed, 0 failed.");
    }

    private static void factoryProducts() {
        GiftCreator[] creators = {new BookCreator(), new CandleCreator(), new TeaCreator()};
        int[] prices = {1500, 800, 1200};
        for (int i = 0; i < creators.length; i++) {
            Gift first = creators[i].createGift();
            equal(prices[i], first.getPrice());
            check(!first.getDescription().isBlank(), "Description is required");
            check(first != creators[i].createGift(), "Factory must create a fresh product");
        }
    }

    private static void newCreator() {
        GiftCreator custom = new GiftCreator() {
            protected Gift createProduct() { return new GiftWrap(new TeaCreator().createGift()); }
        };
        GiftBoxFacade facade = new GiftBoxFacade(Map.of("custom", custom));
        equal(1500, facade.quote("Ada", "", List.of(new GiftSelection("custom", false, true)), new NoDiscount()).getTotal());
    }

    private static void builderSnapshot() {
        GiftBox.Builder builder = new GiftBox.Builder().forRecipient("  Ada  ").withMessage(" Hello ")
                .addGift(new BookCreator().createGift());
        GiftBox first = builder.build();
        builder.addGift(new CandleCreator().createGift()).forRecipient("Bob");
        equal(1, first.getGifts().size()); equal(2, builder.build().getGifts().size());
        check(first.getRecipient().equals("Ada") && first.getMessage().equals("Hello"), "Snapshot changed");
        expect(UnsupportedOperationException.class, () -> first.getGifts().clear());
    }

    private static void builderValidation() {
        expect(IllegalArgumentException.class, () -> new GiftBox.Builder().build());
        expect(IllegalArgumentException.class, () -> new GiftBox.Builder().forRecipient(" "));
        expect(IllegalArgumentException.class, () -> new GiftBox.Builder().forRecipient("A").build());
        expect(IllegalArgumentException.class, () -> new GiftBox.Builder().forRecipient("A".repeat(61)));
        expect(IllegalArgumentException.class, () -> new GiftBox.Builder().withMessage("x".repeat(201)));
        expect(NullPointerException.class, () -> new GiftBox.Builder().addGift(null));
        GiftBox.Builder builder = new GiftBox.Builder().forRecipient("A");
        for (int i = 0; i < 12; i++) { builder.addGift(new BookCreator().createGift()); }
        equal(12, builder.build().getGifts().size());
        expect(IllegalArgumentException.class, () -> builder.addGift(new BookCreator().createGift()));
    }

    private static void threshold() {
        ThresholdDiscount discount = new ThresholdDiscount();
        equal(0, discount.applyDiscount(0)); equal(4999, discount.applyDiscount(4999));
        equal(4500, discount.applyDiscount(5000)); equal(4501, discount.applyDiscount(5001));
        equal(Integer.MAX_VALUE - 500, discount.applyDiscount(Integer.MAX_VALUE));
        expect(IllegalArgumentException.class, () -> discount.applyDiscount(-1));
    }

    private static void twoObservers() {
        Order order = new Order("test");
        CustomerNotifications customer = new CustomerNotifications();
        PackingDesk desk = new PackingDesk();
        order.subscribe(customer); order.subscribe(desk);
        order.advanceTo(OrderStatus.PACKING); order.advanceTo(OrderStatus.READY);
        equal(2, customer.getMessages().size()); equal(2, desk.getMessages().size());
        check(customer.getMessages().get(1).contains("READY"), "Customer missed READY");
        check(desk.getMessages().get(0).contains("PACKING"), "Packing desk missed PACKING");
        expect(UnsupportedOperationException.class, () -> customer.getMessages().clear());
    }

    private static void unsubscribe() {
        Order order = new Order("test");
        List<OrderStatus> events = new ArrayList<>();
        OrderObserver observer = (id, status) -> events.add(status);
        order.subscribe(observer); order.subscribe(observer);
        order.advanceTo(OrderStatus.PACKING); equal(1, events.size());
        order.unsubscribe(observer); order.advanceTo(OrderStatus.READY); equal(1, events.size());
    }

    private static void statusValidation() {
        Order order = new Order("test");
        CustomerNotifications customer = new CustomerNotifications(); order.subscribe(customer);
        expect(IllegalArgumentException.class, () -> order.advanceTo(OrderStatus.READY));
        expect(IllegalArgumentException.class, () -> order.advanceTo(OrderStatus.CREATED));
        equal(0, customer.getMessages().size()); check(order.getStatus() == OrderStatus.CREATED, "Invalid state changed");
        order.advanceTo(OrderStatus.PACKING); order.advanceTo(OrderStatus.READY);
        expect(IllegalArgumentException.class, () -> order.advanceTo(OrderStatus.PACKING));
    }

    private static List<GiftSelection> sample() {
        return List.of(new GiftSelection("book", true, true), new GiftSelection("candle", false, false));
    }

    private static void integration() {
        OrderReceipt receipt = new GiftBoxFacade().placeOrder("Ada", "Enjoy!", sample(), new StudentDiscount());
        equal(2600, receipt.getQuote().getSubtotal()); equal(2340, receipt.getQuote().getTotal());
        equal(260, receipt.getQuote().getSaving());
        check(receipt.getQuote().getBox().getGifts().get(0).getDescription().contains("ribbon"), "Missing decorator");
        receipt.advanceTo(OrderStatus.PACKING); receipt.advanceTo(OrderStatus.READY);
        equal(2, receipt.getCustomerMessages().size()); equal(2, receipt.getPackingMessages().size());
    }

    private static void independentOrders() {
        GiftBoxFacade facade = new GiftBoxFacade();
        List<GiftSelection> selections = new ArrayList<>(sample());
        OrderReceipt first = facade.placeOrder("Ada", "", selections, new NoDiscount());
        selections.clear(); equal(2, first.getQuote().getBox().getGifts().size());
        OrderReceipt second = facade.placeOrder("Bob", "", sample(), new StudentDiscount());
        check(!first.getId().equals(second.getId()), "Order identifiers collide");
        first.advanceTo(OrderStatus.PACKING);
        equal(0, second.getCustomerMessages().size());
        equal(2600, first.getQuote().getTotal()); equal(2340, second.getQuote().getTotal());
    }

    private static void badRequests() {
        GiftBoxFacade facade = new GiftBoxFacade();
        expect(IllegalArgumentException.class, () -> facade.quote("A", "", List.of(), new NoDiscount()));
        expect(IllegalArgumentException.class, () -> facade.quote("A", "",
                List.of(new GiftSelection("missing", false, false)), new NoDiscount()));
    }

    private static void quoteOnly() {
        GiftBoxFacade facade = new GiftBoxFacade();
        Quote quote = facade.quote("A", "", sample(), new NoDiscount());
        equal(2600, quote.getTotal());
        OrderReceipt receipt = facade.placeOrder("A", "", sample(), new NoDiscount());
        check(receipt.getStatus() == OrderStatus.CREATED, "New order is not CREATED");
        equal(0, receipt.getCustomerMessages().size());
    }

    private static void run(String name, Runnable test) {
        test.run(); passed++; System.out.println("PASS: " + name);
    }
    private static void equal(int expected, int actual) { check(expected == actual, "Expected " + expected + ", got " + actual); }
    private static void check(boolean condition, String message) { if (!condition) { throw new AssertionError(message); } }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable error) {
            check(type.isInstance(error), "Expected " + type.getSimpleName() + ", got " + error); return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
