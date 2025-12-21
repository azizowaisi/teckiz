package com.teckiz.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppInfoContributor implements InfoContributor {

    private final Environment environment;

    public AppInfoContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        details.put("name", environment.getProperty("app.name", "Teckiz"));
        details.put("version", environment.getProperty("app.version", "1.0.0"));
        details.put("description", "Teckiz application backend - Multi-tenant CMS platform");
        details.put("environment", environment.getProperty("app.is-dev-env", "false").equals("true") ? "development" : "production");
        
        Map<String, Object> build = new HashMap<>();
        build.put("java.version", System.getProperty("java.version"));
        build.put("spring.boot.version", org.springframework.boot.SpringBootVersion.getVersion());
        details.put("build", build);
        
        builder.withDetails(details);
    }
}

