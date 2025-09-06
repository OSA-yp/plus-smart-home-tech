package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

public interface HubEventHandler {

    HubEventType getMessageType();

    void handle(BaseHubEvent hubEvent);
}
