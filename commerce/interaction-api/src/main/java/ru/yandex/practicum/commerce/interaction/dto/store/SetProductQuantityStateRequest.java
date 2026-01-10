package ru.yandex.practicum.commerce.interaction.dto.store;

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
public class SetProductQuantityStateRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity state is required")
    private QuantityState quantityState;
}
