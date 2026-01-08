package ru.yandex.practicum.kafka.telemetry.service.processor;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.entity.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.service.deserializer.SnapshotDeserializer;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    @Value("${app.kafka.snapshots.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.snapshots.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.topics.snapshots}")
    private String snapshotsTopic;

    private final ScenarioRepository scenarioRepository;
    private final TransactionTemplate transactionTemplate;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    public void start() {
        try {
            // Инициализация Consumer
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SnapshotDeserializer.class);
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

            consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(snapshotsTopic));

            log.info("SnapshotProcessor started. Subscribed to topic: {}", snapshotsTopic);

            // Цикл обработки снапшотов
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(java.time.Duration.ofMillis(1000));
                if (!records.isEmpty()) {
                    log.info("Received {} snapshot records from Kafka", records.count());
                }
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    SensorsSnapshotAvro snapshot = record.value();
                    if (snapshot != null) {
                        try {
                            transactionTemplate.execute(status -> {
                                processSnapshot(snapshot);
                                return null;
                            });
                        } catch (Exception e) {
                            log.error("Error processing snapshot: {}", e.getMessage(), e);
                        }
                    }
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                    log.debug("Committed {} snapshot records", records.count());
                }
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                log.info("SnapshotProcessor interrupted");
                return;
            }
            log.error("Error in SnapshotProcessor", e);
        } finally {
            if (consumer != null) {
                consumer.close();
                log.info("SnapshotProcessor consumer closed");
            }
        }
    }

    protected void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId() != null ? snapshot.getHubId().toString() : null;
        if (hubId == null) {
            log.warn("Snapshot has null hubId");
            return;
        }
        
        log.info("Processing snapshot for hubId: {}, timestamp: {}", hubId, snapshot.getTimestamp());
        
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.debug("No scenarios found for hubId: {}", hubId);
            return;
        }

        log.info("Found {} scenarios for hubId: {}", scenarios.size(), hubId);
        
        // Инициализируем ленивую коллекцию scenarioActions для всех сценариев
        // Это загрузит коллекции через батчинг благодаря @BatchSize
        for (Scenario scenario : scenarios) {
            if (scenario.getScenarioActions() != null) {
                scenario.getScenarioActions().size(); // Инициализация ленивой коллекции
            }
        }
        
        for (Scenario scenario : scenarios) {
            log.debug("Checking scenario: name={}, hubId={}", scenario.getName(), hubId);
            boolean conditionsMet = checkScenarioConditions(scenario, snapshot);
            log.debug("Scenario '{}' conditions met: {}", scenario.getName(), conditionsMet);
            
            if (conditionsMet) {
                log.info("Executing actions for scenario: name={}, hubId={}", scenario.getName(), hubId);
                executeScenarioActions(scenario, hubId, snapshot);
            }
        }
    }

    private boolean checkScenarioConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        if (scenario.getScenarioConditions() == null || scenario.getScenarioConditions().isEmpty()) {
            return false;
        }

        return scenario.getScenarioConditions().stream()
                .allMatch(scenarioCondition -> checkCondition(scenarioCondition, snapshot));
    }

    private boolean checkCondition(ru.yandex.practicum.kafka.telemetry.entity.ScenarioCondition scenarioCondition, SensorsSnapshotAvro snapshot) {
        String sensorId = scenarioCondition.getSensor().getId();
        Condition condition = scenarioCondition.getCondition();
        
        log.debug("Checking condition: sensorId={}, type={}, operation={}, expectedValue={}", 
                sensorId, condition.getType(), condition.getOperation(), condition.getValue());
        
        // Получаем состояние датчика из снапшота
        // В Avro map<string, ...> генерируется как Map<String, ...>
        java.util.Map<String, SensorStateAvro> sensorsState = new java.util.HashMap<>(snapshot.getSensorsState());
        if (sensorsState == null || sensorsState.isEmpty()) {
            log.debug("Snapshot has null or empty sensorsState");
            return false;
        }
        
        SensorStateAvro sensorState = sensorsState.get(sensorId);
        if (sensorState == null) {
            log.debug("Sensor {} not found in snapshot", sensorId);
            return false;
        }

        // Извлекаем значение из payload в зависимости от типа условия
        Integer actualValue = extractValue(condition.getType(), sensorState.getPayload());
        if (actualValue == null) {
            log.debug("Could not extract value for condition type {} from sensor {}", condition.getType(), sensorId);
            return false;
        }

        Integer expectedValue = condition.getValue();
        String operation = condition.getOperation();

        log.debug("Condition check: sensorId={}, actualValue={}, expectedValue={}, operation={}", 
                sensorId, actualValue, expectedValue, operation);

        // Проверяем условие
        boolean result = switch (operation) {
            case "EQUALS" -> actualValue.equals(expectedValue);
            case "GREATER_THAN" -> actualValue > expectedValue;
            case "LOWER_THAN" -> actualValue < expectedValue;
            default -> {
                log.warn("Unknown operation: {}", operation);
                yield false;
            }
        };
        
        log.debug("Condition result: sensorId={}, result={}", sensorId, result);
        return result;
    }

    private Integer extractValue(String conditionType, Object payload) {
        if (payload == null) {
            return null;
        }

        return switch (conditionType) {
            case "TEMPERATURE" -> {
                if (payload instanceof TemperatureSensorAvro temp) {
                    yield temp.getTemperatureC();
                } else if (payload instanceof ClimateSensorAvro climate) {
                    yield climate.getTemperatureC();
                }
                yield null;
            }
            case "LUMINOSITY" -> {
                if (payload instanceof LightSensorAvro light) {
                    yield light.getLuminosity();
                }
                yield null;
            }
            case "MOTION" -> {
                if (payload instanceof MotionSensorAvro motion) {
                    yield motion.getMotion() ? 1 : 0;
                }
                yield null;
            }
            case "SWITCH" -> {
                if (payload instanceof SwitchSensorAvro sw) {
                    yield sw.getState() ? 1 : 0;
                }
                yield null;
            }
            case "CO2LEVEL" -> {
                if (payload instanceof ClimateSensorAvro climate) {
                    yield climate.getCo2Level();
                }
                yield null;
            }
            case "HUMIDITY" -> {
                if (payload instanceof ClimateSensorAvro climate) {
                    yield climate.getHumidity();
                }
                yield null;
            }
            default -> null;
        };
    }

    private void executeScenarioActions(Scenario scenario, String hubId, SensorsSnapshotAvro snapshot) {
        if (scenario.getScenarioActions() == null || scenario.getScenarioActions().isEmpty()) {
            return;
        }

        for (ScenarioAction scenarioAction : scenario.getScenarioActions()) {
            try {
                executeAction(scenario, hubId, scenarioAction, snapshot);
            } catch (Exception e) {
                log.error("Error executing action for scenario {}: {}", scenario.getName(), e.getMessage(), e);
            }
        }
    }

    private void executeAction(Scenario scenario, String hubId, ScenarioAction scenarioAction, SensorsSnapshotAvro snapshot) {
        Action action = scenarioAction.getAction();
        String sensorId = scenarioAction.getSensor().getId();

        // Создаем DeviceActionProto
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId);

        // Устанавливаем тип действия
        ActionTypeProto actionType = switch (action.getType()) {
            case "ACTIVATE" -> ActionTypeProto.ACTIVATE;
            case "DEACTIVATE" -> ActionTypeProto.DEACTIVATE;
            case "INVERSE" -> ActionTypeProto.INVERSE;
            case "SET_VALUE" -> ActionTypeProto.SET_VALUE;
            default -> {
                log.warn("Unknown action type: {}", action.getType());
                yield null;
            }
        };

        if (actionType == null) {
            return;
        }

        actionBuilder.setType(actionType);

        // Устанавливаем значение, если оно есть
        if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        // Создаем DeviceActionRequest
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenario.getName())
                .setAction(actionBuilder.build())
                .setTimestamp(timestamp)
                .build();

        // Отправляем команду через gRPC
        try {
            log.info("Sending action to Hub Router: scenario={}, hubId={}, sensorId={}, actionType={}", 
                    scenario.getName(), hubId, sensorId, action.getType());
            
            if (hubRouterClient == null) {
                log.error("Hub Router client is null! Check gRPC configuration.");
                return;
            }
            
            hubRouterClient.handleDeviceAction(request);
            log.info("Action successfully sent to Hub Router: scenario={}, sensorId={}, actionType={}", 
                    scenario.getName(), sensorId, action.getType());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error executing action: scenario={}, sensorId={}, error={}", 
                    scenario.getName(), sensorId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error executing action: scenario={}, sensorId={}, error={}", 
                    scenario.getName(), sensorId, e.getMessage(), e);
        }
    }
}
