package ru.yandex.practicum.kafka.telemetry.model.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClimateSensorEvent extends SensorEvent {
    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @NotNull
    private Integer temperatureC;

    @NotNull
    private Integer humidity;

    @NotNull
    private Integer co2Level;
}