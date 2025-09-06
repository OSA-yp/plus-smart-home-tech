package ru.yandex.practicum.kafka.telemetry.model.hub.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        defaultImpl = BaseHubEvent.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedHubEvent.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedHubEvent.class, name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedHubEvent.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemovedHubEvent.class, name = "SCENARIO_REMOVED")
})
@Getter
@Setter
@ToString
public abstract class BaseHubEvent {

    @NotBlank
    private String hubId;

    private Instant timestamp = Instant.now(); // По умолчанию текущее время

    @NotNull
    public abstract HubEventType getType();

}