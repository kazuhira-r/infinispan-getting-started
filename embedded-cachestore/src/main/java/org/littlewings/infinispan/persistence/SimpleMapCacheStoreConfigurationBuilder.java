package org.littlewings.infinispan.persistence;

import org.infinispan.commons.configuration.ConfigurationBuilderInfo;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

public class SimpleMapCacheStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<SimpleMapCacheStoreConfiguration, SimpleMapCacheStoreConfigurationBuilder> implements ConfigurationBuilderInfo {
    public SimpleMapCacheStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
        super(builder, AbstractStoreConfiguration.attributeDefinitionSet());
    }

    @Override
    public SimpleMapCacheStoreConfiguration create() {
        return new SimpleMapCacheStoreConfiguration(attributes.protect(), async.create());
    }

    @Override
    public SimpleMapCacheStoreConfigurationBuilder self() {
        return this;
    }
}
