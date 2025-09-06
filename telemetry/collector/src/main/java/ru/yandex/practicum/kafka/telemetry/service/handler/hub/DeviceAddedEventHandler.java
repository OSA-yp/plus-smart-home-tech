package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.DeviceAddedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {

    private final EventsSender sender;

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    public void handle(BaseHubEvent hubEvent) {
        if (!(hubEvent instanceof DeviceAddedHubEvent deviceAddedHubEvent)) {
            throw new IllegalArgumentException("Expected DeviceAddedEvent");
        }

        // Avro-объект для данных датчика
        DeviceAddedEvent payload = new DeviceAddedEvent();
        payload.setId(deviceAddedHubEvent.getId());
        payload.setType(deviceAddedHubEvent.getDeviceType());


        sender.sendHubEvent(deviceAddedHubEvent, payload);
    }
}
