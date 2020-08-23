package org.littlewings.infinispan.query.simply;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class, IndexedBook.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "entities"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
