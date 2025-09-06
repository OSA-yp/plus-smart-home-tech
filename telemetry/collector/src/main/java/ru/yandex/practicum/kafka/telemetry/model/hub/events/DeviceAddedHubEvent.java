package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceType;

@Getter
@Setter
public class DeviceAddedHubEvent extends BaseHubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }

    @NotBlank
    private String id; // ID устройства

    @NotNull
    private DeviceType deviceType; // Тип устройства из enum в Avro

}