package ru.yandex.practicum.commerce.interaction.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewProductInWarehouseRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Dimension is required")
    @Valid
    private DimensionDto dimension;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1")
    private Double weight;

    private Boolean fragile;
}
