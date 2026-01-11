package ru.yandex.practicum.commerce.interaction.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID productId;

    @NotNull(message = "Product name is required")
    private String productName;

    @NotNull(message = "Description is required")
    private String description;

    private String imageSrc;

    @NotNull(message = "Quantity state is required")
    private QuantityState quantityState;

    @NotNull(message = "Product state is required")
    private ProductState productState;

    private ProductCategory productCategory;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be at least 1")
    private BigDecimal price;
}
