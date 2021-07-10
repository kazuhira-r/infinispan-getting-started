package org.littlewings.infinispan.entity;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class, Result.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto",
        schemaPackageName = "org.littlewings.infinispan.entity"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
