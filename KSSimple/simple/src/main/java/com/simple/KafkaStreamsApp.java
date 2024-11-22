// App che legge da un topic e scrive in altro:
package com.simple;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsApp {
    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsApp.class);
    public static void main(String[] args) {
        logger.info("Starting Kafka Streams App");

        // Configurazioni per Kafka Streams
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kssimple");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Creazione del builder di Kafka Streams
        StreamsBuilder builder = new StreamsBuilder();

        // Leggi dal topic mdf_latest_merged_values_topic
        KStream<String, String> sourceStream = builder.stream("topicinput");

        // Trasforma i dati se necessario (puoi applicare una logica di trasformazione qui)
        KStream<String, String> transformedStream = sourceStream.mapValues(value -> {
            // Logica per trasformare il valore, ad esempio aggiungere un prefisso o fare un'elaborazione
            return value; // Puoi applicare una trasformazione qui
        });

        // Scrivi i dati nel topic mdf_latest_values_topic
        transformedStream.to("topicoutput");

        // Creazione dell'istanza Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        // Avvia il flusso
        streams.start();

        // Aggiungi una chiusura del processo al termine
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}




