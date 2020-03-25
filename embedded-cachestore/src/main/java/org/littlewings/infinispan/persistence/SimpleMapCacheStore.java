package org.littlewings.infinispan.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.commons.configuration.ConfiguredBy;
import org.infinispan.commons.io.ByteBuffer;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.persistence.Store;
import org.infinispan.configuration.cache.CustomStoreConfiguration;
import org.infinispan.persistence.spi.ExternalStore;
import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.persistence.spi.MarshallableEntry;
import org.infinispan.persistence.spi.MarshallableEntryFactory;
import org.infinispan.persistence.spi.PersistenceException;
import org.jboss.logging.Logger;

@Store
@ConfiguredBy(SimpleMapCacheStoreConfiguration.class)
public class SimpleMapCacheStore<K, V> implements ExternalStore<K, V> {
    Logger logger = Logger.getLogger(getClass());

    SimpleMapCacheStoreConfiguration storeConfiguration;
    //CustomStoreConfiguration storeConfiguration;
    String storeName;

    Map<ByteBuffer, EntryData> store;

    InitializationContext ctx;

    @Override
    public void init(InitializationContext ctx) {
        this.ctx = ctx;
        storeConfiguration = ctx.getConfiguration();

        if (storeConfiguration.properties().getProperty("storeName") != null) {
            storeName = storeConfiguration.properties().getProperty("storeName");
        } else {
            storeName = "defaultStoreName";
        }

        logging("initialized");
    }

    @Override
    public void write(MarshallableEntry<? extends K, ? extends V> entry) {
        ByteBuffer keyAsBinary = entry.getKeyBytes();
        ByteBuffer valueAsBinary = entry.getValueBytes();

        long created = entry.created();
        long expiryTime = entry.expiryTime();
        long lastUsed = entry.lastUsed();

        ByteBuffer metadataAsBinary = entry.getMetadataBytes();

        EntryData entryData = EntryData.create(valueAsBinary, created, expiryTime, lastUsed, metadataAsBinary);

        store.put(keyAsBinary, entryData);

        logging(
                "write entry: %s / %s, created = %d, expiryTime = %d, lastUsed = %d, metadata = %s",
                entry.getKey(),
                entry.getValue(),
                entry.created(),
                entry.expiryTime(),
                entry.lastUsed(),
                entry.getMetadata()
        );

    }

    @Override
    public MarshallableEntry<K, V> loadEntry(Object key) {
        MarshallableEntryFactory<K, V> marshallableEntryFactory = ctx.getMarshallableEntryFactory();
        Marshaller marshaller = ctx.getPersistenceMarshaller().getUserMarshaller();

        try {
            ByteBuffer keyAsBinary = marshaller.objectToBuffer(key);
            EntryData entryData = store.get(keyAsBinary);

            if (entryData != null) {
                MarshallableEntry<K, V> marshallableEntry =
                        marshallableEntryFactory.create(keyAsBinary, entryData.valueAsBinry, entryData.metadataAsBinary, entryData.getCreated(), entryData.getLastUsed());

                logging(
                        "load entry: %s / %s, created = %d, expiryTime = %d, lastUsed = %d, metadata = %s",
                        marshallableEntry.getKey(),
                        marshallableEntry.getValue(),
                        marshallableEntry.created(),
                        marshallableEntry.expiryTime(),
                        marshallableEntry.lastUsed(),
                        marshallableEntry.getMetadata()
                );
                return marshallableEntry;
            } else {
                logging("missing entry: %s", key);
                return null;
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public boolean delete(Object key) {
        try {
            Marshaller marshaller = ctx.getPersistenceMarshaller();
            ByteBuffer keyAsBinary = marshaller.objectToBuffer(key);
            EntryData deleted = store.remove(keyAsBinary);

            logging("deleted? %b, key = %s", deleted != null, key);

            return deleted != null;
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean contains(Object key) {
        try {
            Marshaller marshaller = ctx.getPersistenceMarshaller();
            ByteBuffer keyAsBinary = marshaller.objectToBuffer(key);

            boolean contains = store.containsKey(keyAsBinary);

            logging("contains? %b, key = %s", contains, key);

            return contains;
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void start() {
        store = new ConcurrentHashMap<>();

        logging("cache store, startup");
    }

    @Override
    public void stop() {
        store.clear();

        logging("cache store, stopped");
    }

    void logging(String messageTemplate, Object... params) {
        List<Object> mergeParams = new ArrayList<>();
        mergeParams.add(ctx.getCache().getAdvancedCache().getDistributionManager().getCacheTopology().getLocalAddress());
        mergeParams.add(getClass().getSimpleName());
        mergeParams.add(storeName);
        mergeParams.addAll(Arrays.asList(params));

        logger.infof("[%s - %s - %s] " + messageTemplate, mergeParams.toArray());
    }
}
