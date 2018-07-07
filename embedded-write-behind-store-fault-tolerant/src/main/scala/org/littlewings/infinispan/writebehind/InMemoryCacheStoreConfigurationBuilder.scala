package org.littlewings.infinispan.writebehind

import org.infinispan.configuration.cache.{AbstractStoreConfiguration, AbstractStoreConfigurationBuilder, PersistenceConfigurationBuilder}

class InMemoryCacheStoreConfigurationBuilder(builder: PersistenceConfigurationBuilder)
  extends AbstractStoreConfigurationBuilder[InMemoryCacheStoreConfiguration, InMemoryCacheStoreConfigurationBuilder](builder, AbstractStoreConfiguration.attributeDefinitionSet) {
  override def self(): InMemoryCacheStoreConfigurationBuilder = this

  override def create(): InMemoryCacheStoreConfiguration = new InMemoryCacheStoreConfiguration(attributes.protect, async.create, singletonStore.create)
}
