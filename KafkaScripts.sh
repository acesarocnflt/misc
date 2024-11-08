kafka-topics --bootstrap-server ip-172-30-1-30.eu-west-1.compute.internal:9092 --list --command-config kafka_client.properties 

kafka-acls --bootstrap-server ip-172-30-1-30.eu-west-1.compute.internal:9092 --command-config kafka_client.properties_user1 --topic '*' --add --operation READ --operation DESCRIBE --operation WRITE --allow-principal 'User:user2' --allow-host '*'
kafka-acls --bootstrap-server ip-172-30-1-30.eu-west-1.compute.internal:9092 --command-config kafka_client.properties_user1 --add --operation DESCRIBE --allow-principal 'User:user2' --allow-host '*' --cluster

kafka-acls --bootstrap-server ip-172-30-1-30.eu-west-1.compute.internal:9092 --command-config kafka_client.properties --list




ubuntu@jumphost2:~$ cat kafka_client.properties
security.protocol=SASL_SSL
ssl.truststore.location=/home/ubuntu/certs/ssl/kafka-truststore.jks
ssl.truststore.password=changeme
#ssl.keystore.location=/path/to/keystore.jks
#ssl.keystore.password=keystore-password
ssl.key.password=changeme
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="user2" password="my-secret";



ubuntu@jumphost2:~$ cat kafka_client.properties_user1 
security.protocol=SASL_SSL
ssl.truststore.location=/home/ubuntu/certs/ssl/kafka-truststore.jks
ssl.truststore.password=changeme
#ssl.keystore.location=/path/to/keystore.jks
#ssl.keystore.password=keystore-password
ssl.key.password=changeme
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="user1" password="my-secret";



