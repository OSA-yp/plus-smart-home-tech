package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

public interface HubEventHandler {

    HubEventType getMessageType();

    void handle(HubEvent hubEvent);
}
