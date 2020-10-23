package com.tonbu.support.config;

import com.jagregory.shiro.freemarker.ShiroTags;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.PostConstruct;


@Configuration
public class FreeMarkerConfig {


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @PostConstruct
    public void setSharedVariable() throws TemplateException {
        freeMarkerConfigurer.getConfiguration().setSharedVariable("shiro", new ShiroTags());
        freeMarkerConfigurer.getConfiguration().setSetting("number_format","0.##");
    }


}
