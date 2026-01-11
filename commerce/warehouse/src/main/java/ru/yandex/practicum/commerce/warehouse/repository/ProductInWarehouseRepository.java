package ru.yandex.practicum.commerce.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.warehouse.entity.ProductInWarehouse;

import java.util.Optional;
import java.util.UUID;

public interface ProductInWarehouseRepository extends JpaRepository<ProductInWarehouse, UUID> {
    Optional<ProductInWarehouse> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}
