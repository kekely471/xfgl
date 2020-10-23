package com.tonbu.support.config;

import com.tonbu.support.filter.ShiroPermissionsFilter;
import com.tonbu.support.shiro.CustomRealm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    Logger logger = LogManager.getLogger(ShiroConfig.class.getName());


    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        logger.info("--------------Shiro拦截器工厂类开始注入----------------");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        Map<String,Filter> filters=shiroFilterFactoryBean.getFilters();
        filters.put("perms",new ShiroPermissionsFilter());
        shiroFilterFactoryBean.setFilters(filters);
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/importantMobile/**", "anon");
        filterChainDefinitionMap.put("/xxfb/**", "anon");
        filterChainDefinitionMap.put("/mobile/**", "anon");
        filterChainDefinitionMap.put("/api/**", "anon");
        filterChainDefinitionMap.put("/ace/**", "anon");
        filterChainDefinitionMap.put("/easyui/**", "anon");
        filterChainDefinitionMap.put("/layui/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/commons/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/error/**", "anon");
        filterChainDefinitionMap.put("/index", "anon");
        filterChainDefinitionMap.put("/wangeditor", "anon");
        //swagger-ui拦截
        filterChainDefinitionMap.put("/doc.html*", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        //ajax登录认证
        filterChainDefinitionMap.put("/check", "anon");
        //登录页面
        filterChainDefinitionMap.put("/login", "anon");

        filterChainDefinitionMap.put("/files/view/**", "anon");
        filterChainDefinitionMap.put("/file/download/**", "anon");
        //手机端登录接口
        filterChainDefinitionMap.put("/api/**", "anon");

        //配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        //<!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
        //<!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
        filterChainDefinitionMap.put("/**", "authc");
        filterChainDefinitionMap.put("/*", "authc");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/*.*", "authc");

        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/main");

        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauth");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        logger.info("--------------Shiro拦截器工厂类注入成功----------------");
        return shiroFilterFactoryBean;
    }



/*
    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        Map<String,Filter> filters=shiroFilterFactoryBean.getFilters();
        shiroFilterFactoryBean.setFilters(filters);
        filters.put("perms",new ShiroPermissionsFilter());
        // Shiro的核心安全接口,这个属性是必须的
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 注销成功，则跳转到指定页面

        // 身份认证失败，则跳转到登录页面的配置
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/main");
        // 权限认证失败，则跳转到指定页面
        // Shiro连接约束配置，即过滤链的定义
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 对静态资源设置匿名访问
        filterChainDefinitionMap.put("/api/**", "anon");
        filterChainDefinitionMap.put("/ace/**", "anon");
        filterChainDefinitionMap.put("/easyui/**", "anon");
        filterChainDefinitionMap.put("/layui/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/commons/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/error/**", "anon");
        filterChainDefinitionMap.put("/index", "anon");
        //ajax登录认证
        filterChainDefinitionMap.put("/check", "anon");
        //登录页面
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/main", "anon");

        filterChainDefinitionMap.put("/files/view/**", "anon");
        filterChainDefinitionMap.put("/file/download/**", "anon");
        //手机端登录接口
        filterChainDefinitionMap.put("/api/**", "anon");

        //配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");

        //<!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
        //<!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
        filterChainDefinitionMap.put("/**", "authc");
        filterChainDefinitionMap.put("/*", "authc");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/*.*", "authc");
        // 系统权限列表
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauth");


        // 所有请求需要认证
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }
*/


    /**
     * 注入 securityManager
     */
    @Bean
    public SecurityManager securityManager() {
        logger.info("--------------shiro安全事务管理器已经加载----------------");
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(customRealm());
        return securityManager;
    }

    /**
     * 自定义身份认证 realm;
     * <p>
     * 必须写这个类，并加上 @Bean 注解，目的是注入 CustomRealm，
     * 否则会影响 CustomRealm类 中其他类的依赖注入
     */
    @Bean
    public CustomRealm customRealm() {
        return new CustomRealm();
    }
}
