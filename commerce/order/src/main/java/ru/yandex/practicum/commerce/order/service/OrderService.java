package ru.yandex.practicum.commerce.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.order.entity.Order;
import ru.yandex.practicum.commerce.order.entity.OrderState;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final ShoppingCartClient shoppingCartClient;

    public OrderService(OrderRepository orderRepository,
                       OrderMapper orderMapper,
                       WarehouseClient warehouseClient,
                       DeliveryClient deliveryClient,
                       PaymentClient paymentClient,
                       ShoppingCartClient shoppingCartClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.warehouseClient = warehouseClient;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
        this.shoppingCartClient = shoppingCartClient;
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }
    }

    private Order findOrderByOrderId(UUID orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Order not found: " + orderId));
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        // Проверка наличия товаров на складе
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());
        } catch (Exception e) {
            throw new NoSpecifiedProductInWarehouseException(
                    "Products not available in warehouse: " + e.getMessage());
        }

        // Получение информации о весе, объеме и хрупкости
        BookedProductsDto bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(
                request.getShoppingCart());

        // Создание заказа сначала (нужен orderId для доставки)
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        order.setProducts(request.getShoppingCart().getProducts());
        order.setOrderState(OrderState.NEW);
        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());

        // Сохраняем заказ временно для получения ID
        Order tempOrder = orderRepository.save(order);

        // Получение адреса склада
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        // Создание доставки с правильным orderId
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setOrderId(tempOrder.getOrderId());
        deliveryDto.setFromAddress(warehouseAddress);
        deliveryDto.setToAddress(request.getDeliveryAddress());
        deliveryDto.setDeliveryState(ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState.CREATED);

        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryDto);

        // Обновляем заказ с deliveryId
        tempOrder.setDeliveryId(createdDelivery.getDeliveryId());

        // Расчет стоимости доставки
        OrderDto tempOrderDto = orderMapper.toDto(tempOrder);
        Double deliveryCost = deliveryClient.deliveryCost(tempOrderDto);
        tempOrder.setDeliveryPrice(deliveryCost);

        // Расчет стоимости товаров
        Double productCost = paymentClient.productCost(tempOrderDto);
        tempOrder.setProductPrice(productCost);

        // Расчет общей стоимости
        OrderDto orderDtoForTotal = orderMapper.toDto(tempOrder);
        Double totalCost = paymentClient.getTotalCost(orderDtoForTotal);
        tempOrder.setTotalPrice(totalCost);

        Order saved = orderRepository.save(tempOrder);
        return orderMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);

        // Получаем корзину пользователя
        var shoppingCart = shoppingCartClient.getShoppingCart(username);
        UUID shoppingCartId = shoppingCart.getShoppingCartId();

        List<Order> orders = orderRepository.findByShoppingCartId(shoppingCartId);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        OrderDto orderDto = orderMapper.toDto(order);
        Double deliveryCost = deliveryClient.deliveryCost(orderDto);
        order.setDeliveryPrice(deliveryCost);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        OrderDto orderDto = orderMapper.toDto(order);
        Double totalCost = paymentClient.getTotalCost(orderDto);
        order.setTotalPrice(totalCost);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        OrderDto orderDto = orderMapper.toDto(order);
        var paymentDto = paymentClient.payment(orderDto);
        order.setPaymentId(paymentDto.getPaymentId());
        order.setOrderState(OrderState.ON_PAYMENT);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        // Вызов warehouse для сборки
        ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest request =
                new ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest();
        request.setOrderId(orderId);
        request.setProducts(order.getProducts());
        warehouseClient.assemblyProductsForOrder(request);
        order.setOrderState(OrderState.ASSEMBLED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.DELIVERED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.COMPLETED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.PAYMENT_FAILED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.ASSEMBLY_FAILED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.DELIVERY_FAILED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = findOrderByOrderId(request.getOrderId());
        // Возврат товаров на склад
        warehouseClient.acceptReturn(request.getProducts());
        order.setOrderState(OrderState.PRODUCT_RETURNED);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto paymentSuccess(UUID orderId) {
        Order order = findOrderByOrderId(orderId);
        order.setOrderState(OrderState.PAID);
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }
}
