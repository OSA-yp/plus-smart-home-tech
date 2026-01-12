package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentController implements PaymentClient {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    @PostMapping("/payment")
    public PaymentDto payment(@RequestBody @Valid OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    @PostMapping("/payment/totalCost")
    public Double getTotalCost(@RequestBody @Valid OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    @PostMapping("/payment/productCost")
    public Double productCost(@RequestBody @Valid OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    @PostMapping("/payment/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    @PostMapping("/payment/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}
