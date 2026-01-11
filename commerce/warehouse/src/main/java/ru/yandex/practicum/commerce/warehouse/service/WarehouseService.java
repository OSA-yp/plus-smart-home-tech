package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.DimensionDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.entity.Dimension;
import ru.yandex.practicum.commerce.warehouse.entity.ProductInWarehouse;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.repository.ProductInWarehouseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WarehouseService {

    private final ProductInWarehouseRepository repository;
    private final WarehouseMapper mapper;

    public WarehouseService(ProductInWarehouseRepository repository, WarehouseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (repository.existsByProductId(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Product with ID " + request.getProductId() + " already exists in warehouse");
        }

        ProductInWarehouse product = new ProductInWarehouse();
        product.setProductId(request.getProductId());
        product.setDimension(mapper.toEntity(request.getDimension()));
        product.setWeight(request.getWeight());
        product.setFragile(request.getFragile() != null ? request.getFragile() : false);
        product.setQuantity(0L);

        repository.save(product);
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        ProductInWarehouse product = repository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Product with ID " + request.getProductId() + " not found in warehouse"));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        repository.save(product);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        List<String> insufficientProducts = new ArrayList<>();
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (var entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            ProductInWarehouse product = repository.findByProductId(productId)
                    .orElse(null);

            if (product == null) {
                insufficientProducts.add("Product " + productId + " not found in warehouse");
                continue;
            }

            if (product.getQuantity() < requiredQuantity) {
                insufficientProducts.add("Product " + productId + ": required " + requiredQuantity +
                        ", available " + product.getQuantity());
                continue;
            }

            // Рассчитываем вес и объем
            totalWeight += product.getWeight() * requiredQuantity;
            Dimension dim = product.getDimension();
            totalVolume += dim.getWidth() * dim.getHeight() * dim.getDepth() * requiredQuantity;

            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragile = true;
            }
        }

        if (!insufficientProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Insufficient products in warehouse: " + String.join("; ", insufficientProducts));
        }

        BookedProductsDto result = new BookedProductsDto();
        result.setDeliveryWeight(totalWeight);
        result.setDeliveryVolume(totalVolume);
        result.setFragile(hasFragile);
        return result;
    }
}
