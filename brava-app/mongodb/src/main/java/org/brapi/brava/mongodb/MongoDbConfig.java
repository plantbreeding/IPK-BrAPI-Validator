package org.brapi.brava.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Profile("mongodb")
@Configuration
@EnableMongoRepositories
public class MongoDbConfig extends AbstractMongoClientConfiguration {

    @Value("${brava.mongo.server-addresses:#{null}}")
    private List<String> serverAddresses;

    @Value("${spring.data.mongodb.host:#{null}}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.username:#{null}}")
    private String username;

    @Value("${spring.data.mongodb.password:#{null}}")
    private String password;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }


    @Bean
    public ExecutorService mongoExecutorService() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    @Bean
    public BeforeConvertCallback<DocumentWithId> beforeSaveCallback() {

        return (entity, collection) -> {

            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        };
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {

        if (serverAddresses != null && !serverAddresses.isEmpty()) {
            builder.applyToClusterSettings(settings -> settings.hosts(
                    requireNonNull(serverAddresses, "mongo server address list is empty").stream().map(this::serverAddress)
                            .collect(toList())
            ));
        } else {
            if (StringUtils.hasText(host)) {
                builder.applyToClusterSettings(settings -> settings.hosts(Collections.singletonList(serverAddress(host)))) ;
            }
        }

        if (StringUtils.hasText(username)) {
            builder.credential(
                    MongoCredential.createCredential(
                            username,
                            databaseName,
                            requireNonNull(password, "mongo password is empty").toCharArray()
                    )
            );
        }
    }

    private ServerAddress serverAddress(String server) {
        if (server.contains(":")) {
            String[] split = server.split(":");
            return new ServerAddress(split[0], parseInt(split[1])) ;
        } else {
            return new ServerAddress(server, this.port);
        }
    }
}
