package org.brapi.brava.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Profile("jpa")
@Configuration
@EnableJpaRepositories
@EntityScan
public class JPAConfig {

}