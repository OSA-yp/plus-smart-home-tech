package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interaction.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {
    @NotNull(message = "Shopping cart is required")
    @Valid
    private ShoppingCartDto shoppingCart;

    @NotNull(message = "Delivery address is required")
    @Valid
    private AddressDto deliveryAddress;
}
