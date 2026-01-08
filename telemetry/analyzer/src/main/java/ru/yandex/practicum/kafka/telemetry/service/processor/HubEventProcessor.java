package ru.yandex.practicum.kafka.telemetry.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.entity.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.repository.*;
import ru.yandex.practicum.kafka.telemetry.service.deserializer.HubEventDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    @Value("${app.kafka.hub-events.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.hub-events.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.topics.hub-events}")
    private String hubEventsTopic;

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    private KafkaConsumer<String, HubEvent> consumer;

    @Override
    public void run() {
        try {
            // Инициализация Consumer
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

            consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(hubEventsTopic));

            log.info("HubEventProcessor started. Subscribed to topic: {}", hubEventsTopic);

            // Цикл опроса
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, HubEvent> records = consumer.poll(Duration.ofMillis(1000));
                if (!records.isEmpty()) {
                    log.info("Received {} hub event records from Kafka", records.count());
                }
                for (ConsumerRecord<String, HubEvent> record : records) {
                    HubEvent event = record.value();
                    if (event != null) {
                        try {
                            handleEvent(event);
                        } catch (Exception e) {
                            log.error("Error processing hub event: {}", e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                log.info("HubEventProcessor interrupted");
                return;
            }
            log.error("Error in HubEventProcessor", e);
        } finally {
            if (consumer != null) {
                consumer.close();
                log.info("HubEventProcessor consumer closed");
            }
        }
    }

    private void handleEvent(HubEvent event) {
        Object payload = event.getPayload();
        String hubId = event.getHubId() != null ? event.getHubId().toString() : null;
        
        log.info("Processing hub event: hubId={}, payloadType={}", hubId, 
                payload != null ? payload.getClass().getSimpleName() : "null");

        if (payload instanceof DeviceAddedEvent deviceAddedEvent) {
            handleDeviceAdded(hubId, deviceAddedEvent);
        } else if (payload instanceof DeviceRemovedEvent deviceRemovedEvent) {
            handleDeviceRemoved(hubId, deviceRemovedEvent);
        } else if (payload instanceof ScenarioAddedEvent scenarioAddedEvent) {
            handleScenarioAdded(hubId, scenarioAddedEvent);
        } else if (payload instanceof ScenarioRemovedEvent scenarioRemovedEvent) {
            handleScenarioRemoved(hubId, scenarioRemovedEvent);
        } else {
            log.warn("Unknown payload type: {}", payload != null ? payload.getClass() : "null");
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEvent event) {
        String sensorId = event.getId() != null ? event.getId().toString() : null;
        if (sensorId == null) {
            log.warn("DeviceAddedEvent has null id");
            return;
        }
        if (sensorRepository.existsById(sensorId)) {
            log.debug("Sensor {} already exists, ignoring", sensorId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("Device added: sensorId={}, hubId={}", sensorId, hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEvent event) {
        String sensorId = event.getId() != null ? event.getId().toString() : null;
        if (sensorId == null) {
            log.warn("DeviceRemovedEvent has null id");
            return;
        }
        sensorRepository.findById(sensorId).ifPresent(sensor -> {
            sensorRepository.delete(sensor);
            log.info("Device removed: sensorId={}, hubId={}", sensorId, hubId);
        });
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEvent event) {
        String scenarioName = event.getName() != null ? event.getName().toString() : null;
        if (scenarioName == null) {
            log.warn("ScenarioAddedEvent has null name");
            return;
        }
        
        // Проверяем, существует ли сценарий
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName)
                .orElseGet(() -> {
                    Scenario newScenario = new Scenario();
                    newScenario.setHubId(hubId);
                    newScenario.setName(scenarioName);
                    return scenarioRepository.save(newScenario);
                });

        // Инициализируем и удаляем старые условия и действия
        // Используем removeAll() вместо clear() для сохранения ссылки на оригинальную коллекцию
        if (scenario.getScenarioConditions() == null) {
            scenario.setScenarioConditions(new java.util.ArrayList<>());
        } else {
            scenario.getScenarioConditions().removeAll(new java.util.ArrayList<>(scenario.getScenarioConditions()));
        }
        if (scenario.getScenarioActions() == null) {
            scenario.setScenarioActions(new java.util.ArrayList<>());
        } else {
            scenario.getScenarioActions().removeAll(new java.util.ArrayList<>(scenario.getScenarioActions()));
        }

        // Сохраняем условия
        if (event.getConditions() != null) {
            for (Object conditionObj : event.getConditions()) {
                if (conditionObj instanceof ru.yandex.practicum.kafka.telemetry.event.ScenarioCondition) {
                    ru.yandex.practicum.kafka.telemetry.event.ScenarioCondition avroCondition = 
                            (ru.yandex.practicum.kafka.telemetry.event.ScenarioCondition) conditionObj;
                    String sensorId = avroCondition.getSensorId() != null ? avroCondition.getSensorId().toString() : null;
                    if (sensorId == null) {
                        log.warn("ScenarioCondition has null sensorId");
                        continue;
                    }
                    
                    // Проверяем, существует ли датчик
                    Sensor sensor = sensorRepository.findById(sensorId)
                            .orElseGet(() -> {
                                Sensor newSensor = new Sensor();
                                newSensor.setId(sensorId);
                                newSensor.setHubId(hubId);
                                return sensorRepository.save(newSensor);
                            });

                    // Создаем условие
                    Condition condition = new Condition();
                    if (avroCondition.getType() != null) {
                        condition.setType(avroCondition.getType().toString());
                    }
                    if (avroCondition.getOperation() != null) {
                        condition.setOperation(avroCondition.getOperation().toString());
                    }
                    
                    Object value = avroCondition.getValue();
                    if (value instanceof Integer) {
                        condition.setValue((Integer) value);
                    } else if (value instanceof Boolean) {
                        condition.setValue(((Boolean) value) ? 1 : 0);
                    }
                    condition = conditionRepository.save(condition);

                    // Связываем сценарий, датчик и условие
                    ru.yandex.practicum.kafka.telemetry.entity.ScenarioCondition scenarioCondition = 
                            new ru.yandex.practicum.kafka.telemetry.entity.ScenarioCondition();
                    scenarioCondition.setScenario(scenario);
                    scenarioCondition.setSensor(sensor);
                    scenarioCondition.setCondition(condition);
                    // Сохраняем через scenario для каскадного сохранения
                    if (scenario.getScenarioConditions() == null) {
                        scenario.setScenarioConditions(new java.util.ArrayList<>());
                    }
                    scenario.getScenarioConditions().add(scenarioCondition);
                }
            }
        }

        // Сохраняем действия
        if (event.getActions() != null) {
            for (Object actionObj : event.getActions()) {
                if (actionObj instanceof ru.yandex.practicum.kafka.telemetry.event.DeviceAction avroAction) {
                    String sensorId = avroAction.getSensorId() != null ? avroAction.getSensorId().toString() : null;
                    if (sensorId == null) {
                        log.warn("DeviceAction has null sensorId");
                        continue;
                    }
                    
                    // Проверяем, существует ли датчик
                    Sensor sensor = sensorRepository.findById(sensorId)
                            .orElseGet(() -> {
                                Sensor newSensor = new Sensor();
                                newSensor.setId(sensorId);
                                newSensor.setHubId(hubId);
                                return sensorRepository.save(newSensor);
                            });

                    // Создаем действие
                    Action action = new Action();
                    if (avroAction.getType() != null) {
                        action.setType(avroAction.getType().toString());
                    }
                    if (avroAction.getValue() != null) {
                        action.setValue(avroAction.getValue());
                    }
                    action = actionRepository.save(action);

                    // Связываем сценарий, датчик и действие
                    ScenarioAction scenarioAction = new ScenarioAction();
                    scenarioAction.setScenario(scenario);
                    scenarioAction.setSensor(sensor);
                    scenarioAction.setAction(action);
                    // Сохраняем через scenario для каскадного сохранения
                    if (scenario.getScenarioActions() == null) {
                        scenario.setScenarioActions(new java.util.ArrayList<>());
                    }
                    scenario.getScenarioActions().add(scenarioAction);
                }
            }
        }

        scenarioRepository.saveAndFlush(scenario);
        log.info("Scenario added: name={}, hubId={}, conditionsCount={}, actionsCount={}", 
                scenarioName, hubId, 
                scenario.getScenarioConditions() != null ? scenario.getScenarioConditions().size() : 0,
                scenario.getScenarioActions() != null ? scenario.getScenarioActions().size() : 0);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEvent event) {
        String scenarioName = event.getName() != null ? event.getName().toString() : null;
        if (scenarioName == null) {
            log.warn("ScenarioRemovedEvent has null name");
            return;
        }
        scenarioRepository.findByHubIdAndName(hubId, scenarioName).ifPresent(scenario -> {
            scenarioRepository.delete(scenario);
            log.info("Scenario removed: name={}, hubId={}", scenarioName, hubId);
        });
    }
}
