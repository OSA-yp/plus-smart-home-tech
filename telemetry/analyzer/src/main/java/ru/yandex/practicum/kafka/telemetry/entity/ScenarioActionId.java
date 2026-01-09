package ru.yandex.practicum.kafka.telemetry.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ScenarioActionId implements Serializable {
    private Long scenario;
    private String sensor;
    private Long action;
}
