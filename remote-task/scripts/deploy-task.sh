#!/bin/bash

cd `dirname $0`

CONTAINERS=`cat servers`

for C in $CONTAINERS
do
    docker exec $C bash -c 'bin/ispn-cli.sh -u=test -p=testpassword -c --command="deploy --force /var/volume/target/scala-2.12/remote-task.jar"'
done
