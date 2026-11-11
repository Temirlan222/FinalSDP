package giftbox.facade;

import giftbox.observer.CustomerNotifications;
import giftbox.observer.Order;
import giftbox.observer.OrderStatus;
import giftbox.observer.PackingDesk;
import java.util.List;

public final class OrderReceipt {
    private final Quote quote;
    private final Order order;
    private final CustomerNotifications customer = new CustomerNotifications();
    private final PackingDesk packingDesk = new PackingDesk();

    public OrderReceipt(String id, Quote quote) {
        this.quote = quote;
        order = new Order(id);
        order.subscribe(customer);
        order.subscribe(packingDesk);
    }

    public Quote getQuote() { return quote; }
    public String getId() { return order.getId(); }
    public OrderStatus getStatus() { return order.getStatus(); }
    public List<String> getCustomerMessages() { return customer.getMessages(); }
    public List<String> getPackingMessages() { return packingDesk.getMessages(); }
    public void advanceTo(OrderStatus status) { order.advanceTo(status); }
}
