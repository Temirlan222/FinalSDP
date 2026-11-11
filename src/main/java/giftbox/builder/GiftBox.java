package giftbox.builder;

import giftbox.api.Gift;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GiftBox {
    private final String recipient;
    private final String message;
    private final List<Gift> gifts;

    private GiftBox(Builder draft) {
        recipient = draft.recipient;
        message = draft.message;
        gifts = List.copyOf(draft.gifts);
    }

    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public List<Gift> getGifts() { return gifts; }

    public static final class Builder {
        private String recipient = "";
        private String message = "";
        private final List<Gift> gifts = new ArrayList<>();

        public Builder forRecipient(String name) {
            recipient = Objects.requireNonNull(name, "recipient").trim();
            if (recipient.isEmpty() || recipient.length() > 60) {
                throw new IllegalArgumentException("Recipient must contain 1 to 60 characters");
            }
            return this;
        }

        public Builder withMessage(String text) {
            message = Objects.requireNonNull(text, "message").trim();
            if (message.length() > 200) {
                throw new IllegalArgumentException("Message must not exceed 200 characters");
            }
            return this;
        }

        public Builder addGift(Gift gift) {
            if (gifts.size() == 12) {
                throw new IllegalArgumentException("A box can contain at most 12 gifts");
            }
            gifts.add(Objects.requireNonNull(gift, "gift"));
            return this;
        }

        public GiftBox build() {
            if (recipient.isEmpty() || gifts.isEmpty()) {
                throw new IllegalArgumentException("A recipient and at least one gift are required");
            }
            return new GiftBox(this);
        }
    }
}
