package in.onewish.model;

import java.math.BigDecimal;

public final class OrderMath {
    private OrderMath() {}
    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null || unitPrice.signum() < 0 || quantity < 1 || quantity > 20) {
            throw new IllegalArgumentException("Invalid order line.");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
