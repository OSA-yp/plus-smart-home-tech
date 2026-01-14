package ru.yandex.practicum.commerce.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.warn("Warehouse service is unavailable. Fallback called for newProductInWarehouse");
        throw new RuntimeException("Warehouse service is temporarily unavailable");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        log.warn("Warehouse service is unavailable. Fallback called for checkProductQuantityEnoughForShoppingCart");
        throw new RuntimeException("Warehouse service is temporarily unavailable. Cannot check product availability.");
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.warn("Warehouse service is unavailable. Fallback called for addProductToWarehouse");
        throw new RuntimeException("Warehouse service is temporarily unavailable");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.warn("Warehouse service is unavailable. Fallback called for getWarehouseAddress");
        throw new RuntimeException("Warehouse service is temporarily unavailable");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.warn("Warehouse service is unavailable. Fallback called for assemblyProductsForOrder");
        throw new RuntimeException("Warehouse service is temporarily unavailable. Cannot assemble products for order.");
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.warn("Warehouse service is unavailable. Fallback called for shippedToDelivery");
        throw new RuntimeException("Warehouse service is temporarily unavailable");
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        log.warn("Warehouse service is unavailable. Fallback called for acceptReturn");
        throw new RuntimeException("Warehouse service is temporarily unavailable. Cannot accept product return.");
    }
}
