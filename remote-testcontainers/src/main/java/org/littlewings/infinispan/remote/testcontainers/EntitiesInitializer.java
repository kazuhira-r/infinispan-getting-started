package org.littlewings.infinispan.remote.testcontainer;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto",
        schemaPackageName = "org.littlewings.infinispan.remote.testcontainer"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
