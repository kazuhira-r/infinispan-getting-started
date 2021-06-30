package org.littlewings.infinispan.distexec.protostream.entity;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {ProtoBook.class, ProtoSummary.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto",
        schemaPackageName = "entity"
)
public interface EntityInitializer extends SerializationContextInitializer {
}
