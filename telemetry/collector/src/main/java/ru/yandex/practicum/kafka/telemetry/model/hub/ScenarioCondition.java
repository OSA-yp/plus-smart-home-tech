package ru.yandex.practicum.kafka.telemetry.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioCondition {

    @NotBlank
    private String sensorId;

    @NotBlank
    private String type; // Тип условия из enum в Avro

    @NotBlank
    private String operation; // Операция из enum в Avro

    @NotNull
    private Integer value;

}