package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.model.sensors.ClimateSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {

    private final EventsSender sender;

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent) {
        if (!(sensorEvent instanceof ClimateSensorEvent climateSensorEvent)) {
            throw new IllegalArgumentException("Expected ClimateSensorEvent");
        }

        // Avro-объект для данных датчика
        ClimateSensorAvro payload = new ClimateSensorAvro();
        payload.setTemperatureC(climateSensorEvent.getTemperatureC());
        payload.setHumidity(climateSensorEvent.getHumidity());
        payload.setCo2Level(climateSensorEvent.getCo2Level());


        sender.sendSensorEvent(climateSensorEvent, payload);
    }
}