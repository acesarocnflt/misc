// // App che legge da un topic e scrive in altro:
// package com.simple;

// import org.apache.kafka.streams.KafkaStreams;
// import org.apache.kafka.streams.StreamsConfig;
// import org.apache.kafka.streams.StreamsBuilder;
// import org.apache.kafka.streams.kstream.KStream;
// import org.apache.kafka.streams.kstream.KTable;
// import org.apache.kafka.streams.kstream.Materialized;
// import org.apache.kafka.streams.kstream.Produced;
// import org.apache.kafka.streams.state.KeyValueStore;
// import org.apache.kafka.clients.admin.AdminClient;
// import org.apache.kafka.clients.admin.NewTopic;
// import org.apache.kafka.common.serialization.Serdes;
// import org.apache.kafka.common.utils.Bytes;

// import java.util.Collections;
// import java.util.Properties;
// import java.util.concurrent.ExecutionException;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// public class KafkaStreamsApp {
//     private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsApp.class);
//     public static void main(String[] args) {
//         logger.info("Starting Kafka Streams App");

//         // Configurazioni per Kafka Streams
//         Properties properties = new Properties();
//         properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "kssimple");
//         properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//         properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//         properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

//         // Properties topicProperties = new Properties();
//         // topicProperties.put("topicInput", "topicinput");
//         // topicProperties.put("topicInputPartitions", "1");
//         // topicProperties.put("topicInputReplicationFactor", "1");
//         // topicProperties.put("topicOutput", "topicoutput");
//         // topicProperties.put("topicOutputPartitions", "1");
//         // topicProperties.put("topicOutputReplicationFactor", "1");


//         // Creazione del builder di Kafka Streams
//         StreamsBuilder builder = new StreamsBuilder();


//         // KTable Test
//         final String orderNumberStart = "orderNumber-";
//         KTable<String, String> firsKTable = builder.table("topicinputtable",
//                 Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("ktable-store")
//                     .withKeySerde(Serdes.String())
//                     .withValueSerde(Serdes.String()));

//         firsKTable.filter((key, value) -> value.contains(orderNumberStart))
//                 .mapValues(value -> value.substring(value.indexOf("-") + 1))
//                 .filter((key, value) -> Long.parseLong(value) > 1000)
//                 .toStream()
//                 .peek((key, value) -> System.out.println("Outgoing record" + key + " value" + value))
//                 .to("topicoutputtable", Produced.with(Serdes.String(), Serdes.String()));


//         // Leggi dal topic mdf_latest_merged_values_topic
//         KStream<String, String> sourceStream = builder.stream("topicinput");

//         // Trasforma i dati se necessario (puoi applicare una logica di trasformazione qui)
//         KStream<String, String> transformedStream = sourceStream.mapValues(value -> {
//             // Logica per trasformare il valore, ad esempio aggiungere un prefisso o fare un'elaborazione
//             return value; // Puoi applicare una trasformazione qui
//         });

//         // Scrivi i dati nel topic mdf_latest_values_topic
//         transformedStream.to("topicoutput");

//         // Creazione dell'istanza Kafka Streams
//         KafkaStreams streams = new KafkaStreams(builder.build(), properties);

//         // Avvia il flusso
//         streams.start();

//         // Aggiungi una chiusura del processo al termine
//         Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
//     }

//     // /**
//     //  * Creates a Kafka topic if it doesn't exist
//     //  * @param topicName Name of the topic
//     //  * @param numPartitions Number of partitions
//     //  * @param replicationFactor Replication factor
//     //  * @param properties Kafka connection properties
//     //  */
//     // private static void createTopic(String topicName, int numPartitions, short replicationFactor, Properties properties) {
//     //     try (AdminClient adminClient = AdminClient.create(properties)) {
//     //         NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
//     //         adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
//     //         logger.info("Topic '{}' created successfully", topicName);
//     //     } catch (ExecutionException e) {
//     //         logger.warn("Topic '{}' already exists or cannot be created: {}", topicName, e.getMessage());
//     //     } catch (Exception e) {
//     //         logger.error("Error creating topic '{}': {}", topicName, e.getMessage());
//     //     }
//     // }

// }




