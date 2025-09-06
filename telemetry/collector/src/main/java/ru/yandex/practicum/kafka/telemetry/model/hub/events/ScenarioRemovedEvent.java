package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioRemovedEvent extends HubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @NotBlank
    @Size(min = 3)
    private String name;

}