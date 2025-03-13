import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * A utility class for MongoDB operations in pure Java without Spring dependencies.
 * This provides similar functionality to Spring's MongoTemplate.
 */
public class MongoDBClient<T> {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final Class<T> entityClass;
    private final String collectionName;

    /**
     * Creates a new MongoDB client instance.
     *
     * @param connectionString MongoDB connection string
     * @param databaseName     Database name
     * @param entityClass      Entity class type
     * @param collectionName   Collection name
     */
    public MongoDBClient(String connectionString, String databaseName, Class<T> entityClass, String collectionName) {
        // Configure codec registry for POJO support
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        // Build client settings with POJO support
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();

        // Create the client with settings
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        this.entityClass = entityClass;
        this.collectionName = collectionName;
    }

    /**
     * Gets the MongoDB collection for the configured entity type.
     *
     * @return MongoCollection instance
     */
    public MongoCollection<T> getCollection() {
        return database.getCollection(collectionName, entityClass);
    }

    /**
     * Finds all documents in the collection.
     *
     * @return List of entity objects
     */
    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        getCollection().find().forEach((Consumer<T>) results::add);
        return results;
    }

    /**
     * Finds documents matching the provided filter.
     *
     * @param filter Filter criteria
     * @return List of matching entity objects
     */
    public List<T> find(Bson filter) {
        List<T> results = new ArrayList<>();
        getCollection().find(filter).forEach((Consumer<T>) results::add);
        return results;
    }

    /**
     * Finds a single document by its ID.
     *
     * @param id Document ID
     * @return Entity object or null if not found
     */
    public T findById(String id) {
        return getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
    }

    /**
     * Finds a single document matching the provided filter.
     *
     * @param filter Filter criteria
     * @return Entity object or null if not found
     */
    public T findOne(Bson filter) {
        return getCollection().find(filter).first();
    }

    /**
     * Inserts a single document.
     *
     * @param entity Entity to insert
     * @return Inserted entity with populated ID
     */
    public T insert(T entity) {
        InsertOneResult result = getCollection().insertOne(entity);
        return entity; // The entity should now have its ID populated
    }

    /**
     * Inserts multiple documents.
     *
     * @param entities List of entities to insert
     * @return List of inserted entities with populated IDs
     */
    public List<T> insertMany(List<T> entities) {
        InsertManyResult result = getCollection().insertMany(entities);
        return entities; // Entities should now have their IDs populated
    }

    /**
     * Updates a document matching the filter with the provided updates.
     *
     * @param filter  Filter criteria
     * @param updates Updates to apply
     * @return Number of documents modified
     */
    public long updateOne(Bson filter, Bson updates) {
        UpdateResult result = getCollection().updateOne(filter, updates);
        return result.getModifiedCount();
    }

    /**
     * Updates a document and returns the updated version.
     *
     * @param filter  Filter criteria
     * @param updates Updates to apply
     * @return Updated entity or null if not found
     */
    public T findAndModify(Bson filter, Bson updates) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER);
        return getCollection().findOneAndUpdate(filter, updates, options);
    }

    /**
     * Updates multiple documents matching the filter with the provided updates.
     *
     * @param filter  Filter criteria
     * @param updates Updates to apply
     * @return Number of documents modified
     */
    public long updateMany(Bson filter, Bson updates) {
        UpdateResult result = getCollection().updateMany(filter, updates);
        return result.getModifiedCount();
    }

    /**
     * Updates a document or inserts it if not found (upsert).
     *
     * @param filter  Filter criteria
     * @param updates Updates to apply
     * @return Number of documents modified or created
     */
    public long upsert(Bson filter, Bson updates) {
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult result = getCollection().updateOne(filter, updates, options);
        return result.getModifiedCount() + (result.getUpsertedId() != null ? 1 : 0);
    }

    /**
     * Deletes a document matching the provided filter.
     *
     * @param filter Filter criteria
     * @return Number of documents deleted
     */
    public long deleteOne(Bson filter) {
        DeleteResult result = getCollection().deleteOne(filter);
        return result.getDeletedCount();
    }

    /**
     * Deletes multiple documents matching the provided filter.
     *
     * @param filter Filter criteria
     * @return Number of documents deleted
     */
    public long deleteMany(Bson filter) {
        DeleteResult result = getCollection().deleteMany(filter);
        return result.getDeletedCount();
    }

    /**
     * Counts documents matching the provided filter.
     *
     * @param filter Filter criteria
     * @return Count of matching documents
     */
    public long count(Bson filter) {
        return getCollection().countDocuments(filter);
    }

    /**
     * Closes the MongoDB client connection.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}


import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

public class MongoDBExample {
    public static void main(String[] args) {
        // Example User class
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setAge(30);

        // Initialize MongoDB client
        String connectionString = "mongodb://localhost:27017";
        String databaseName = "testdb";
        String collectionName = "users";
        
        MongoDBClient<User> mongoClient = new MongoDBClient<>(
                connectionString, 
                databaseName, 
                User.class, 
                collectionName
        );

        try {
            // Insert a single document
            System.out.println("Inserting a user...");
            User insertedUser = mongoClient.insert(user);
            System.out.println("Inserted user with ID: " + insertedUser.getId());

            // Find a document by ID
            String userId = insertedUser.getId().toString();
            System.out.println("Finding user by ID: " + userId);
            User foundUser = mongoClient.findById(userId);
            System.out.println("Found user: " + foundUser.getName());

            // Update a document
            System.out.println("Updating user...");
            long updateCount = mongoClient.updateOne(
                    Filters.eq("_id", insertedUser.getId()),
                    Updates.combine(
                            Updates.set("name", "John Smith"),
                            Updates.inc("age", 1)
                    )
            );
            System.out.println("Updated " + updateCount + " documents");

            // Find and modify
            System.out.println("Finding and modifying user...");
            User updatedUser = mongoClient.findAndModify(
                    Filters.eq("_id", insertedUser.getId()),
                    Updates.set("email", "johnsmith@example.com")
            );
            System.out.println("Updated user email: " + updatedUser.getEmail());

            // Insert multiple documents
            System.out.println("Inserting multiple users...");
            List<User> users = Arrays.asList(
                    createUser("Alice", "alice@example.com", 25),
                    createUser("Bob", "bob@example.com", 35),
                    createUser("Charlie", "charlie@example.com", 40)
            );
            List<User> insertedUsers = mongoClient.insertMany(users);
            System.out.println("Inserted " + insertedUsers.size() + " users");

            // Find all documents
            System.out.println("Finding all users...");
            List<User> allUsers = mongoClient.findAll();
            System.out.println("Found " + allUsers.size() + " users");
            
            // Find with filter
            System.out.println("Finding users with age > 30...");
            List<User> olderUsers = mongoClient.find(Filters.gt("age", 30));
            System.out.println("Found " + olderUsers.size() + " users older than 30");
            
            // Count documents
            System.out.println("Counting users with age > 25...");
            long userCount = mongoClient.count(Filters.gt("age", 25));
            System.out.println("Found " + userCount + " users older than 25");

            // Delete a document
            System.out.println("Deleting a user...");
            long deleteCount = mongoClient.deleteOne(Filters.eq("name", "Alice"));
            System.out.println("Deleted " + deleteCount + " users");

            // Perform an upsert
            System.out.println("Upserting a user...");
            long upsertCount = mongoClient.upsert(
                    Filters.eq("email", "david@example.com"),
                    Updates.combine(
                            Updates.set("name", "David"),
                            Updates.set("age", 45)
                    )
            );
            System.out.println("Upserted " + upsertCount + " documents");

            // Clean up - delete all test documents
            System.out.println("Cleaning up...");
            long cleanupCount = mongoClient.deleteMany(new Document());
            System.out.println("Deleted " + cleanupCount + " documents during cleanup");
            
        } finally {
            // Close the connection
            mongoClient.close();
        }
    }
    
    private static User createUser(String name, String email, int age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
}

// Example POJO class
class User {
    private ObjectId id;
    private String name;
    private String email;
    private int age;

    // Getters and setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
