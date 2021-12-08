#!/bin/bash

git push -u origin master
#переход в папку удаленную
docker exec -w /tmp/file-server/scripts nervous_moser bash -c 'chmod 755 build.sh'
docker exec -w /tmp/file-server/scripts nervous_moser bash -c './build.sh prod'