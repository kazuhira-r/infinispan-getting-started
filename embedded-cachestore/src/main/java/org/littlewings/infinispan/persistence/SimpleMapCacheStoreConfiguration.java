package org.littlewings.infinispan.persistence;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.AsyncStoreConfiguration;

@BuiltBy(SimpleMapCacheStoreConfigurationBuilder.class)
@ConfigurationFor(SimpleMapCacheStore.class)
public class SimpleMapCacheStoreConfiguration extends AbstractStoreConfiguration {
    public SimpleMapCacheStoreConfiguration(AttributeSet attributes, AsyncStoreConfiguration async) {
        super(attributes, async);
    }
}
