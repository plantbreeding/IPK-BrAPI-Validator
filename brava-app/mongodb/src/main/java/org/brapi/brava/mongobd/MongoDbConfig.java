package org.brapi.brava.mongobd;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Profile("mongodb")
@Configuration
@EnableMongoRepositories
public class MongoDbConfig {
}
