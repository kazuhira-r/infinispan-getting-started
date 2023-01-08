package org.littlewings.infinispan.remote.newclient;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {
                Book.class
        },
        schemaFileName = "entities.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "remote_newclient")
public interface EntitiesInitializer extends GeneratedSchema {
}
