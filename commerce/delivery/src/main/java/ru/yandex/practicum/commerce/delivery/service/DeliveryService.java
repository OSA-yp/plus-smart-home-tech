package ru.yandex.practicum.commerce.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.delivery.entity.Delivery;
import ru.yandex.practicum.commerce.delivery.entity.DeliveryState;
import ru.yandex.practicum.commerce.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;

import java.util.UUID;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    public DeliveryService(DeliveryRepository deliveryRepository,
                          DeliveryMapper deliveryMapper,
                          WarehouseClient warehouseClient,
                          OrderClient orderClient) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryMapper = deliveryMapper;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
    }

    private Delivery findDeliveryByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Delivery not found for order: " + orderId));
    }

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        // Получаем адрес склада
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        deliveryDto.setFromAddress(warehouseAddress);

        Delivery delivery = deliveryMapper.toEntity(deliveryDto);
        delivery.setDeliveryId(UUID.randomUUID());
        delivery.setDeliveryState(DeliveryState.CREATED);

        Delivery saved = deliveryRepository.save(delivery);
        return deliveryMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required for delivery cost calculation");
        }

        // Находим доставку по orderId
        Delivery delivery = findDeliveryByOrderId(order.getOrderId());

        // Базовая стоимость
        double cost = 5.0;

        // Умножение на коэффициент склада
        String warehouseStreet = delivery.getFromAddress().getStreet();
        if (warehouseStreet != null) {
            if (warehouseStreet.contains("ADDRESS_1")) {
                cost = cost * 1.0;
            } else if (warehouseStreet.contains("ADDRESS_2")) {
                cost = cost * 2.0;
            }
        }
        // Складываем с базовой стоимостью
        cost = cost + 5.0;

        // Учет хрупкости
        if (Boolean.TRUE.equals(order.getFragile())) {
            cost = cost + (cost * 0.2);
        }

        // Добавляем вес
        if (order.getDeliveryWeight() != null) {
            cost = cost + (order.getDeliveryWeight() * 0.3);
        }

        // Добавляем объем
        if (order.getDeliveryVolume() != null) {
            cost = cost + (order.getDeliveryVolume() * 0.2);
        }

        // Учет адреса доставки
        String deliveryStreet = delivery.getToAddress().getStreet();
        if (deliveryStreet != null && warehouseStreet != null) {
            if (!deliveryStreet.equals(warehouseStreet)) {
                cost = cost + (cost * 0.2);
            }
        }

        return cost;
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        // Изменить статус заказа на ASSEMBLED
        orderClient.assembly(orderId);

        // Вызвать warehouse для передачи в доставку
        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(orderId);
        request.setDeliveryId(delivery.getDeliveryId());
        warehouseClient.shippedToDelivery(request);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        // Изменить статус заказа на DELIVERED
        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = findDeliveryByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        // Изменить статус заказа на DELIVERY_FAILED
        orderClient.deliveryFailed(orderId);
    }
}
