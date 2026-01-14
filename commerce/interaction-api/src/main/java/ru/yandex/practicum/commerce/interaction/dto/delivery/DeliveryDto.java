package ru.yandex.practicum.commerce.interaction.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    @NotNull(message = "Delivery ID is required")
    private UUID deliveryId;

    @NotNull(message = "From address is required")
    private AddressDto fromAddress;

    @NotNull(message = "To address is required")
    private AddressDto toAddress;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Delivery state is required")
    private DeliveryState deliveryState;
}
