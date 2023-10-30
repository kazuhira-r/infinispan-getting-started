package org.littlewings.infinispan.remote.otlp;

import jakarta.ws.rs.SeBootstrap;
import org.jboss.logging.Logger;

import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String... args) throws ExecutionException, InterruptedException {
        Logger logger = Logger.getLogger(App.class);

        SeBootstrap.Configuration configuration =
                SeBootstrap
                        .Configuration
                        .builder()
                        .host("0.0.0.0")
                        .port(8080)
                        .build();

        SeBootstrap.Instance instance =
                SeBootstrap
                        .start(new JaxrsActivator(), configuration)
                        .toCompletableFuture()
                        .get();

        logger.info("server startup.");
        System.console().readLine("> Enter stop.");

        instance
                .stop()
                .toCompletableFuture()
                .get();
    }
}
