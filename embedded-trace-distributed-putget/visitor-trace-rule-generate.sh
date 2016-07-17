#!/bin/bash

cat <<EOF
RULE trace DataContainer.get entry
INTERFACE ^org.infinispan.container.DataContainer
METHOD get
AT ENTRY
IF TRUE
  DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":get, entry")
ENDRULE

RULE trace DataContainer.get exit
INTERFACE ^org.infinispan.container.DataContainer
METHOD get
AT EXIT
IF TRUE
  DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":get, exit")
ENDRULE

# RULE trace DataContainer.peek entry
# INTERFACE ^org.infinispan.container.DataContainer
# METHOD peek
# AT ENTRY
# IF TRUE
#   DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":peek, entry")
# ENDRULE
#  
# RULE trace DataContainer.peek exit
# INTERFACE ^org.infinispan.container.DataContainer
# METHOD peek
# AT EXIT
# IF TRUE
#   DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":peek, exit")
# ENDRULE

RULE trace DataContainer.put entry
INTERFACE ^org.infinispan.container.DataContainer
METHOD put
AT ENTRY
IF TRUE
  DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":put, entry")
ENDRULE

RULE trace DataContainer.put exit
INTERFACE ^org.infinispan.container.DataContainer
METHOD put
AT EXIT
IF TRUE
  DO traceln("[DataContainer] " + \$0.getClass().getSimpleName() + ":put, exit")
ENDRULE

RULE trace EntryLookup.lookupEntry entry
INTERFACE ^org.infinispan.context.EntryLookup
METHOD lookupEntry
AT ENTRY
IF TRUE
  DO traceln("[EntryLookup] " + \$0.getClass().getSimpleName() + ":lookupEntry, entry")
ENDRULE

RULE trace EntryLookup.lookupEntry exit
INTERFACE ^org.infinispan.context.EntryLookup
METHOD lookupEntry
AT EXIT
IF TRUE
  DO traceln("[EntryLookup] " + \$0.getClass().getSimpleName() + ":lookupEntry, exit")
ENDRULE

RULE trace EntryLookup.putLookedUpEntry entry
INTERFACE ^org.infinispan.context.EntryLookup
METHOD putLookedUpEntry
AT ENTRY
IF TRUE
  DO traceln("[EntryLookup] " + \$0.getClass().getSimpleName() + ":putLookedUpEntry, entry")
ENDRULE

RULE trace EntryLookup.putLookedUpEntry exit
INTERFACE ^org.infinispan.context.EntryLookup
METHOD putLookedUpEntry
AT EXIT
IF TRUE
  DO traceln("[EntryLookup] " + \$0.getClass().getSimpleName() + ":putLookedUpEntry, exit")
ENDRULE

RULE trace EntryFactory.wrapEntryForReading entry
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapEntryForReading
AT ENTRY
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapEntryForReading, entry")
ENDRULE

RULE trace EntryFactory.wrapEntryForReading exit
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapEntryForReading
AT EXIT
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapEntryForReading, exit")
ENDRULE

RULE trace EntryFactory.wrapEntryForWriting entry
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapEntryForWriting
AT ENTRY
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapEntryForWriting, entry")
ENDRULE

RULE trace EntryFactory.wrapEntryForWriting exit
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapEntryForWriting
AT EXIT
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapEntryForWriting, exit")
ENDRULE

RULE trace EntryFactory.wrapExternalEntry entry
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapExternalEntry
AT ENTRY
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapExternalEntry, entry")
ENDRULE

RULE trace EntryFactory.wrapExternalEntry exit
INTERFACE ^org.infinispan.container.EntryFactory
METHOD wrapExternalEntry
AT EXIT
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":wrapExternalEntry, exit")
ENDRULE

RULE trace EntryFactory.getFromContext entry
INTERFACE ^org.infinispan.container.EntryFactory
METHOD getFromContext
AT ENTRY
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":getFromContext, entry")
ENDRULE

RULE trace EntryFactory.getFromContext exit
INTERFACE ^org.infinispan.container.EntryFactory
METHOD getFromContext
AT EXIT
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":getFromContext, exit")
ENDRULE

RULE trace EntryFactory.getFromContainer entry
INTERFACE ^org.infinispan.container.EntryFactory
METHOD getFromContainer
AT ENTRY
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":getFromContainer, entry")
ENDRULE

RULE trace EntryFactory.getFromContainer exit
INTERFACE ^org.infinispan.container.EntryFactory
METHOD getFromContainer
AT EXIT
IF TRUE
  DO traceln("[EntryFactory] " + \$0.getClass().getSimpleName() + ":getFromContainer, exit")
ENDRULE

RULE trace Marshaller.objectFromByteBuffer entry
INTERFACE ^org.infinispan.commons.marshall.Marshaller
METHOD objectFromByteBuffer
AT ENTRY
IF TRUE
  DO traceln("[Marshaller] " + \$0.getClass().getSimpleName() + ":objectFromByteBuffer, entry")
ENDRULE
 
