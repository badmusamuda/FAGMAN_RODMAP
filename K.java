import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonPostClient {

    /**
     * Sends a POST request with JSON payload to the specified URL.
     * 
     * @param urlString The URL to send the request to
     * @param jsonPayload The JSON payload as a string
     * @return The response from the server as a string
     * @throws IOException If an I/O error occurs
     */
    public static String postJson(String urlString, String jsonPayload) throws IOException {
        // Create URL object
        URL url = new URL(urlString);
        
        // Open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // Set up the connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            // Write JSON data to the connection
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check the response code
            int responseCode = conn.getResponseCode();
            
            // Read the response (from either input or error stream)
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            // Build the response string
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        } finally {
            // Always disconnect
            conn.disconnect();
        }
    }
    
    // Example usage
    public static void main(String[] args) {
        try {
            String endpoint = "https://jsonplaceholder.typicode.com/posts";
            String payload = "{\"title\":\"Test Title\",\"body\":\"Test Body\",\"userId\":1}";
            
            String response = postJson(endpoint, payload);
            System.out.println("Response: " + response);
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}















import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * A robust and scalable HTTP client for making REST API calls
 */
public class HttpClient {
    
    // Configurable properties
    private final int connectTimeout;
    private final int readTimeout;
    private final int maxRetries;
    private final long retryDelayMs;
    private final ExecutorService executorService;
    private final boolean enableCompression;
    
    // Default headers
    private final Map<String, String> defaultHeaders;
    
    /**
     * Builder for creating HttpClient instances
     */
    public static class Builder {
        private int connectTimeout = 10000; // 10 seconds
        private int readTimeout = 30000; // 30 seconds
        private int maxRetries = 3;
        private long retryDelayMs = 1000; // 1 second
        private int threadPoolSize = 10;
        private boolean enableCompression = true;
        private Map<String, String> defaultHeaders = new HashMap<>();
        
        public Builder() {
            // Add default headers
            defaultHeaders.put("Content-Type", "application/json");
            defaultHeaders.put("Accept", "application/json");
        }
        
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = (int) timeout.toMillis();
            return this;
        }
        
        public Builder readTimeout(Duration timeout) {
            this.readTimeout = (int) timeout.toMillis();
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder retryDelay(Duration delay) {
            this.retryDelayMs = delay.toMillis();
            return this;
        }
        
        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }
        
        public Builder enableCompression(boolean enable) {
            this.enableCompression = enable;
            return this;
        }
        
        public Builder withDefaultHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }
        
        public HttpClient build() {
            return new HttpClient(this);
        }
    }
    
    /**
     * Response object containing HTTP response details
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, String> headers;
        
        public HttpResponse(int statusCode, String body, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + statusCode +
                    ", body='" + body + '\'' +
                    ", headers=" + headers +
                    '}';
        }
    }
    
    /**
     * Private constructor - use Builder to create instances
     */
    private HttpClient(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
        this.executorService = Executors.newFixedThreadPool(builder.threadPoolSize);
        this.enableCompression = builder.enableCompression;
        this.defaultHeaders = builder.defaultHeaders;
    }
    
    /**
     * Send a POST request with JSON payload synchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @return HttpResponse object containing the response
     * @throws IOException If an I/O error occurs
     */
    public HttpResponse post(String url, String jsonPayload) throws IOException {
        return post(url, jsonPayload, null);
    }
    
    /**
     * Send a POST request with JSON payload and custom headers synchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @param headers Additional headers (will override default headers)
     * @return HttpResponse object containing the response
     * @throws IOException If an I/O error occurs
     */
    public HttpResponse post(String url, String jsonPayload, Map<String, String> headers) throws IOException {
        Map<String, String> mergedHeaders = new HashMap<>(defaultHeaders);
        if (headers != null) {
            mergedHeaders.putAll(headers);
        }
        
        if (enableCompression) {
            mergedHeaders.put("Accept-Encoding", "gzip");
        }
        
        IOException lastException = null;
        
        // Retry logic
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    // Exponential backoff with jitter
                    long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                    delay += (long) (delay * 0.2 * Math.random()); // Add 0-20% jitter
                    Thread.sleep(delay);
                }
                
                return executeRequest("POST", url, jsonPayload, mergedHeaders);
                
            } catch (IOException e) {
                lastException = e;
                
                // Only retry on connection issues, not HTTP errors
                if (!(e instanceof ConnectException || e instanceof SocketTimeoutException)) {
                    throw e;
                }
                
                System.err.println("Request failed (attempt " + (attempt + 1) + "/" + (maxRetries + 1) + 
                        "): " + e.getMessage());
                
                if (attempt == maxRetries) {
                    System.err.println("Max retries reached. Giving up.");
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
        
        // Should never reach here due to the throw in the loop
        throw lastException;
    }
    
    /**
     * Send a POST request asynchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @return CompletableFuture that will complete with the HttpResponse
     */
    public CompletableFuture<HttpResponse> postAsync(String url, String jsonPayload) {
        return postAsync(url, jsonPayload, null);
    }
    
    /**
     * Send a POST request with custom headers asynchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @param headers Additional headers (will override default headers)
     * @return CompletableFuture that will complete with the HttpResponse
     */
    public CompletableFuture<HttpResponse> postAsync(String url, String jsonPayload, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(url, jsonPayload, headers);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute request: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    /**
     * Execute the actual HTTP request
     */
    private HttpResponse executeRequest(String method, String urlString, String payload, 
                                       Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            
            // Set headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            
            // Send payload if provided
            if (payload != null && !payload.isEmpty()) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // Get response code
            int responseCode = connection.getResponseCode();
            
            // Read the response
            Map<String, String> responseHeaders = new HashMap<>();
            for (String key : connection.getHeaderFields().keySet()) {
                if (key != null) {  // HttpURLConnection returns a key=null for the status line
                    responseHeaders.put(key, connection.getHeaderField(key));
                }
            }
            
            // Get input stream (error stream if response code is >= 400)
            InputStream inputStream;
            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            
            // Handle GZIP compression if enabled
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            }
            
            // Read response body
            StringBuilder response = new StringBuilder();
            if (inputStream != null) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            
            return new HttpResponse(responseCode, response.toString(), responseHeaders);
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Shutdown the client and its resources
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Example usage
     */
    public static void main(String[] args) {
        // Create a client with custom settings
        HttpClient client = new HttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .maxRetries(2)
                .retryDelay(Duration.ofMillis(500))
                .threadPoolSize(4)
                .withDefaultHeader("X-API-Key", "your-api-key")
                .build();
        
        try {
            // Synchronous request
            String jsonPayload = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
            HttpResponse response = client.post("https://api.example.com/users", jsonPayload);
            
            System.out.println("Response code: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            // Asynchronous request
            client.postAsync("https://api.example.com/async", jsonPayload)
                .thenAccept(asyncResponse -> {
                    System.out.println("Async response received: " + asyncResponse.getStatusCode());
                })
                .exceptionally(e -> {
                    System.err.println("Async request failed: " + e.getMessage());
                    return null;
                });
            
            // Allow time for async request to complete
            Thread.sleep(2000);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown the client when done
            client.shutdown();
        }
    }
}


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
