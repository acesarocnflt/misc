// App che legge da un topic e scrive in altro:
package com.simple;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class KafkaStreamsApp {

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "KafkaStreamsApp-Simple");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100");

        // Create Topics
        List<String> topicNames = Arrays.asList("topicInput", "topicOutput", "leftTopic", "rightTopic", "joinedTopic");
        try (AdminClient adminClient = AdminClient.create(properties)) {
            List<NewTopic> topicsToCreate = new ArrayList<>();
            for (String topicName: topicNames) {
                NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                topicsToCreate.add(newTopic);
            }
            CreateTopicsResult result = adminClient.createTopics(topicsToCreate);
            result.all().whenComplete((v, e) -> {
                if (e != null && e instanceof TopicExistsException) {
                    System.out.println("One or more topics already exist!");
                } else if (e != null) {
                    System.err.println("Error when creating one or more topics: " + e.getMessage());
                } else {
                    System.out.println("Topics created: " + topicNames);
                }
            });
        }

        final StreamsBuilder builder = new StreamsBuilder();


        // // (APP per copiare eventi in terzo topic solo se id dei primi due topic sono uguali.
        Duration joinWindowSizeMs = Duration.ofSeconds(30);
        Duration gracePeriod = Duration.ofMinutes(1);
        KStream<String, String> leftStream = builder.stream("leftTopic");
        KStream<String, String> rightStream = builder.stream("rightTopic");
        KStream<String, String> leftStreamWithIdKey = leftStream.selectKey((key, value) -> extractId(value));
        KStream<String, String> rightStreamWithIdKey = rightStream.selectKey((key, value) -> extractId(value));
        JoinWindows joinWindow = JoinWindows.ofTimeDifferenceAndGrace(joinWindowSizeMs, gracePeriod);
        KStream<String, String> joinedStream = leftStreamWithIdKey.join(rightStreamWithIdKey, (leftValue, rightValue) -> {
            return "{\"event1\":" + leftValue + ",\"event2\":" + rightValue + "}";
        }, joinWindow,
        StreamJoined.with(Serdes.String(), Serdes.String(), Serdes.String()));
        joinedStream.to("joinedTopic", Produced.with(Serdes.String(), Serdes.String()));



        /* 
        * App che fa la join fra 2 topics/Streams co-partitioned. 
        * kafka-console-producer --bootstrap-server localhost:9092 --topic leftTopic --property "parse.key=true" --property "key.separator=,"
        *  >test1,prova1
        * 
        * kafka-console-producer --bootstrap-server localhost:9092 --topic rightTopic --property "parse.key=true" --property "key.separator=,"
        * >test1,prova2
        * 
        * kafka-console-consumer --bootstrap-server localhost:9092 --topic joinedTopic --from-beginning --property print.key=true --group joinedTopicConsumer
        *  test1   prova1-prova2
        *  test1   prova1-prova2
        */
        // Duration joinWindowSizeMs = Duration.ofSeconds(30);
        // Duration gracePeriod = Duration.ofMinutes(1);
        // KStream<String, String> leftStream = builder.stream("leftTopic");
        // KStream<String, String> rightStream = builder.stream("rightTopic");
        // JoinWindows joinWindow = JoinWindows.ofTimeDifferenceAndGrace(joinWindowSizeMs, gracePeriod);
        // KStream<String, String> joinedStream = leftStream.join(rightStream, (leftValue, rightValue) -> {
        //     if (leftValue == null || rightValue == null) {
        //         return "null";
        //     }
        //     return leftValue + "-" + rightValue;
        // }, joinWindow);
        // joinedStream.to("joinedTopic");




        // App che legge stringhe da topicInput e scrive su topicOutput in lowercase.
        // KStream<String, String> source = builder.stream("topicInput");
        // KStream<String, String> output = source.mapValues(value -> value.toLowerCase());
        // output.to("topicOutput");

         /*
         * To produce to the topic:
         * kafka-console-producer --bootstrap-server localhost:9092 --topic topicInput
         * 
         * To consume from the topic:
         * kafka-console-consumer --bootstrap-server localhost:9092 --topic topicOutput --from-beginning --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
         */
        // Conta le occorrenze delle parole: i.e. ciao world -> ciao: 1, world: 1
        // source.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
        //         .groupBy((key, value) -> value)
        //         .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"))
        //         .toStream()
        //         .to("topicOutput", Produced.with(Serdes.String(), Serdes.Long()));



        Topology topology = builder.build();
        System.out.println("Topology: " + topology);
        System.out.println("Describe of the Topology: \n" + topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, properties);
        streams.start();
         
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping app...");
            streams.close();
        }));
        
    }


    // (APP per copiare eventi in terzo topic solo se ID dei primi due topic sono uguali.
    // Funzione di supporto per estrarre l'id dall'evento JSON (formattazione json manuale)
    private static String extractId(String json) {
        try {
            int startIndex = json.indexOf("\"id\":\"") + 6; // Trova la posizione iniziale del campo "id"
            int endIndex = json.indexOf("\"", startIndex); // Trova la posizione della chiusura del valore "id"
            return json.substring(startIndex, endIndex); // Estrai il valore dell'id
        } catch (Exception e) {
            return ""; // Ritorna stringa vuota se non valido
        }
    }


}


        // Create Topics snippet
        //     NewTopic newTopic1 = new NewTopic("topicInput", 1, (short) 1);
        //     NewTopic newTopic2 = new NewTopic("topicOutput", 1, (short) 1);
        //     CreateTopicsResult result1 = adminClient.createTopics(Collections.singleton(newTopic1));
        //     CreateTopicsResult result2 = adminClient.createTopics(Collections.singleton(newTopic2));
        //     result1.all().get();
        //     result2.all().get();
        //     System.out.println("Topics created!");
        // } catch (TopicExistsException e) {
        //     System.out.println("One or more topics already exist!");
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
