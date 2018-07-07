package org.littlewings.infinispan.writebehind

import org.infinispan.commons.configuration.attributes.AttributeSet
import org.infinispan.commons.configuration.{BuiltBy, ConfigurationFor}
import org.infinispan.configuration.cache.{AbstractStoreConfiguration, AsyncStoreConfiguration, SingletonStoreConfiguration}

@BuiltBy(classOf[InMemoryCacheStoreConfigurationBuilder])
@ConfigurationFor(classOf[InMemoryCacheStore[_, _]])
class InMemoryCacheStoreConfiguration(
                                       attributes: AttributeSet,
                                       async: AsyncStoreConfiguration,
                                       singletonStore: SingletonStoreConfiguration
                                     ) extends AbstractStoreConfiguration(attributes, async, singletonStore) {
}
