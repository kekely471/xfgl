package com.tonbu;
//import com.bstek.ureport.console.UReportServlet;
import org.apache.catalina.connector.Connector;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootApplication(exclude={org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.activiti.spring.boot.SecurityAutoConfiguration.class})
@EnableScheduling
@EnableAsync
@EnableCaching
@EnableTransactionManagement
//@ImportResource("classpath:context.xml")
//@EnableJms //启动消息队列
public class WebApplication extends SpringBootServletInitializer {

    Logger logger = LogManager.getLogger(WebApplication.class.getName());

    @Value("${https.port}")
    private Integer port;

    @Value("${https.ssl.key-store-type}")
    private String key_store_type;

    @Value("${https.ssl.key-password}")
    private String key_password;

    @Value("${https.ssl.ssl.key-store}")
    private String key_store;

    @Value("${https.ssl.key-alias}")
    private String key_alias;

    @Value("${https.ssl.maxHttpHeaderSize}")
    private String maxHttpHeaderSize;


    /*@Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createSslConnector()); // 添加http
        return tomcat;
    }

    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        try {
            ClassPathResource resource = new ClassPathResource(key_store);
            // 临时目录
            String tempPath = System.getProperty("java.io.tmpdir") + System.currentTimeMillis() + ".keystore";
            logger.info("临时存储密钥的目录：" + tempPath);
            File f = new File(tempPath);
            IOUtils.copy(resource.getInputStream(), new FileOutputStream(f));
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(port);
            connector.setAttribute("maxHttpHeaderSize", maxHttpHeaderSize);
            connector.setURIEncoding("UTF-8");
            protocol.setSSLEnabled(true);
            protocol.setKeystoreFile(f.getAbsolutePath());
            protocol.setKeystoreType(key_store_type);
            protocol.setKeyAlias(key_alias);
            protocol.setKeyPass(key_password);
            protocol.setKeystorePass(key_password);

            return connector;

        } catch (IOException ex) {
            throw new IllegalStateException("can't access keystore: [" + "keystore"
                    + "] or truststore: [" + "keystore" + "]", ex);
        }
    }*/

    /**
     * 加载ureportServlet
     */
    /*@Bean
    public ServletRegistrationBean buildUreportServlet(){
        return new ServletRegistrationBean(new UReportServlet(),"/ureport/*");
    }*/

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplication.class);
    }


    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(WebApplication.class);
        application.run(args);
    }
    
}

