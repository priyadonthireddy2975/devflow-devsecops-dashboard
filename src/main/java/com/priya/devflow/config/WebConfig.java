package com.priya.devflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String route : new String[]{"/login", "/pipelines", "/security", "/repositories", "/settings"}) {
            registry.addViewController(route).setViewName("forward:/index.html");
        }
    }
}
