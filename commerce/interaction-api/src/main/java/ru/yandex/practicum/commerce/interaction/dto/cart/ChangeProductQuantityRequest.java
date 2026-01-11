package ru.yandex.practicum.commerce.interaction.dto.cart;

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
public class ChangeProductQuantityRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "New quantity is required")
    private Long newQuantity;
}
