package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

import java.security.SecureRandom;

@Service
public class AddressService {
    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private final String currentAddress;

    public AddressService() {
        SecureRandom random = new SecureRandom();
        this.currentAddress = ADDRESSES[random.nextInt(ADDRESSES.length)];
    }

    public AddressDto getWarehouseAddress() {
        AddressDto dto = new AddressDto();
        dto.setCountry(currentAddress);
        dto.setCity(currentAddress);
        dto.setStreet(currentAddress);
        dto.setHouse(currentAddress);
        dto.setFlat(currentAddress);
        return dto;
    }
}
