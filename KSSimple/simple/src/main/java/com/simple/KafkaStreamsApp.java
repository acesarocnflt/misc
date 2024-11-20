// App che legge da un topic e scrive in altro:
package com.simple;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;

public class KafkaStreamsApp {
    public static void main(String[] args) {

        // Configurazioni per Kafka Streams
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "mdf-streams-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Creazione del builder di Kafka Streams
        StreamsBuilder builder = new StreamsBuilder();

        // Leggi dal topic mdf_latest_merged_values_topic
        KStream<String, String> sourceStream = builder.stream("mdf_latest_merged_values_topic");

        // Trasforma i dati se necessario (puoi applicare una logica di trasformazione qui)
        KStream<String, String> transformedStream = sourceStream.mapValues(value -> {
            // Logica per trasformare il valore, ad esempio aggiungere un prefisso o fare un'elaborazione
            return value; // Puoi applicare una trasformazione qui
        });

        // Scrivi i dati nel topic mdf_latest_values_topic
        transformedStream.to("mdf_latest_values_topic");

        // Creazione dell'istanza Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        // Avvia il flusso
        streams.start();

        // Aggiungi una chiusura del processo al termine
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}




