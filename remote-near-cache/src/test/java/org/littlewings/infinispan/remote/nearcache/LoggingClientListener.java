package org.littlewings.infinispan.remote.nearcache;

import org.infinispan.client.hotrod.annotation.*;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;

@ClientListener
public class LoggingClientListener {
    @ClientCacheEntryCreated
    public void handleCeatedEvent(ClientCacheEntryCreatedEvent event) {
        System.out.printf("created, key = %s%n", event.getKey());
    }

    @ClientCacheEntryModified
    public void handleModifiedEvent(ClientCacheEntryModifiedEvent event) {
        System.out.printf("modified, key = %s%n", event.getKey());
    }

    @ClientCacheEntryRemoved
    public void handleRemovedEvent(ClientCacheEntryRemovedEvent event) {
        System.out.printf("removed, key = %s%n", event.getKey());
    }

    @ClientCacheEntryExpired
    public void handleExpiredEvent(ClientCacheEntryExpiredEvent event) {
        System.out.printf("expired, key = %s%n", event.getKey());
    }
}
