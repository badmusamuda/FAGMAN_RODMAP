import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaComplexObjectProducer {

    private final Producer<String, Interval<Server<Data>>> producer;
    private final String topic;

    public KafkaComplexObjectProducer(String bootstrapServers, String topic, String keytabPrincipal, 
                                      String keytabPath, String roleArn, String eventTopicArn) {
        Properties props = new Properties();
        
        // Basic Kafka producer configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Use StringSerializer for keys and our custom serializer for complex values
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put("serializer.class", Interval.class.getName());
        
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10000000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        
        // Security configuration
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.kerberos.service.name", "kafka");
        props.put("sasl.mechanism", "AWS_MSK_IAM");
        props.put("ssl.algorithm", "https");
        
        // IAM authentication
        props.put("sasl.jaas.config", 
                "software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.put("classHandler", "security.FabricAdaptorGSINETIAMCallbackHandler");
        
        // Kerberos configuration
        props.put("principal", keytabPrincipal);
        props.put("keytab", keytabPath);
        
        // AWS specific configuration
        props.put("role.arn", roleArn + "/" + eventTopicArn + "_publisher");
        props.put("agent", "TalX");
        props.put("reconnect.ms", 5000);
        props.put("login.retry.backoff.ms", 5000);
        props.put("enable.idempotence", "false");
        
        @SuppressWarnings("unchecked")
        Producer<String, Interval<Server<Data>>> typedProducer = 
            (Producer<String, Interval<Server<Data>>>) (Object) new KafkaProducer<>(props);
        
        this.producer = typedProducer;
        this.topic = topic;
    }

    public void sendEvent(String key, Interval<Server<Data>> message) {
        try {
            ProducerRecord<String, Interval<Server<Data>>> record = 
                new ProducerRecord<>(topic, key, message);
                
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error sending message: " + exception.getMessage());
                } else {
                    System.out.println("Message sent successfully to topic " + metadata.topic() +
                            " partition " + metadata.partition() +
                            " offset " + metadata.offset());
                }
            }).get();  // Blocking call - remove .get() for async
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        producer.flush();
        producer.close();
    }
}


public class KafkaComplexProducerDemo {
    public static void main(String[] args) {
        // Default configuration values
        String bootstrapServers = "kafka-broker1:9092,kafka-broker2:9092,kafka-broker3:9092";
        String topic = "mytopic";
        String keytabPrincipal = "${keytab.principal}";
        String keytabPath = "${keytab.path}";
        String roleArn = "${kafka.roleArn}";
        String eventTopicArn = "${kafka.eventTopicArn}";

        // Resolve configuration variables from environment
        bootstrapServers = KafkaConfigUtils.resolveVariables(bootstrapServers);
        topic = KafkaConfigUtils.resolveVariables(topic);
        keytabPrincipal = KafkaConfigUtils.resolveVariables(keytabPrincipal);
        keytabPath = KafkaConfigUtils.resolveVariables(keytabPath);
        roleArn = KafkaConfigUtils.resolveVariables(roleArn);
        eventTopicArn = KafkaConfigUtils.resolveVariables(eventTopicArn);

        // Create producer instance
        KafkaComplexObjectProducer producer = new KafkaComplexObjectProducer(
                bootstrapServers, 
                topic, 
                keytabPrincipal, 
                keytabPath, 
                roleArn, 
                eventTopicArn
        );

        try {
            // Create sample data objects
            Data data1 = new Data("temperature", "23.5", System.currentTimeMillis());
            Data data2 = new Data("humidity", "45.2", System.currentTimeMillis());
            
            // Create server objects containing the data
            Server<Data> server1 = new Server<>("srv-001", "app-server-1.example.com", 8080, data1);
            Server<Data> server2 = new Server<>("srv-002", "app-server-2.example.com", 8080, data2);
            
            // Create interval objects wrapping the servers
            long now = System.currentTimeMillis();
            Interval<Server<Data>> interval1 = new Interval<>(server1, now, now + 3600000); // 1 hour interval
            Interval<Server<Data>> interval2 = new Interval<>(server2, now, now + 7200000); // 2 hour interval
            
            // Send the complex objects to Kafka
            producer.sendEvent("server1", interval1);
            producer.sendEvent("server2", interval2);
            
            System.out.println("All complex messages sent successfully");
        } catch (Exception e) {
            System.err.println("Error in the demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the producer
            producer.close();
        }
    }
}


stages:
  - build

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository -Xmx4096m"
  CL_PROJECT_DIR: "."  # Adjust if your project uses a subdirectory

cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .m2/repository/
    - target/

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - microdnf install -y fontconfig
    - export JAVA_HOME=/usr/local/jdk-21

    # Detect changed modules
    - |
      if [[ -n "$CI_COMMIT_BEFORE_SHA" ]]; then
        BASE_COMMIT="$CI_COMMIT_BEFORE_SHA"
      else
        BASE_COMMIT=$(git merge-base origin/${CI_MERGE_REQUEST_TARGET_BRANCH_NAME} HEAD)
      fi

      CHANGED_MODULES=$(
        git diff --name-only ${BASE_COMMIT}..HEAD |
        grep -E 'pom\.xml|src/.*' |
        sed -n 's#\(.*\)/pom\.xml#\1#p; s#\(.*/src/.*\)#\1#p' |
        xargs -I{} dirname {} |
        sort -u |
        sed 's#/#:#g' |
        tr '\n' ',' | sed 's/,$//'
      )

    # Build only changed modules + dependencies
    - |
      if [[ -n "$CHANGED_MODULES" ]]; then
        echo "Building changed modules: ${CHANGED_MODULES}"
        mvn -B versions:set -DnewVersion="${VERSION}" -DskipTests
        mvn install -pl "$CHANGED_MODULES" -am -amd -DskipTests -T 1C
      else
        echo "No changes detected. Skipping build."
      fi

    # Copy artifacts only from built modules
    - mkdir -p artifacts
    - mkdir -p serviceArtifacts
    - |
      for module in back-service ls-event-listener feedback-event-listener feedback-batch feedback-goals-app rules-engine security; do
        if [[ -d "${module}/target" ]]; then
          cp ${module}/target/*.jar artifacts/
        fi
      done
    - ls -al artifacts/
