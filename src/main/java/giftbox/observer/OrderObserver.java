package giftbox.observer;

@FunctionalInterface
public interface OrderObserver {
    void onStatusChanged(String orderId, OrderStatus status);
}
