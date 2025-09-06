package ru.yandex.practicum.kafka.telemetry.model.sensors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LightSensorEvent extends SensorEvent {
    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    private Integer linkQuality;
    private Integer luminosity;

}