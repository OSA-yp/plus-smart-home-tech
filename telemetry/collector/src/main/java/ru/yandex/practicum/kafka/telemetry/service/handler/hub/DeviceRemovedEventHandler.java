package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

@Component
public class DeviceRemovedEventHandler implements HubEventHandler {
    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEvent hubEvent) {
        // Логика обработки события удаления устройства
        // Пример: hubEvent.getPayload() будет содержать DeviceRemovedEvent
        // System.out.println("Device removed: " + ((DeviceRemovedEvent) hubEvent.getPayload()).getId());
    }
}