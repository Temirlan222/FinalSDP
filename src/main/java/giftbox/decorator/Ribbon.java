package giftbox.decorator;

import giftbox.api.Gift;

public final class Ribbon extends GiftDecorator {
    public Ribbon(Gift gift) {
        super(gift);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + ", ribbon";
    }

    @Override
    public int getPrice() {
        return Math.addExact(super.getPrice(), 100);
    }
}