RULE trace Marshaller.objectFromByteBuffer exit
INTERFACE ^org.infinispan.commons.marshall.Marshaller
METHOD objectFromByteBuffer
AT EXIT
IF TRUE
  DO traceln("[Marshaller] " + \$0.getClass().getSimpleName() + ":objectFromByteBuffer, exit")
ENDRULE
 
RULE trace Marshaller.objectToBuffer entry
INTERFACE ^org.infinispan.commons.marshall.Marshaller
METHOD objectToBuffer
AT ENTRY
IF TRUE
  DO traceln("[Marshaller] " + \$0.getClass().getSimpleName() + ":objectToBuffer, entry")
ENDRULE
 
RULE trace Marshaller.objectToBuffer exit
INTERFACE ^org.infinispan.commons.marshall.Marshaller
METHOD objectToBuffer
AT EXIT
IF TRUE
  DO traceln("[Marshaller] " + \$0.getClass().getSimpleName() + ":objectToBuffer, exit")
ENDRULE

RULE trace ReplicableCommand.perform entry
INTERFACE ^org.infinispan.commands.ReplicableCommand
METHOD perform
AT ENTRY
IF TRUE
  DO traceln("[Command] " + \$0.getClass().getSimpleName() + ":perform, entry")
ENDRULE

RULE trace ReplicableCommand.perform exit
INTERFACE ^org.infinispan.commands.ReplicableCommand
METHOD perform
AT EXIT
IF TRUE
  DO traceln("[Command] " + \$0.getClass().getSimpleName() + ":perform, exit")
ENDRULE

RULE trace BaseDistributionInterceptor.retrieveFromRemoteSource entry
INTERFACE ^org.infinispan.interceptors.distribution.BaseDistributionInterceptor
METHOD retrieveFromRemoteSource
AT ENTRY
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":retrieveFromRemoteSource, entry")
ENDRULE

RULE trace BaseDistributionInterceptor.retrieveFromRemoteSource exit
INTERFACE ^org.infinispan.interceptors.distribution.BaseDistributionInterceptor
METHOD retrieveFromRemoteSource
AT EXIT
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":retrieveFromRemoteSource, exit")
ENDRULE

RULE trace BaseDistributionInterceptor.invokeClusterGetCommandRemotely entry
INTERFACE ^org.infinispan.interceptors.distribution.BaseDistributionInterceptor
METHOD invokeClusterGetCommandRemotely
AT ENTRY
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":invokeClusterGetCommandRemotely, entry")
ENDRULE

RULE trace BaseDistributionInterceptor.invokeClusterGetCommandRemotely exit
INTERFACE ^org.infinispan.interceptors.distribution.BaseDistributionInterceptor
METHOD invokeClusterGetCommandRemotely
AT EXIT
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":invokeClusterGetCommandRemotely, exit")
ENDRULE

RULE trace RpcManager.invokeRemotely entry
INTERFACE ^org.infinispan.remoting.rpc.RpcManager
METHOD invokeRemotely
AT ENTRY
IF TRUE
  DO traceln("[RpcManager] " + \$0.getClass().getSimpleName() + ":invokeRemotely, entry")
ENDRULE

RULE trace RpcManager.invokeRemotely exit
INTERFACE ^org.infinispan.remoting.rpc.RpcManager
METHOD invokeRemotely
AT EXIT
IF TRUE
  DO traceln("[RpcManager] " + \$0.getClass().getSimpleName() + ":invokeRemotely, exit")
ENDRULE

# RULE trace RpcManager.invokeRemotelyAsync entry
# INTERFACE ^org.infinispan.remoting.rpc.RpcManager
# METHOD invokeRemotelyAsync
# AT ENTRY
# IF TRUE
#   DO traceln("[RpcManager] " + \$0.getClass().getSimpleName() + ":invokeRemotelyAsync, entry")
# ENDRULE
#  
# RULE trace RpcManager.invokeRemotelyAsync exit
# INTERFACE ^org.infinispan.remoting.rpc.RpcManager
# METHOD invokeRemotelyAsync
# AT EXIT
# IF TRUE
#   DO traceln("[RpcManager] " + \$0.getClass().getSimpleName() + ":invokeRemotelyAsync, exit")
# ENDRULE

EOF

for METHOD in `curl https://raw.githubusercontent.com/infinispan/infinispan/8.2.3.Final/core/src/main/java/org/infinispan/commands/Visitor.java 2> /dev/null | grep visit | perl -wp -e 's!.+ (visit[^\(]+).+!$1!'`
do

cat <<EOF
RULE trace Visitor.${METHOD} entry
INTERFACE ^org.infinispan.commands.Visitor
METHOD ${METHOD}
AT ENTRY
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":${METHOD}, entry")
ENDRULE

RULE trace Visitor.${METHOD} exit
INTERFACE ^org.infinispan.commands.Visitor
METHOD ${METHOD}
AT EXIT
IF TRUE
  DO traceln("[Interceptor] " + \$0.getClass().getSimpleName() + ":${METHOD}, exit")
ENDRULE

EOF

done
