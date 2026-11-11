package giftbox.facade;

import giftbox.api.DiscountStrategy;
import giftbox.api.Gift;
import giftbox.builder.GiftBox;
import giftbox.decorator.GiftWrap;
import giftbox.decorator.Ribbon;
import giftbox.factory.BookCreator;
import giftbox.factory.CandleCreator;
import giftbox.factory.GiftCreator;
import giftbox.factory.TeaCreator;
import giftbox.pricing.PriceCalculator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** One entry point for assembling, pricing and placing a gift-box order. */
public final class GiftBoxFacade {
    private final Map<String, GiftCreator> creators;

    public GiftBoxFacade() {
        this(Map.of("book", new BookCreator(), "candle", new CandleCreator(), "tea", new TeaCreator()));
    }

    public GiftBoxFacade(Map<String, GiftCreator> creators) {
        this.creators = Map.copyOf(creators);
    }

    public Quote quote(String recipient, String message, List<GiftSelection> selections,
                       DiscountStrategy strategy) {
        Objects.requireNonNull(selections, "selections");
        GiftBox.Builder builder = new GiftBox.Builder().forRecipient(recipient).withMessage(message);
        for (GiftSelection selection : selections) {
            builder.addGift(createDecoratedGift(Objects.requireNonNull(selection, "selection")));
        }
        GiftBox box = builder.build();
        PriceCalculator calculator = new PriceCalculator(strategy);
        return new Quote(box, calculator.calculateSubtotal(box.getGifts()), calculator.calculateTotal(box.getGifts()));
    }

    public OrderReceipt placeOrder(String recipient, String message, List<GiftSelection> selections,
                                   DiscountStrategy strategy) {
        Quote quote = quote(recipient, message, selections, strategy);
        return new OrderReceipt("GB-" + UUID.randomUUID(), quote);
    }

    private Gift createDecoratedGift(GiftSelection selection) {
        GiftCreator creator = creators.get(selection.getProduct());
        if (creator == null) { throw new IllegalArgumentException("Unknown gift: " + selection.getProduct()); }
        Gift gift = creator.createGift();
        if (selection.isWrapped()) { gift = new GiftWrap(gift); }
        if (selection.hasRibbon()) { gift = new Ribbon(gift); }
        return gift;
    }
}
