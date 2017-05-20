#!/bin/sh

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

export ANDROID_HOME=/Users/wen/dev/android-sdk-macosx
export PATH=$PATH:$ANDROID_HOME:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

nohup java -jar agent-daemon-server-1.0-SNAPSHOT.jar --spring.config.location=config/application.properties --username=$username --password=$password > /dev/null 2>&1 &

echo $! > tpid

echo Start Success!