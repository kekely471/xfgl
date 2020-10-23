package com.tonbu.framework.config;


import com.tonbu.framework.dao.dialect.DialectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FactoryConfig {

    @Bean(name = "dialectProperties")
    public DialectFactory createDialectFactory(){




        return new DialectFactory();

    }


}
