#!/bin/bash

cd `dirname $0`

CONTAINERS=`cat servers`

SCRIPT=$(cat <<EOF
mkdir -p /opt/infinispan-server/modules/system/layers/base/org/littlewings/task/entity/main
cp /var/volume/scripts/entity-module.xml /opt/infinispan-server/modules/system/layers/base/org/littlewings/task/entity/main/module.xml
cp /var/volume/target/scala-2.12/entity.jar /opt/infinispan-server/modules/system/layers/base/org/littlewings/task/entity/main
cp /var/volume/scripts/infinispan-commons-module.xml /opt/infinispan-server/modules/system/layers/base/org/infinispan/commons/main/module.xml
pkill standalone
nohup sh /opt/infinispan-server/start-clustered-server.sh &
EOF
)


for C in $CONTAINERS
do
    docker exec $C bash -c "${SCRIPT}" &
done