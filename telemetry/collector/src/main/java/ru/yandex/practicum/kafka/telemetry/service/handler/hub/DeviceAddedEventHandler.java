package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

@Component
public class DeviceAddedEventHandler implements HubEventHandler {
    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEvent hubEvent) {

    }
}
