#!/bin/bash

git push -u origin master
#переход в папку удаленную
docker exec -w /tmp/file-server ba6acccedd29 bash -c './build.sh dev'
