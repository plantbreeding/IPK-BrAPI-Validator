package org.brapi.brava.api;

import org.brapi.brava.core.config.CollectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    @Bean
    public CollectionFactory collectionFactory() {
       return new CollectionFactory() ;
    }
}
