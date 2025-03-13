import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

public class MongoTemplateFactory {

    /**
     * Creates a MongoTemplate instance using the provided connection details.
     *
     * @param connectionString MongoDB connection string (e.g., "mongodb://localhost:27017/dbname")
     * @return A configured MongoTemplate instance
     */
    public static MongoTemplate createMongoTemplate(String connectionString) {
        // Create MongoClient
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();
        
        MongoClient mongoClient = MongoClients.create(settings);
        
        // Extract database name from connection string
        String databaseName = new ConnectionString(connectionString).getDatabase();
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name must be specified in the connection string");
        }
        
        // Create MongoTemplate
        SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
        return new MongoTemplate(factory);
    }
    
    /**
     * Creates a MongoTemplate instance with more explicit parameters.
     *
     * @param host MongoDB host (e.g., "localhost")
     * @param port MongoDB port (e.g., 27017)
     * @param databaseName Name of the database to connect to
     * @return A configured MongoTemplate instance
     */
    public static MongoTemplate createMongoTemplate(String host, int port, String databaseName) {
        String connectionString = String.format("mongodb://%s:%d/%s", host, port, databaseName);
        return createMongoTemplate(connectionString);
    }
    
    /**
     * Creates a MongoTemplate instance with authentication.
     *
     * @param host MongoDB host
     * @param port MongoDB port
     * @param databaseName Name of the database to connect to
     * @param username MongoDB username
     * @param password MongoDB password
     * @return A configured MongoTemplate instance
     */
    public static MongoTemplate createMongoTemplate(String host, int port, String databaseName, 
                                                   String username, String password) {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s", 
                                              username, password, host, port, databaseName);
        return createMongoTemplate(connectionString);
    }
}
