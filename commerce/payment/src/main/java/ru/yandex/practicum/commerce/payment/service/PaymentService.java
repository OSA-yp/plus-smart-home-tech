package ru.yandex.practicum.commerce.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductDto;
import ru.yandex.practicum.commerce.payment.entity.Payment;
import ru.yandex.practicum.commerce.payment.entity.PaymentState;
import ru.yandex.practicum.commerce.payment.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentService(PaymentRepository paymentRepository,
                         PaymentMapper paymentMapper,
                         ShoppingStoreClient shoppingStoreClient,
                         OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Transactional(readOnly = true)
    public Double productCost(OrderDto order) {
        if (order == null || order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Order or products are missing");
        }

        double totalCost = 0.0;
        for (var entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);
            if (product == null || product.getPrice() == null) {
                throw new NotEnoughInfoInOrderToCalculateException(
                        "Product information not available for product: " + productId);
            }

            totalCost += product.getPrice().doubleValue() * quantity;
        }

        return totalCost;
    }

    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto order) {
        if (order == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Order is missing");
        }

        // Сначала рассчитываем стоимость товаров
        Double productPrice = productCost(order);

        // НДС составляет 10% от стоимости товаров
        Double feeTotal = productPrice * 0.1;

        // Стоимость товаров с НДС
        Double productPriceWithTax = productPrice + feeTotal;

        // Получаем стоимость доставки из заказа
        Double deliveryPrice = order.getDeliveryPrice();
        if (deliveryPrice == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Delivery price is missing in order");
        }

        // Итоговая стоимость = стоимость товаров с НДС + стоимость доставки
        return productPriceWithTax + deliveryPrice;
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        if (order == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Order is missing");
        }

        // Рассчитываем стоимость товаров
        Double productPrice = productCost(order);

        // Получаем стоимость доставки
        Double deliveryPrice = order.getDeliveryPrice();
        if (deliveryPrice == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Delivery price is missing in order");
        }

        // Рассчитываем общую стоимость
        Double totalPrice = getTotalCost(order);

        // НДС составляет 10% от стоимости товаров
        Double feeTotal = productPrice * 0.1;

        // Создаем Payment
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(order.getOrderId());
        payment.setProductPrice(productPrice);
        payment.setDeliveryPrice(deliveryPrice);
        payment.setTotalPrice(totalPrice);
        payment.setFeeTotal(feeTotal);
        payment.setPaymentState(PaymentState.PENDING);

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toDto(saved);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentId));

        payment.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        // Вызываем изменение статуса заказа на оплачен
        orderClient.paymentSuccess(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Payment not found: " + paymentId));

        payment.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(payment);

        // Вызываем изменение статуса заказа
        orderClient.paymentFailed(payment.getOrderId());
    }
}
