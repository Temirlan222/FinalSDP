package giftbox.web;

import giftbox.api.Gift;
import giftbox.facade.OrderReceipt;
import giftbox.facade.Quote;
import java.util.List;
import java.util.stream.Collectors;

final class Json {
    private Json() { }

    static String quote(Quote quote) {
        String items = quote.getBox().getGifts().stream().map(Json::gift).collect(Collectors.joining(","));
        return "{\"recipient\":" + string(quote.getBox().getRecipient())
                + ",\"message\":" + string(quote.getBox().getMessage())
                + ",\"subtotal\":" + quote.getSubtotal() + ",\"total\":" + quote.getTotal()
                + ",\"saving\":" + quote.getSaving() + ",\"items\":[" + items + "]}";
    }

    static String receipt(OrderReceipt receipt) {
        return "{\"id\":" + string(receipt.getId()) + ",\"status\":" + string(receipt.getStatus().name())
                + ",\"quote\":" + quote(receipt.getQuote())
                + ",\"customer\":" + strings(receipt.getCustomerMessages())
                + ",\"packing\":" + strings(receipt.getPackingMessages()) + "}";
    }

    private static String gift(Gift gift) {
        return "{\"description\":" + string(gift.getDescription()) + ",\"price\":" + gift.getPrice() + "}";
    }

    private static String strings(List<String> values) {
        return values.stream().map(Json::string).collect(Collectors.joining(",", "[", "]"));
    }

    static String string(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (character < 32) { result.append(String.format("\\u%04x", (int) character)); }
                    else { result.append(character); }
            }
        }
        return result.append('"').toString();
    }
}
