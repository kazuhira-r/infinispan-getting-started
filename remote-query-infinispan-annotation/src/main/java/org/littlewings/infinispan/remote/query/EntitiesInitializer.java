package org.littlewings.infinispan.remote.query;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class, IndexedBook.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "entity"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
