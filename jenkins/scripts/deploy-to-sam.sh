#!/usr/bin/env bash

echo 'Starting Deployment of updated Trucking Ref Application to SAM'
set -x
NAME=`mvn help:evaluate -Dexpression=project.name | grep "^[^\[]"`
set +x

set -x
VERSION=`mvn help:evaluate -Dexpression=project.version | grep "^[^\[]"`
set +x


set -x
java -cp target/${NAME}-${VERSION}-shaded.jar  hortonworks.hdf.sam.refapp.trucking.deploy.DeployTruckingRefAdvancedApp jenkins/app-properties/trucking-advanced-ref-app.properties