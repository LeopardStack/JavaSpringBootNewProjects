package com.scnu.springbootjdk17demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean("primaryDS")
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("replicaDS")
    @ConfigurationProperties("spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("primaryDS") DataSource primary,
            @Qualifier("replicaDS") DataSource replica) {

        RoutingDataSource routing = new RoutingDataSource();
        routing.setDefaultTargetDataSource(primary);
        routing.setTargetDataSources(Map.of(
                "primary", primary,
                "replica", replica
        ));
        routing.afterPropertiesSet();
        return new LazyConnectionDataSourceProxy(routing);
    }
}