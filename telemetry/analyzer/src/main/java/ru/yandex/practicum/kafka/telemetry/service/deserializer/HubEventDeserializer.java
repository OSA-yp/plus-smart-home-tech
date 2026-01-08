package ru.yandex.practicum.kafka.telemetry.service.deserializer;

import ru.yandex.practicum.kafka.telemetry.event.HubEvent;
import ru.yandex.practicum.kafka.telemetry.service.BaseAvroDeserializer;

public class HubEventDeserializer extends BaseAvroDeserializer<HubEvent> {
    public HubEventDeserializer() {
        super(HubEvent.getClassSchema());
    }
}
