package org.littlewings.infinispan.remote.vectorsearch;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.protostream.annotations.ProtoSyntax;

@ProtoSchema(
        includeClasses = {Movie.class},
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "entity",
        syntax = ProtoSyntax.PROTO3
)
public interface EntitiesInitializer extends GeneratedSchema {
}
