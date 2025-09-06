package ru.yandex.practicum.kafka.telemetry.model.hub;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceAction {

    @NotBlank
    private String sensorId;

    @NotBlank
    private String type; // Тип действия из enum в Avro

    private Integer value; // Может быть null

}