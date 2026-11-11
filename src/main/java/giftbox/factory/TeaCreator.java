package giftbox.factory;

import giftbox.api.Gift;

public final class TeaCreator extends GiftCreator {
    @Override
    protected Gift createProduct() { return new Tea(); }

    private static final class Tea implements Gift {
        public String getDescription() { return "Tea collection"; }
        public int getPrice() { return 1200; }
    }
}
