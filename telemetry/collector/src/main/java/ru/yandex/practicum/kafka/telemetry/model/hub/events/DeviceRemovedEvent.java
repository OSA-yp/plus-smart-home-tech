package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRemovedEvent extends HubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @NotBlank
    private String id; // ID устройства
}