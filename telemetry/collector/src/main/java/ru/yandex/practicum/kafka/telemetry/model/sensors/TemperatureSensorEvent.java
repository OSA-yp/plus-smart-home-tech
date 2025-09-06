package ru.yandex.practicum.kafka.telemetry.model.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemperatureSensorEvent extends SensorEvent {
    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @NotNull
    private Integer temperatureC;

    @NotNull
    private Integer temperatureF;

}