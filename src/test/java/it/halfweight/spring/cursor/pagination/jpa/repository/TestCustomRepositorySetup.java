package it.halfweight.spring.cursor.pagination.jpa.repository;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"it.halfweight.spring.cursor.pagination.jpa.repository"}, repositoryBaseClass = CustomRepositoryImpl.class)
@EntityScan("it.halfweight.spring.cursor.pagination.jpa.domain")
public class TestCustomRepositorySetup {


}
