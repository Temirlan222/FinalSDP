package giftbox.factory;

import giftbox.api.Gift;

/** Creator: subclasses choose the product used by this shared operation. */
public abstract class GiftCreator {
    public final Gift createGift() {
        Gift gift = createProduct();
        if (gift == null || gift.getPrice() < 0 || gift.getDescription().isBlank()) {
            throw new IllegalStateException("The creator returned an invalid gift");
        }
        return gift;
    }

    protected abstract Gift createProduct();
}
