package com.tonbu.web.activiti;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration//声名为配置类，继承Activiti抽象配置类
public class ActivitiDBConfig extends AbstractProcessEngineAutoConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.activiti")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            SpringAsyncExecutor springAsyncExecutor) throws IOException {

        return baseSpringProcessEngineConfiguration(
                activitiDataSource(),
                new DataSourceTransactionManager(activitiDataSource()),
                springAsyncExecutor);
    }
}
