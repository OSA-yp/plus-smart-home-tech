package ru.yandex.practicum.kafka.telemetry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "hub_id")
    private String hubId;
}
