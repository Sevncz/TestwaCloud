#!/usr/bin/env bash

mvn clean compile package -Dmaven.test.skip=true
scp target/webcenter-0.0.1-SNAPSHOT.jar wadmin@139.129.219.167:/data/www/wacenter/