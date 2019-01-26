package org.littlewings.infinispan.okd;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

@Dependent
public class RemoteCacheManagerProducer {
    @Produces
    public RemoteCacheManager remoteCacheManager() {
        return new RemoteCacheManager(
                new ConfigurationBuilder()
                        .addServer()
                        .host("infinispan-server")
                        .port(11222)
                        .build()
        );
    }

    public void destroy(@Disposes RemoteCacheManager remoteCacheManager) {
        remoteCacheManager.stop();
    }
}
