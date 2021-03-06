package org.littlewings.infinispan.marshalling;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {Book.class},
        schemaFileName = "book.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "sample")
public interface BookContextInitializer extends SerializationContextInitializer {
}
