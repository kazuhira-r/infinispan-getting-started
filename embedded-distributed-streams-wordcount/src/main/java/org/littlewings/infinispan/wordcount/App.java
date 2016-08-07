package org.littlewings.infinispan.wordcount;

import java.io.IOException;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    public static void main(String... args) {
        SpringApplication.run(App.class, args);
    }

    @Bean(destroyMethod = "stop")
    public EmbeddedCacheManager embeddedCacheManager() throws IOException {
        EmbeddedCacheManager cacheManager = new DefaultCacheManager("infinispan.xml");
        cacheManager.startCaches(cacheManager.getCacheNames().toArray(new String[cacheManager.getCacheNames().size()]));
        return cacheManager;
    }
}
