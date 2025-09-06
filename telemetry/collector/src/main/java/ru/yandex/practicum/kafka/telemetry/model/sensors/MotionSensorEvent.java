package ru.yandex.practicum.kafka.telemetry.model.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MotionSensorEvent extends SensorEvent {
    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @NotNull
    private Integer linkQuality;

    @NotNull
    private Boolean motion;

    @NotNull
    private Integer voltage;

}