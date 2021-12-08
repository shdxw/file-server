#!/bin/bash

git reset --hard HEAD
git pull

mvn clean install

mvn package -P "$1" #выбираем какой профиль билдить

#java -jar file.jar