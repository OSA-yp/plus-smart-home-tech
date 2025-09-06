package ru.yandex.practicum.kafka.telemetry.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.service.handler.sensors.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/events") // Базовый путь для обоих эндпоинтов
public class CollectorController {

    private final Map<HubEventType, HubEventHandler> hubEventHandlers;
    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final ObjectMapper objectMapper;

    public CollectorController(Set<HubEventHandler> hubEventHandlers,
                               Set<SensorEventHandler> sensorEventHandlers, ObjectMapper objectMapper) {

        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.objectMapper = objectMapper;
    }

        @PostMapping("/sensors")
        public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {

            try {
                log.info(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize SensorEvent to JSON", e);
            }

            if (sensorEventHandlers.containsKey(event.getType())) {
                sensorEventHandlers.get(event.getType()).handle(event);
            } else {
                throw new IllegalArgumentException("Can't find handler for " + event.getType());
            }
            return ResponseEntity.ok().build();
        }



    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {

        try {
            log.info(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize HubEvent to JSON", e);
        }

        if (hubEventHandlers.containsKey(event.getType())) {
            hubEventHandlers.get(event.getType()).handle(event);
        } else {
            throw new IllegalArgumentException("Can't find handler for " + event.getType());
        }
        return ResponseEntity.ok().build();
    }
}