#!/bin/bash

cd `dirname $0`

cd ../target/scala-2.12

jar -cvf entity.jar -C classes org/littlewings/infinispan/task/entity/Book.class
