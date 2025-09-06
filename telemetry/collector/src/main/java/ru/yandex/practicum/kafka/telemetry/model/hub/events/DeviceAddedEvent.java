package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceAddedEvent extends HubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    @NotBlank
    private String id; // ID устройства

    @NotNull
    private String deviceType; // Тип устройства из enum в Avro

}