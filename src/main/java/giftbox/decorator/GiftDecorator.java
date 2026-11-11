package giftbox.decorator;

import giftbox.api.Gift;
import java.util.Objects;

public abstract class GiftDecorator implements Gift {
    private final Gift wrappedGift;

    protected GiftDecorator(Gift wrappedGift) {
        this.wrappedGift = Objects.requireNonNull(wrappedGift, "gift");
    }

    @Override
    public String getDescription() {
        return Objects.requireNonNull(wrappedGift.getDescription(), "gift description");
    }

    @Override
    public int getPrice() {
        int cost = wrappedGift.getPrice();
        if (cost < 0) {
            throw new IllegalArgumentException("Gift price cannot be negative");
        }
        return cost;
    }
}
