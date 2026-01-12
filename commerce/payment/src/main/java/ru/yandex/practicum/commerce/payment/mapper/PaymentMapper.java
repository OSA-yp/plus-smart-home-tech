package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.entity.Payment;
import ru.yandex.practicum.commerce.payment.entity.PaymentState;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setTotalPayment(payment.getTotalPrice());
        dto.setDeliveryTotal(payment.getDeliveryPrice());
        dto.setFeeTotal(payment.getFeeTotal());
        return dto;
    }

    public PaymentState convertPaymentState(ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState state) {
        if (state == null) {
            return null;
        }
        return PaymentState.valueOf(state.name());
    }

    public ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState convertPaymentState(PaymentState state) {
        if (state == null) {
            return null;
        }
        return ru.yandex.practicum.commerce.interaction.dto.payment.PaymentState.valueOf(state.name());
    }
}
