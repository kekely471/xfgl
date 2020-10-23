
package com.tonbu.support.config;

import com.tonbu.support.interceptor.ApiInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class DefaultViewConfig extends WebMvcConfigurerAdapter {
    @Value("${server.file.root}")
    private String fileRoot;
    @Value("${file.filter.url}")
    private String fileFilterUrl;

    public DefaultViewConfig() {
    }


    @Bean
    ApiInterceptor apiInterceptor(){return new ApiInterceptor();}

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(apiInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/user/login")
                .excludePathPatterns("/api/v1/user/loginfake")
                .excludePathPatterns("/api/v1/files/delete")
                .excludePathPatterns("/api/v1/dict/list")
                .excludePathPatterns("/api/v1/version/Lastest")
                .excludePathPatterns("/api/v1/user/smscode")
                .excludePathPatterns("/api/v1/user/passwordbind")
                .excludePathPatterns("/api/v1/dwxx/get")
                .excludePathPatterns("/api/v1/download/apk")
                .excludePathPatterns("/api/v1/download/xiaojia")
                .excludePathPatterns("/api/v1/mobile/sjtb/sqb")
                .excludePathPatterns("/api/v1/mobile/sqb/list")
                .excludePathPatterns("/api/v1/mobile/sqb/view")
                .excludePathPatterns("/api/v1/mobile/sqbxq/list")
                .excludePathPatterns("/api/v1/mobile/list/user")
                .excludePathPatterns("/api/v1/mobile/pcs")
                .excludePathPatterns("/api/v1/mobile/dwsg/list")
                .excludePathPatterns("/api/v1/dwsg/pcs/list")
                .excludePathPatterns("/api/v1/mobile/sgck")
                .excludePathPatterns("/api/v1/mobile/dwsg")
                .excludePathPatterns("/api/v1/mobile/list/user")
                .excludePathPatterns("/api/v1/mobile/pcs/list");
        super.addInterceptors(interceptorRegistry);
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.addViewControllers(registry);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler(fileFilterUrl+"**").addResourceLocations("file:"+fileRoot);
        super.addResourceHandlers(registry);
    }

}
