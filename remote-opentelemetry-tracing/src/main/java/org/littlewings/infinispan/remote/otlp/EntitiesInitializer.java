package org.littlewings.infinispan.remote.otlp;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "entity"
)
public interface EntitiesInitializer extends SerializationContextInitializer {
}
