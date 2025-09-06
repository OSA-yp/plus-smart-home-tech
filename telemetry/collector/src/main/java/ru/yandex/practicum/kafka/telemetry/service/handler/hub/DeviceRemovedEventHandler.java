package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.DeviceRemovedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {

    private final EventsSender sender;

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    public void handle(BaseHubEvent hubEvent) {
        if (!(hubEvent instanceof DeviceRemovedHubEvent deviceRemovedHubEvent)) {
            throw new IllegalArgumentException("Expected DeviceRemovedHubEvent");
        }

        // Avro-объект для данных датчика
        DeviceRemovedHubEvent payload = new DeviceRemovedHubEvent();
        payload.setId(deviceRemovedHubEvent.getId());

        sender.sendHubEvent(deviceRemovedHubEvent, payload);
    }
}