package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.model.sensors.LightSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler {

    private final EventsSender sender;


    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent) {
        if (!(sensorEvent instanceof LightSensorEvent lightEvent)) {
            throw new IllegalArgumentException("Expected LightSensorEvent");
        }

        // Avro-объект для данных датчика
        LightSensorAvro payload = new LightSensorAvro();
        payload.setLinkQuality(lightEvent.getLinkQuality());
        payload.setLuminosity(lightEvent.getLuminosity());

        sender.sendSensorEvent(lightEvent, payload);
    }
}
