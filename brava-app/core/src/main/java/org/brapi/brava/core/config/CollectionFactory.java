package org.brapi.brava.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Collection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionFactory {

    private static final Pattern BRAPI_COLLECTION_FILE_NAME_PATTERN = Pattern.compile(	"^CompleteBrapiTest\\.(v\\d+\\.\\d+)\\.json$") ;
    private Map<String, CollectionDetails> collections;
    private ObjectMapper objectMapper ;
    private Path collectionsPath ;

    public CollectionFactory() {
        this(new ObjectMapper()) ;
    }

    public CollectionFactory(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper ;
        collections = new TreeMap<>(Comparator.comparing(name -> Float.valueOf(name.substring(1) ))) ;

        final FileSystem fileSystem = null ;

        try {
            URI uri = ClassLoader.getSystemResource("collections").toURI() ;

            // This is a workaround for when the 'collections' directory is within a jar
            if("jar".equals(uri.getScheme())) {
                final Map<String, String> env = new HashMap<>();
                final String[] array = uri.toString().split("!");
                final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
                collectionsPath = fs.getPath(array[1]);

            } else {
                collectionsPath = Path.of(uri) ;
            }

            Files.list(collectionsPath).forEach(path ->
            {
                Matcher matcher = BRAPI_COLLECTION_FILE_NAME_PATTERN.matcher(path.getName(path.getNameCount() - 1).toString());

                if (matcher.matches()) {
                    collections.put(matcher.group(1), new CollectionDetails(matcher.group(1), path)) ;
                }
            });

            if (fileSystem != null) {
                fileSystem.close();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection getCollection(String collectionName) throws CollectionNotFound {

        CollectionDetails collectionDetails = collections.get(collectionName);

        if (collectionDetails != null) {
            if (collectionDetails.getCollection() == null) {

                try {
                    collectionDetails.setCollection(objectMapper.readValue(Files.newInputStream(collectionDetails.getPath()), Collection.class));
                } catch (IOException e) {
                    throw new CollectionNotFound(e.getMessage());
                }
            }

            return collectionDetails.getCollection() ;
        } else {
            throw new CollectionNotFound(String.format("Can not find collection call %s", collectionName));
        }
    }

    public List<String> getCollectionNames() {
        return new ArrayList<>(collections.keySet());
    }

    public String getDefaultCollectionName() {
        if (!collections.isEmpty()) {
            return getCollectionNames().get(collections.keySet().size() - 1);
        } else {
            return null ;
        }
    }

    @Getter
    @Setter
    private class CollectionDetails implements Comparable<CollectionDetails>{
        @NonNull
        @Setter(AccessLevel.NONE)
        private String name ;
        @NonNull
        @Setter(AccessLevel.NONE)
        private Path path ;
        private Collection collection ;

        private Float version ;

        public CollectionDetails(@NonNull String name, @NonNull Path path) {
            this.name = name;
            this.path = path;
            this.version = Float.valueOf(name.substring(1));
        }

        @Override
        public int compareTo(CollectionDetails collectionDetails) {
            return this.version.compareTo(collectionDetails.getVersion());
        }
    }
}
