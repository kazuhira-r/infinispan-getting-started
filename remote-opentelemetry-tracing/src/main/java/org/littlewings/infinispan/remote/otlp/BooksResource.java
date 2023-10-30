package org.littlewings.infinispan.remote.otlp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.Comparator;
import java.util.List;

@Path("books")
@ApplicationScoped
public class BooksResource {
    private RemoteCacheManager remoteCacheManager;
    private RemoteCache<String, Book> bookCache;

    @PostConstruct
    public void init() {
        /*
        String uri = """
                hotrod://ispn-user:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222\
                ?tracing.propagation_enabled=false\
                &context-initializers=org.littlewings.infinispan.remote.otlp.EntitiesInitializerImpl\
                """;
         */

        String uri = """
                hotrod://ispn-user:password@172.18.0.2:11222,172.18.0.3:11222,172.18.0.4:11222\
                ?context-initializers=org.littlewings.infinispan.remote.otlp.EntitiesInitializerImpl\
                """;

        Configuration configuration = new ConfigurationBuilder()
                .uri(uri)
                .build();
        remoteCacheManager = new RemoteCacheManager(configuration);

        bookCache = remoteCacheManager.getCache("bookCache");
    }

    @PreDestroy
    public void destroy() {
        remoteCacheManager.close();
    }

    @GET
    @Path("{isbn}")
    @Produces(MediaType.APPLICATION_JSON)
    public Book find(@PathParam("isbn") String isbn) {
        return bookCache.get(isbn);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Book> findAll() {
        return bookCache
                .values()
                .stream()
                .sorted(Comparator.<Book, Integer>comparing(b -> b.getPrice()).reversed())
                .toList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Book register(Book book) {
        bookCache.put(book.getIsbn(), book);

        return book;
    }
}
