# Create the connector using the Datagen connector plugin
curl -X POST -H "Content-Type: application/json" \
  --data '{
    "name": "datagen-connector",
    "config": {
      "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
      "tasks.max": "1",
      "kafka.topic": "datagen_angelo1",
      "iterations": "100000",
      "sleep.ms": "1000",
      "key.field": "id",
      "value.field": "data",
      "value.converter": "org.apache.kafka.connect.storage.StringConverter",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "quickstart": "clickstream"
    }
  }' \
  http://connect-0.angeloc:8083/connectors


# Update the connector
curl -X PUT -H "Content-Type: application/json" \
  --data '{
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "tasks.max": "1",
    "kafka.topic": "datagen_angelo1",
    "iterations": "100000",
    "sleep.ms": "1000",
    "key.field": "id",
    "value.field": "data",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "quickstart": "orders"
  }' \
  http://connect-0.angeloc:8083/connectors/datagen-connector/config


# Validate connector against the Datagen plugin
curl -X PUT -H "Content-Type: application/json" \
  --data '{
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "tasks.max": "1",
    "kafka.topic": "datagen_angelo1",
    "iterations": "100000",
    "sleep.ms": "1000",
    "key.field": "id",
    "value.field": "data",
    "value.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "quickstart": "clickstream"
  }' \
  http://connect-0.angeloc:8083/connector-plugins/io.confluent.kafka.connect.datagen.DatagenConnector/config/validate



http://localhost:8083/connector-plugins
http://localhost:8083/connectors/datagen-connector/config
