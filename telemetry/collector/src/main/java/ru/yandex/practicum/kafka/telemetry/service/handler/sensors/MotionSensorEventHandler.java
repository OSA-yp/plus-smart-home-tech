package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.model.sensors.MotionSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {

    private final EventsSender sender;

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent) {
        if (!(sensorEvent instanceof MotionSensorEvent motionSensorEvent)) {
            throw new IllegalArgumentException("Expected MotionSensorEvent");
        }

        // Avro-объект для данных датчика
        MotionSensorAvro payload = new MotionSensorAvro();
        payload.setLinkQuality(motionSensorEvent.getLinkQuality());
        payload.setMotion(motionSensorEvent.getMotion());
        payload.setVoltage(motionSensorEvent.getVoltage());

        sender.sendSensorEvent(motionSensorEvent, payload);
    }
}