package org.littlewings.infinispan.remote.testcontainers;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto",
        schemaPackageName = "org.littlewings.infinispan.remote.testcontainers"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
