package ru.yandex.practicum.commerce.delivery.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    @PutMapping("/delivery")
    public DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto delivery) {
        return deliveryService.planDelivery(delivery);
    }

    @Override
    @PostMapping("/delivery/cost")
    public Double deliveryCost(@RequestBody @Valid OrderDto order) {
        return deliveryService.deliveryCost(order);
    }

    @Override
    @PostMapping("/delivery/successful")
    public void deliverySuccessful(@RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    @PostMapping("/delivery/picked")
    public void deliveryPicked(@RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    @PostMapping("/delivery/failed")
    public void deliveryFailed(@RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }
}
