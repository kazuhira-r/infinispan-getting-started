package org.littlewings.infinispan.okd;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

@ApplicationScoped
@Path("remote")
public class RemoteCacheResource {
    @Inject
    RemoteCacheManager remoteCacheManager;

    @POST
    @Path("cache")
    @Consumes(MediaType.APPLICATION_JSON)
    public String put(Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");

        RemoteCache<String, String> cache = remoteCacheManager.getCache("default");
        cache.put(key, value);

        return "OK!!";
    }

    @GET
    @Path("cache/{key}")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@PathParam("key") String key) {
        RemoteCache<String, String> cache = remoteCacheManager.getCache("default");
        return cache.get(key);
    }

    @GET
    @Path("servers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> servers() {
        return Arrays.asList(remoteCacheManager.getServers());
    }
}
