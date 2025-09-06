package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.service.EventsSender;

@Component
@AllArgsConstructor
public class SwitchSensorEventHandler implements SensorEventHandler {

    private final EventsSender sender;

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent sensorEvent) {
        if (!(sensorEvent instanceof SwitchSensorEvent switchSensorEvent)) {
            throw new IllegalArgumentException("Expected SwitchSensorEvent");
        }

        // Avro-объект для данных датчика
        SwitchSensorAvro payload = new SwitchSensorAvro();
        payload.setState(switchSensorEvent.getState());

        sender.sendSensorEvent(switchSensorEvent, payload);
    }
}