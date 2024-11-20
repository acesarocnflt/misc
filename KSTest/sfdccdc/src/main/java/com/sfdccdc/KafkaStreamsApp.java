package com.sfdccdc;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.common.serialization.Serdes;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import com.sfdccdc.avro.ChangeEvent;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsApp {
    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsApp.class);
    public static void main(String[] args) {
        logger.info("Starting Kafka Streams App");

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "mdf-streams-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put("schema.registry.url", "http://localhost:8081");
        properties.put("specific.avro.reader", "true");

        // App starting log
        logger.debug("Configuration of the app Kafka Streams: {}", properties);

        // Configura l'Avro Serde
        SpecificAvroSerde<ChangeEvent> avroSerde = new SpecificAvroSerde<>();
        avroSerde.configure(propertiesToMap(properties), false);

        // Creazione del builder di Kafka Streams
        StreamsBuilder builder = new StreamsBuilder();

        // Leggi dal topic di origine
        KStream<String, ChangeEvent> sourceStream = builder.stream(
            "mdf_latest_merged_values_topic",
            Consumed.with(Serdes.String(), avroSerde)
        );

        // Elabora ogni evento separando per recordId
        sourceStream.flatMapValues(changeEvent -> {
            List<ChangeEvent> newEvents = new ArrayList<>();
            for (CharSequence recordId : changeEvent.getChangeEventHeader().getRecordIds()) {
                ChangeEvent newEvent = ChangeEvent.newBuilder(changeEvent)
                                                  .setId(recordId.toString())
                                                  .build();
                newEvents.add(newEvent);
            }
            return newEvents;
        }).to("mdf_latest_values_topic", Produced.with(Serdes.String(), avroSerde));

        // Creazione e avvio dell'istanza Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);
        streams.start();

        // KS Log when it started
        logger.info("Kafka Streams avviato");

        // Add Shutdown hook when the app gets shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down the Kafka Streams App");
            streams.close();
        }));
    }

    // Metodo utility per convertire Properties in Map<String, Object>
    private static Map<String, Object> propertiesToMap(Properties properties) {
        Map<String, Object> map = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return map;
    }
}
