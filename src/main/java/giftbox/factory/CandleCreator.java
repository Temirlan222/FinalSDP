package giftbox.factory;

import giftbox.api.Gift;

public final class CandleCreator extends GiftCreator {
    @Override
    protected Gift createProduct() { return new Candle(); }

    private static final class Candle implements Gift {
        public String getDescription() { return "Candle"; }
        public int getPrice() { return 800; }
    }
}
