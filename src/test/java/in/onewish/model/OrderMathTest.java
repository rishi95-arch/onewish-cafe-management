package in.onewish.model;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderMathTest {
    @Test void multipliesPriceWithoutFloatingPointRounding() {
        assertEquals(new BigDecimal("482.70"), OrderMath.lineTotal(new BigDecimal("160.90"), 3));
    }
    @Test void acceptsMaximumCartLineQuantity() {
        assertEquals(new BigDecimal("200.00"), OrderMath.lineTotal(new BigDecimal("10.00"), 20));
    }
    @Test void rejectsInvalidQuantitiesAndPrices() {
        assertThrows(IllegalArgumentException.class, () -> OrderMath.lineTotal(new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class, () -> OrderMath.lineTotal(new BigDecimal("10.00"), 21));
        assertThrows(IllegalArgumentException.class, () -> OrderMath.lineTotal(new BigDecimal("-1.00"), 1));
    }
}
