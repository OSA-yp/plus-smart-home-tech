package ru.yandex.practicum.commerce.interaction.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {
    @NotNull(message = "Shopping cart ID is required")
    private UUID shoppingCartId;

    @NotNull(message = "Products map is required")
    private Map<UUID, Long> products = new HashMap<>();
}
