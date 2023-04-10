package org.brapi.brava.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.utils.ReportParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIConfig {

    @Bean
    public ValidationService validationService(CollectionFactory collectionFactory, ReportParser reportParser) {
        return new ValidationService(collectionFactory, reportParser) ;
    }
    @Bean
    public CollectionFactory collectionFactory() {
       return new CollectionFactory() ;
    }

    @Bean
    public ReportParser reportParser(ObjectMapper objectMapper) {
        return new ReportParser(objectMapper) ;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper() ;
    }
}
