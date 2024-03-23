package org.littlewings.infinispan.remote.proto3record;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.protostream.annotations.ProtoSyntax;

@ProtoSchema(
        includeClasses = {Book.class, IndexedBook.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "entity",
        syntax = ProtoSyntax.PROTO3
)
public interface EntitiesInitializer extends GeneratedSchema {
}
