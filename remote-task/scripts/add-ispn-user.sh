#!/bin/bash

cd `dirname $0`

CONTAINERS=`cat servers`

for C in $CONTAINERS
do
    docker exec $C bash -c 'bin/add-user.sh -u test -p testpassword'
done
