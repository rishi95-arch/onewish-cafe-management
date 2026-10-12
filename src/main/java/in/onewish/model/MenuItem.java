package in.onewish.model;

import java.math.BigDecimal;

public record MenuItem(long id, String name, String description, BigDecimal price, long categoryId, String category,
                       String imageUrl, String foodType, boolean available) {}
