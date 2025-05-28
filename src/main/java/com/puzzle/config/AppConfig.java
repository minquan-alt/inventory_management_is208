package com.puzzle.config;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
public class AppConfig {

    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> requestContextListener(){
        return new ServletListenerRegistrationBean<>(new RequestContextListener());
    }
}
