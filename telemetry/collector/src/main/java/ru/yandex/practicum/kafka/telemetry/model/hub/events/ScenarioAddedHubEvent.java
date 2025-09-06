package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAction;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioCondition;

import java.util.List;

@Getter
@Setter
public class ScenarioAddedHubEvent extends BaseHubEvent {
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