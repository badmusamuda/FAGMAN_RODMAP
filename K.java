import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;

public class GenericCodec<T> implements Codec<T> {
    private final Codec<Document> documentCodec = new DocumentCodec();
    private final Class<T> clazz;

    public GenericCodec(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return document.to(clazz);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        Document document = new Document();
        document.putAll((Document) value);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }
}

CodecRegistry customCodecRegistry = CodecRegistries.fromRegistries(
    MongoClientSettings.getDefaultCodecRegistry(),
    CodecRegistries.fromCodecs(new GenericCodec<>(IntervalRecord.class))
);

MongoClientSettings settings = MongoClientSettings.builder()
    .codecRegistry(customCodecRegistry)
    .build();

MongoClient mongoClient = MongoClients.create(settings);
MongoDatabase database = mongoClient.getDatabase("yourDatabase");
MongoCollection<IntervalRecord> collection = database.getCollection("yourCollection", IntervalRecord.class);

IntervalRecord record = collection.find().first();
System.out.println("Record: " + record);

