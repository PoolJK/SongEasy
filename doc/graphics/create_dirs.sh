#! /bin/bash
echo Creating subdirs in directory ...
path=$(dirname $1)
mkdir  $1/drawable-ldpi
mkdir  $1//drawable-mdpi
mkdir  $1/drawable-hdpi
mkdir  $1/drawable-xhdpi
mkdir  $1/drawable-xxhdpi
mkdir  $1/drawable-xxxhdpi
echo done!
