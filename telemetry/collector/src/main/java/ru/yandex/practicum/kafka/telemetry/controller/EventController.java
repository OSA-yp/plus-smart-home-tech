package ru.yandex.practicum.kafka.telemetry.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAction;
import ru.yandex.practicum.kafka.telemetry.event.DeviceType;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.BaseHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.DeviceAddedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.DeviceRemovedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.HubEventType;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.ScenarioAddedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.hub.events.ScenarioRemovedHubEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.ClimateSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.LightSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.MotionSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.model.sensors.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.model.sensors.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.service.handler.sensors.SensorEventHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers,
                           Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {

        log.info("Received SensorEventProto: id={}, hubId={}, timestamp={}",
                request.getId(), request.getHubId(), request.getTimestamp());
        try {
            SensorEvent event = convertSensorEventProto(request);

            if (sensorEventHandlers.containsKey(event.getType())) {
                sensorEventHandlers.get(event.getType()).handle(event);
            } else {
                throw new IllegalArgumentException("Can't find a handler for the event " + event.getType());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            BaseHubEvent event = convertHubEventProto(request);

            if (hubEventHandlers.containsKey(event.getType())) {
                hubEventHandlers.get(event.getType()).handle(event);
            } else {
                throw new IllegalArgumentException("Can't find a handler for the event " + event.getType());
            }

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    private SensorEvent convertSensorEventProto(SensorEventProto request) {
        Instant timestamp = Instant.ofEpochSecond(
                request.getTimestamp().getSeconds(),
                request.getTimestamp().getNanos()
        );

        SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();
        SensorEvent event;

        switch (payloadCase) {
            case MOTION_SENSOR_EVENT:
                MotionSensorProto motionProto = request.getMotionSensorEvent();
                MotionSensorEvent motionEvent = new MotionSensorEvent();
                motionEvent.setId(request.getId());
                motionEvent.setHubId(request.getHubId());
                motionEvent.setTimestamp(timestamp);
                motionEvent.setLinkQuality(motionProto.getLinkQuality());
                motionEvent.setMotion(motionProto.getMotion());
                motionEvent.setVoltage(motionProto.getVoltage());
                event = motionEvent;
                break;

            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorProto tempProto = request.getTemperatureSensorEvent();
                TemperatureSensorEvent tempEvent = new TemperatureSensorEvent();
                tempEvent.setId(request.getId());
                tempEvent.setHubId(request.getHubId());
                tempEvent.setTimestamp(timestamp);
                tempEvent.setTemperatureC(tempProto.getTemperatureC());
                tempEvent.setTemperatureF(tempProto.getTemperatureF());
                event = tempEvent;
                break;

            case LIGHT_SENSOR_EVENT:
                LightSensorProto lightProto = request.getLightSensorEvent();
                LightSensorEvent lightEvent = new LightSensorEvent();
                lightEvent.setId(request.getId());
                lightEvent.setHubId(request.getHubId());
                lightEvent.setTimestamp(timestamp);
                lightEvent.setLinkQuality(lightProto.getLinkQuality());
                lightEvent.setLuminosity(lightProto.getLuminosity());
                event = lightEvent;
                break;

            case CLIMATE_SENSOR_EVENT:
                ClimateSensorProto climateProto = request.getClimateSensorEvent();
                ClimateSensorEvent climateEvent = new ClimateSensorEvent();
                climateEvent.setId(request.getId());
                climateEvent.setHubId(request.getHubId());
                climateEvent.setTimestamp(timestamp);
                climateEvent.setTemperatureC(climateProto.getTemperatureC());
                climateEvent.setHumidity(climateProto.getHumidity());
                climateEvent.setCo2Level(climateProto.getCo2Level());
                event = climateEvent;
                break;

            case SWITCH_SENSOR_EVENT:
                SwitchSensorProto switchProto = request.getSwitchSensorEvent();
                SwitchSensorEvent switchEvent = new SwitchSensorEvent();
                switchEvent.setId(request.getId());
                switchEvent.setHubId(request.getHubId());
                switchEvent.setTimestamp(timestamp);
                switchEvent.setState(switchProto.getState());
                event = switchEvent;
                break;

            default:
                throw new IllegalArgumentException("Получено событие неизвестного типа: " + payloadCase);
        }

        return event;
    }

    private BaseHubEvent convertHubEventProto(HubEventProto request) {
        Instant timestamp = Instant.ofEpochSecond(
                request.getTimestamp().getSeconds(),
                request.getTimestamp().getNanos()
        );

        HubEventProto.PayloadCase payloadCase = request.getPayloadCase();
        BaseHubEvent event;

        switch (payloadCase) {
            case DEVICE_ADDED:
                DeviceAddedEventProto deviceAddedProto = request.getDeviceAdded();
                DeviceAddedHubEvent deviceAddedEvent = new DeviceAddedHubEvent();
                deviceAddedEvent.setHubId(request.getHubId());
                deviceAddedEvent.setTimestamp(timestamp);
                deviceAddedEvent.setId(deviceAddedProto.getId());
                deviceAddedEvent.setDeviceType(convertDeviceTypeProto(deviceAddedProto.getType()));
                event = deviceAddedEvent;
                break;

            case DEVICE_REMOVED:
                DeviceRemovedEventProto deviceRemovedProto = request.getDeviceRemoved();
                DeviceRemovedHubEvent deviceRemovedEvent = new DeviceRemovedHubEvent();
                deviceRemovedEvent.setHubId(request.getHubId());
                deviceRemovedEvent.setTimestamp(timestamp);
                deviceRemovedEvent.setId(deviceRemovedProto.getId());
                event = deviceRemovedEvent;
                break;

            case SCENARIO_ADDED:
                ScenarioAddedEventProto scenarioAddedProto = request.getScenarioAdded();
                ScenarioAddedHubEvent scenarioAddedEvent = new ScenarioAddedHubEvent();
                scenarioAddedEvent.setHubId(request.getHubId());
                scenarioAddedEvent.setTimestamp(timestamp);
                scenarioAddedEvent.setName(scenarioAddedProto.getName());
                scenarioAddedEvent.setConditions(convertScenarioConditions(scenarioAddedProto.getConditionList()));
                scenarioAddedEvent.setActions(convertDeviceActions(scenarioAddedProto.getActionList()));
                event = scenarioAddedEvent;
                break;

            case SCENARIO_REMOVED:
                ScenarioRemovedEventProto scenarioRemovedProto = request.getScenarioRemoved();
                ScenarioRemovedHubEvent scenarioRemovedEvent = new ScenarioRemovedHubEvent();
                scenarioRemovedEvent.setHubId(request.getHubId());
                scenarioRemovedEvent.setTimestamp(timestamp);
                scenarioRemovedEvent.setName(scenarioRemovedProto.getName());
                event = scenarioRemovedEvent;
                break;

            default:
                throw new IllegalArgumentException("Получено событие неизвестного типа: " + payloadCase);
        }

        return event;
    }

    private DeviceType convertDeviceTypeProto(DeviceTypeProto proto) {
        return DeviceType.valueOf(proto.name());
    }

    private List<ScenarioCondition> convertScenarioConditions(List<ScenarioConditionProto> protoList) {
        return protoList.stream()
                .map(proto -> {
                    ScenarioCondition condition = new ScenarioCondition();
                    condition.setSensorId(proto.getSensorId());

                    // Конвертация ConditionTypeProto в ConditionType enum
                    ru.yandex.practicum.kafka.telemetry.event.ConditionType conditionType =
                            ru.yandex.practicum.kafka.telemetry.event.ConditionType.valueOf(proto.getType().name());
                    condition.setType(conditionType);

                    // Конвертация ConditionOperationProto в ConditionOperation enum
                    ru.yandex.practicum.kafka.telemetry.event.ConditionOperation conditionOperation =
                            ru.yandex.practicum.kafka.telemetry.event.ConditionOperation.valueOf(proto.getOperation().name());
                    condition.setOperation(conditionOperation);

                    // Конвертация value (union { null, int, boolean })
                    ScenarioConditionProto.ValueCase valueCase = proto.getValueCase();
                    if (valueCase == ScenarioConditionProto.ValueCase.BOOL_VALUE) {
                        condition.setValue(proto.getBoolValue());
                    } else if (valueCase == ScenarioConditionProto.ValueCase.INT_VALUE) {
                        condition.setValue(proto.getIntValue());
                    } else {
                        condition.setValue(null);
                    }

                    return condition;
                })
                .collect(Collectors.toList());
    }

    private List<DeviceAction> convertDeviceActions(List<DeviceActionProto> protoList) {
        return protoList.stream()
                .map(proto -> {
                    DeviceAction action = new DeviceAction();
                    action.setSensorId(proto.getSensorId());

                    // Конвертация ActionTypeProto в ActionType enum
                    ru.yandex.practicum.kafka.telemetry.event.ActionType actionType =
                            ru.yandex.practicum.kafka.telemetry.event.ActionType.valueOf(proto.getType().name());
                    action.setType(actionType);

                    if (proto.hasValue()) {
                        action.setValue(proto.getValue());
                    } else {
                        action.setValue(null);
                    }
                    return action;
                })
                .collect(Collectors.toList());
    }
}
