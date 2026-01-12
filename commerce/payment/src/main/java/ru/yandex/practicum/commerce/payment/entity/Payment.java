package ru.yandex.practicum.commerce.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "payment_id", unique = true, nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_price", nullable = false)
    private Double productPrice;

    @Column(name = "delivery_price", nullable = false)
    private Double deliveryPrice;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "fee_total", nullable = false)
    private Double feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false)
    private PaymentState paymentState;
}
