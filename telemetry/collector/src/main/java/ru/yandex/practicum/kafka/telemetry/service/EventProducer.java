package ru.yandex.practicum.kafka.telemetry.service; // Или другой подходящий пакет

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

@Slf4j
@Component
public class EventProducer {

    @Value("${app.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Getter
    private Producer<String, SpecificRecordBase> producer;


    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);


        this.producer = new KafkaProducer<>(props);
    }

    // Метод для отправки Avro-сообщения
    public void sendAvroEvent(String topic, SpecificRecordBase avroEvent) {
        try {
            ProducerRecord<String, SpecificRecordBase> record =
                    new ProducerRecord<>(topic, avroEvent);

            // Отправляем в Kafka
            Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка отправки сообщения в Kafka topic '{}': {}", topic, exception.getMessage(), exception);
                } else {
                    log.info("Сообщение успешно отправлено в топик: {}, partition: {}, offset: {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });

        } catch (Exception e) { // Более общий перехват
            log.error("Ошибка при подготовке или отправке Avro объекта в топик '{}'", topic, e);
        }

    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
            log.info("Kafka producer закрыт");
        }
    }
}