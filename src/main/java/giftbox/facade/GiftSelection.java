package giftbox.facade;

import java.util.Objects;

public final class GiftSelection {
    private final String product;
    private final boolean wrapped;
    private final boolean ribbon;

    public GiftSelection(String product, boolean wrapped, boolean ribbon) {
        this.product = Objects.requireNonNull(product, "product");
        this.wrapped = wrapped;
        this.ribbon = ribbon;
    }

    public String getProduct() { return product; }
    public boolean isWrapped() { return wrapped; }
    public boolean hasRibbon() { return ribbon; }
}
