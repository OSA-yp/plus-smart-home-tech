package ru.yandex.practicum.commerce.interaction.dto.order;

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
public class ProductReturnRequest {
    private UUID orderId;

    @NotNull(message = "Products map is required")
    private Map<UUID, Long> products = new HashMap<>();
}
