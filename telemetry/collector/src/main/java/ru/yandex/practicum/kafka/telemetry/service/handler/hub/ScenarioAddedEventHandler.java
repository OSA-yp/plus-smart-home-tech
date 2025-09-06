package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

@Component
public class ScenarioAddedEventHandler implements HubEventHandler {
    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEvent hubEvent) {
        // Логика обработки события добавления сценария
        // Пример: hubEvent.getPayload() будет содержать ScenarioAddedEvent
        // ScenarioAddedEvent scenarioEvent = (ScenarioAddedEvent) hubEvent.getPayload();
        // System.out.println("Scenario added: " + scenarioEvent.getName());
    }
}