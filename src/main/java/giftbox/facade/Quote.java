package giftbox.facade;

import giftbox.builder.GiftBox;

public final class Quote {
    private final GiftBox box;
    private final int subtotal;
    private final int total;

    public Quote(GiftBox box, int subtotal, int total) {
        this.box = box;
        this.subtotal = subtotal;
        this.total = total;
    }

    public GiftBox getBox() { return box; }
    public int getSubtotal() { return subtotal; }
    public int getTotal() { return total; }
    public int getSaving() { return subtotal - total; }
}
