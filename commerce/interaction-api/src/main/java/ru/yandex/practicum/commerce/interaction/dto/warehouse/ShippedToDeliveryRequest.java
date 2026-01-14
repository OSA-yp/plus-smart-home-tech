package ru.yandex.practicum.commerce.interaction.dto.warehouse;

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
public class ShippedToDeliveryRequest {
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Delivery ID is required")
    private UUID deliveryId;
}
