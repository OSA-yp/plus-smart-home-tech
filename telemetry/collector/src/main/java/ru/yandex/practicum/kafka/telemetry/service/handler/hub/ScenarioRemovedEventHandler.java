package ru.yandex.practicum.kafka.telemetry.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;

@Component
public class ScenarioRemovedEventHandler implements HubEventHandler {
    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEvent hubEvent) {
        // Логика обработки события удаления сценария
        // Пример: hubEvent.getPayload() будет содержать ScenarioRemovedEvent
        // ScenarioRemovedEvent scenarioEvent = (ScenarioRemovedEvent) hubEvent.getPayload();
        // System.out.println("Scenario removed: " + scenarioEvent.getName());
    }
}