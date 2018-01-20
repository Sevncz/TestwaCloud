#!/usr/bin/env bash

mvn clean compile package -Dmaven.test.skip=true
scp target/distest-web-1.0-SNAPSHOT.jar wadmin@139.129.219.167:/data/www/wacenter/