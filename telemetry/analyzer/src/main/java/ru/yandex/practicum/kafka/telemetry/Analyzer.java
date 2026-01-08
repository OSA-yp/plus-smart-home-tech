package ru.yandex.practicum.kafka.telemetry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.kafka.telemetry.service.processor.HubEventProcessor;
import ru.yandex.practicum.kafka.telemetry.service.processor.SnapshotProcessor;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
                SpringApplication.run(Analyzer.class, args);

        final HubEventProcessor hubEventProcessor = 
                context.getBean(HubEventProcessor.class);
        SnapshotProcessor snapshotProcessor = 
                context.getBean(SnapshotProcessor.class);

        // Регистрируем shutdown hook для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Analyzer...");
            // Прерываем потоки
            Thread.currentThread().getThreadGroup().interrupt();
        }));

        // запускаем в отдельном потоке обработчик событий
        // от пользовательских хабов
        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        snapshotProcessor.start();
    }
}
