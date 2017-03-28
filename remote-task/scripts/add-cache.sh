#!/bin/bash

cd `dirname $0`

CONTAINERS=`cat servers`

for C in $CONTAINERS
do
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/configurations=CONFIGURATIONS/distributed-cache-configuration=simple:add(start=EAGER,mode=SYNC,owners=2)"'
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/distributed-cache=simpleCache:add(configuration=simple)"'
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/distributed-cache=bookCache:add(configuration=simple)"'

    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/configurations=CONFIGURATIONS/distributed-cache-configuration=compatibility:add(start=EAGER,mode=SYNC,owners=2)"'
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/configurations=CONFIGURATIONS/distributed-cache-configuration=compatibility/compatibility=COMPATIBILITY:add(enabled=true)"'

#    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/distributed-cache=simpleCache:add(configuration=compatibility)"'

#    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/distributed-cache=bookCache:add(configuration=compatibility)"'
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="/subsystem=datagrid-infinispan/cache-container=clustered/distributed-cache=compatibilityBookCache:add(configuration=compatibility)"'
done