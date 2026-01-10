package ru.yandex.practicum.commerce.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "products_in_warehouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductInWarehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "product_id", unique = true, nullable = false)
    private UUID productId;

    @Embedded
    private Dimension dimension;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "quantity", nullable = false)
    private Long quantity;
}
