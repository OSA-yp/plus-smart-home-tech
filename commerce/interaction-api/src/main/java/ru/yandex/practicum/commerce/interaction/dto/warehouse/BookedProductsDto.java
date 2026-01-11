package ru.yandex.practicum.commerce.interaction.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookedProductsDto {
    @NotNull(message = "Delivery weight is required")
    private Double deliveryWeight;

    @NotNull(message = "Delivery volume is required")
    private Double deliveryVolume;

    @NotNull(message = "Fragile flag is required")
    private Boolean fragile;
}
