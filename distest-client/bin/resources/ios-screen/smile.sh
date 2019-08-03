#!/bin/sh

while [ true ]; do
	idevicescreenshot -u $1 $1.png  > ./log 2>&1
	magick convert $1.png -resize 30% $1.jpg
	echo $(cat $1.jpg | base64)
	sleep 0.01
done
