package giftbox.observer;

import java.util.ArrayList;
import java.util.List;

public final class CustomerNotifications implements OrderObserver {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void onStatusChanged(String orderId, OrderStatus status) {
        messages.add("Customer: " + orderId + " is " + status);
    }

    public List<String> getMessages() { return List.copyOf(messages); }
}
