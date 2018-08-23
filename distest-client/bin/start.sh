#!/bin/sh

adb kill-server

username=""
password=""

while getopts "u:p:" opt; do
    case $opt in
        u)
            username="$OPTARG"
            ;;
        p)
            password="$OPTARG"
            ;;
        \?)
            break;;
        esac
done

if [ ! $username ]; then
 echo "-u IS NULL"
 exit 1
fi
if [ ! $password ]; then
 echo "-p IS NULL"
 exit 1
fi

export ANDROID_HOME=/home/testwa/agent/sdk
export PATH=$PATH:$ANDROID_HOME:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

nodepath=""
resourcepath="/home/testwa/agent/resources"
appiumapth="/home/testwa/agent/wappium/index.js"


nohup java -jar -server -Xmx1024m -Xms2048m --spring.profiles.active=test distest-client-1.0-SNAPSHOT.jar --username=$username --password=$password --node.excute.path=$nodepath --distest.agent.resources=$resourcepath --appium.js.path=$appiumapth > /dev/null 2>&1 &

echo $! > tpid

echo Start Success!