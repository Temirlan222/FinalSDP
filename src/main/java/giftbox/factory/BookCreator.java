package giftbox.factory;

import giftbox.api.Gift;

public final class BookCreator extends GiftCreator {
    @Override
    protected Gift createProduct() { return new Book(); }

    private static final class Book implements Gift {
        public String getDescription() { return "Book"; }
        public int getPrice() { return 1500; }
    }
}
