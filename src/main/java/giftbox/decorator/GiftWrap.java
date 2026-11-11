package giftbox.decorator;

import giftbox.api.Gift;

public final class GiftWrap extends GiftDecorator {
    public GiftWrap(Gift gift) {
        super(gift);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + ", gift wrap";
    }

    @Override
    public int getPrice() {
        return Math.addExact(super.getPrice(), 200);
    }
}
