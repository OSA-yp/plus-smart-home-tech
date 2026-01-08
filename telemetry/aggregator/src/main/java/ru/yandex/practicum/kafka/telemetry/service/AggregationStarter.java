package ru.yandex.practicum.kafka.telemetry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    @Value("${app.kafka.consumer.bootstrap-servers}")
    private String consumerBootstrapServers;

    @Value("${app.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${app.kafka.producer.bootstrap-servers}")
    private String producerBootstrapServers;

    @Value("${app.kafka.topics.sensor-events}")
    private String sensorEventsTopic;

    @Value("${app.kafka.topics.snapshots}")
    private String snapshotsTopic;

    private KafkaConsumer<String, SensorEventAvro> consumer;
    private KafkaProducer<String, SpecificRecordBase> producer;
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();


    public void start() {
        try {
            // Инициализация Consumer
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(sensorEventsTopic));

            // Инициализация Producer
            Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

            producer = new KafkaProducer<>(producerProps);

            log.info("Aggregation started. Subscribed to topic: {}", sensorEventsTopic);

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    if (event != null) {
                        Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(event);
                        if (updatedSnapshot.isPresent()) {
                            SensorsSnapshotAvro snapshot = updatedSnapshot.get();
                            ProducerRecord<String, SpecificRecordBase> producerRecord =
                                    new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);
                            producer.send(producerRecord, (metadata, exception) -> {
                                if (exception != null) {
                                    log.warn("Error to sent snapshot to Kafka topic '{}': {}",
                                            snapshotsTopic, exception.getMessage(), exception);
                                } else {
                                    log.info("Snapshot sent to topic: {}, partition: {}, offset: {}",
                                            metadata.topic(), metadata.partition(), metadata.offset());
                                }
                            });
                        }
                    }
                }
                consumer.commitSync();
            }

        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедиться,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                if (consumer != null) {
                    consumer.close();
                }
                log.info("Закрываем продюсер");
                if (producer != null) {
                    producer.close();
                }
            }
        }
    }

    /**
     * Обновляет состояние снапшота на основе события датчика.
     * Возвращает Optional с обновленным снапшотом, если были изменения,
     * или пустой Optional, если изменений не было.
     */
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        // Проверяем, есть ли снапшот для hubId
        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            // Если снапшота нет, создаём новый
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(event.getTimestamp())
                    .setSensorsState(new HashMap<>())
                    .build();
        }

        // Проверяем, есть ли в снапшоте данные для sensorId
        Map<String, SensorStateAvro> sensorsState = new HashMap<>(snapshot.getSensorsState());
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            // Если данные есть, проверяем timestamp и payload
            if (oldState.getTimestamp() > event.getTimestamp()) {
                // Событие произошло раньше, чем последнее обновление - игнорируем
                return Optional.empty();
            }
            // Проверяем, изменились ли данные
            // Сравниваем payload: если оба null или оба равны - не обновляем
            Object oldPayload = oldState.getPayload();
            Object newPayload = event.getPayload();
            if (oldPayload == null && newPayload == null) {
                return Optional.empty();
            }
            if (oldPayload != null && oldPayload.equals(newPayload)) {
                // Данные не изменились - не обновляем
                return Optional.empty();
            }
        }

        // Если дошли до сюда, значит, пришли новые данные и снапшот нужно обновить
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setPayload(event.getPayload())
                .build();

        sensorsState.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, updatedSnapshot);
        return Optional.of(updatedSnapshot);
    }
}
