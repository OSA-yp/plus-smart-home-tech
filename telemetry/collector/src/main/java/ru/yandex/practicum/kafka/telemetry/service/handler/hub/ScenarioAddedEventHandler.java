package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.ScenarioAddedHubEvent;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final EventsSender sender;

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public void handle(BaseHubEvent hubEvent) {
        if (!(hubEvent instanceof ScenarioAddedHubEvent scenarioAddedHubEvent)) {
            throw new IllegalArgumentException("Expected ScenarioAddedHubEvent");
        }

        // Avro-объект для данных датчика
        ScenarioAddedEvent payload = new ScenarioAddedEvent();
        payload.setName(scenarioAddedHubEvent.getName());
        payload.setConditions(scenarioAddedHubEvent.getConditions());
        payload.setActions(scenarioAddedHubEvent.getActions());


        sender.sendHubEvent(scenarioAddedHubEvent, payload);
    }
}