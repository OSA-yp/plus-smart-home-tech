package ru.yandex.practicum.kafka.telemetry.service.handler.sensors;

import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;

public interface SensorEventHandler {

    SensorEventType getMessageType();

    void handle(SensorEvent sensorEvent);

}
