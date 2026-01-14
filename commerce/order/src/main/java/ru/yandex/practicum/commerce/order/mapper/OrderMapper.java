package ru.yandex.practicum.commerce.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.order.entity.Order;
import ru.yandex.practicum.commerce.order.entity.OrderState;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setProducts(order.getProducts());
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setState(convertOrderState(order.getOrderState()));
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.getFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setProductPrice(order.getProductPrice());
        return dto;
    }

    public Order toEntity(OrderDto dto) {
        if (dto == null) {
            return null;
        }

        Order order = new Order();
        order.setOrderId(dto.getOrderId());
        order.setShoppingCartId(dto.getShoppingCartId());
        order.setProducts(dto.getProducts());
        order.setPaymentId(dto.getPaymentId());
        order.setDeliveryId(dto.getDeliveryId());
        order.setOrderState(convertOrderState(dto.getState()));
        order.setDeliveryWeight(dto.getDeliveryWeight());
        order.setDeliveryVolume(dto.getDeliveryVolume());
        order.setFragile(dto.getFragile());
        order.setTotalPrice(dto.getTotalPrice());
        order.setDeliveryPrice(dto.getDeliveryPrice());
        order.setProductPrice(dto.getProductPrice());
        return order;
    }

    private OrderState convertOrderState(ru.yandex.practicum.commerce.interaction.dto.order.OrderState state) {
        if (state == null) {
            return null;
        }
        return OrderState.valueOf(state.name());
    }

    private ru.yandex.practicum.commerce.interaction.dto.order.OrderState convertOrderState(OrderState state) {
        if (state == null) {
            return null;
        }
        return ru.yandex.practicum.commerce.interaction.dto.order.OrderState.valueOf(state.name());
    }
}
