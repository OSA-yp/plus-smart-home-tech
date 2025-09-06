package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.ScenarioRemovedHubEvent;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {

    private final EventsSender sender;

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    public void handle(BaseHubEvent hubEvent) {
        if (!(hubEvent instanceof ScenarioRemovedHubEvent scenarioRemovedHubEvent)) {
            throw new IllegalArgumentException("Expected ScenarioAddedHubEvent");
        }

        // Avro-объект для данных датчика
        ScenarioRemovedEvent payload = new ScenarioRemovedEvent();
        payload.setName(scenarioRemovedHubEvent.getName());

        sender.sendHubEvent(scenarioRemovedHubEvent, payload);
    }
}