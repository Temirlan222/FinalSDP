package giftbox.api;

/** Shared product contract. Price is a nonnegative integer number of tenge. */
public interface Gift {
    String getDescription();
    int getPrice();
}
