package ru.yandex.practicum.commerce.shoppingstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductState;
import ru.yandex.practicum.commerce.shoppingstore.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByProductCategoryAndProductState(
            ProductCategory category,
            ProductState state,
            Pageable pageable
    );

    Optional<Product> findByProductIdAndProductState(UUID productId, ProductState state);

    Optional<Product> findByProductId(UUID productId);
}
