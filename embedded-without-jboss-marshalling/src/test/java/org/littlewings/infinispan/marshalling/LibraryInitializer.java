package org.littlewings.infinispan.marshalling;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {
                Book.class
        },
        schemaFileName = "library.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "book")
public interface LibraryInitializer extends SerializationContextInitializer {
}
