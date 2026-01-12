package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.delivery.entity.Address;
import ru.yandex.practicum.commerce.delivery.entity.Delivery;
import ru.yandex.practicum.commerce.delivery.entity.DeliveryState;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setOrderId(delivery.getOrderId());
        dto.setFromAddress(toAddressDto(delivery.getFromAddress()));
        dto.setToAddress(toAddressDto(delivery.getToAddress()));
        dto.setDeliveryState(convertDeliveryState(delivery.getDeliveryState()));
        return dto;
    }

    public Delivery toEntity(DeliveryDto dto) {
        if (dto == null) {
            return null;
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setOrderId(dto.getOrderId());
        delivery.setFromAddress(toAddress(dto.getFromAddress()));
        delivery.setToAddress(toAddress(dto.getToAddress()));
        delivery.setDeliveryState(convertDeliveryState(dto.getDeliveryState()));
        return delivery;
    }

    private AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        AddressDto dto = new AddressDto();
        dto.setCountry(address.getCountry());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());
        dto.setFlat(address.getFlat());
        return dto;
    }

    private Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        Address address = new Address();
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setHouse(dto.getHouse());
        address.setFlat(dto.getFlat());
        return address;
    }

    private DeliveryState convertDeliveryState(ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState state) {
        if (state == null) {
            return null;
        }
        return DeliveryState.valueOf(state.name());
    }

    private ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState convertDeliveryState(DeliveryState state) {
        if (state == null) {
            return null;
        }
        return ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState.valueOf(state.name());
    }
}
