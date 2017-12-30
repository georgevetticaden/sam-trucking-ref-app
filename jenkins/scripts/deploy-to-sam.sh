#!/usr/bin/env bash

echo 'Starting Deployment of updated Trucking Ref Application to SAM'
set -x
NAME=`mvn help:evaluate -Dexpression=project.name | grep "^[^\[]"`
set +x

set -x
VERSION=`mvn help:evaluate -Dexpression=project.version | grep "^[^\[]"`
set +x


set -x
java -cp target/${NAME}-${VERSION}-shaded.jar  hortonworks.hdf.sam.refapp.trucking.deploy.DeployTruckingRefApp jenkins/app-properties/trucking-ref-app.properties