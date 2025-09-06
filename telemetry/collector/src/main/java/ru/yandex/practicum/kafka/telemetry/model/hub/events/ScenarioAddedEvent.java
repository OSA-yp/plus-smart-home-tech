package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.model.hub.DeviceAction;
import ru.yandex.practicum.kafka.telemetry.model.hub.ScenarioCondition;

import java.util.List;

@Getter
@Setter
public class ScenarioAddedEvent extends HubEvent {
    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @NotBlank
    @Size(min = 3)
    private String name;

    @NotEmpty
    private List<ScenarioCondition> conditions;

    @NotEmpty
    private List<DeviceAction> actions;

}