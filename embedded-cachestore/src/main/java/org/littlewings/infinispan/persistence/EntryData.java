package org.littlewings.infinispan.persistence;

import org.infinispan.commons.io.ByteBuffer;
import org.infinispan.metadata.Metadata;

public class EntryData {
    ByteBuffer valueAsBinry;
    long created;
    long expiryTime;
    long lastUsed;
    ByteBuffer metadataAsBinary;

    public static EntryData create(ByteBuffer valueAsBinary, long created, long expiryTime, long lastUsed, ByteBuffer metadataAsBinary) {
        EntryData data = new EntryData();
        data.valueAsBinry = valueAsBinary;
        data.created = created;
        data.expiryTime = expiryTime;
        data.lastUsed = lastUsed;
        data.metadataAsBinary = metadataAsBinary;

        return data;
    }

    public ByteBuffer getValueAsBinry() {
        return valueAsBinry;
    }

    public long getCreated() {
        return created;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public ByteBuffer getMetadataAsBinary() {
        return metadataAsBinary;
    }
}
