#!/bin/bash

cat <<EOF
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

EOF

for METHOD in `curl https://raw.githubusercontent.com/infinispan/infinispan/8.2.2.Final/core/src/main/java/org/infinispan/commands/Visitor.java 2> /dev/null | grep visit | perl -wp -e 's!.+ (visit[^\(]+).+!$1!'`
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
