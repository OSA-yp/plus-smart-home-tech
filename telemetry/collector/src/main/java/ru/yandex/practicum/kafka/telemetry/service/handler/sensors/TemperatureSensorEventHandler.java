package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.model.sensors.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {

    private final EventsSender sender;

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent) {
        if (!(sensorEvent instanceof TemperatureSensorEvent temperatureSensorEvent)) {
            throw new IllegalArgumentException("Expected TemperatureSensorEvent");
        }

        // Avro-объект для данных датчика
        TemperatureSensorAvro payload = new TemperatureSensorAvro();
        payload.setTemperatureC(temperatureSensorEvent.getTemperatureC());
        payload.setTemperatureF(temperatureSensorEvent.getTemperatureF());

        sender.sendSensorEvent(temperatureSensorEvent, payload);
    }
}