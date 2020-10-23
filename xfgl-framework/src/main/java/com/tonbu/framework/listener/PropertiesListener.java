package com.tonbu.framework.listener;

import com.tonbu.framework.config.PropertiesConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class PropertiesListener implements ApplicationListener<ApplicationStartedEvent> {


    @Value("${spring.profiles.main}")
    private String propertyFileName;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        PropertiesConfig.loadAllProperties(propertyFileName);
    }
}