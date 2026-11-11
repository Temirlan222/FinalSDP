package giftbox.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Order {
    private final String id;
    private OrderStatus status = OrderStatus.CREATED;
    private final List<OrderObserver> observers = new ArrayList<>();

    public Order(String id) { this.id = Objects.requireNonNull(id, "order id"); }
    public String getId() { return id; }
    public OrderStatus getStatus() { return status; }

    public void subscribe(OrderObserver observer) {
        Objects.requireNonNull(observer, "observer");
        if (!observers.contains(observer)) { observers.add(observer); }
    }

    public void unsubscribe(OrderObserver observer) { observers.remove(observer); }

    public void advanceTo(OrderStatus next) {
        Objects.requireNonNull(next, "status");
        if (next.ordinal() != status.ordinal() + 1) {
            throw new IllegalArgumentException("Follow CREATED -> PACKING -> READY");
        }
        status = next;
        for (OrderObserver observer : List.copyOf(observers)) {
            observer.onStatusChanged(id, status);
        }
    }
}
