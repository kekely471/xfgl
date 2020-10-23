package com.tonbu.web.activiti;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * activiti配置数据源和加载流程引擎
 */


/*
@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("activitiDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            SpringAsyncExecutor springAsyncExecutor) throws IOException {
        return baseSpringProcessEngineConfiguration(
                activitiDataSource(),
                primaryTransactionManager(activitiDataSource()),
                springAsyncExecutor);
    }
}*/


@Configuration
public class ActivitiConfig {

}