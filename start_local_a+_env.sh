#!/bin/bash

git clone https://github.com/apluslms/course-templates.git my_new_course &&
cd my_new_course &&
git submodule init &&
git submodule update &&
./docker-compile.sh &&
head -97 docker-up.sh > docker-up-custom.sh &&
chmod +x docker-up-custom.sh &&
ls -al &&
docker ps &&
netstat -tulpn &&
./docker-up-custom.sh &&
sleep 2m &&
docker ps &&
netstat -tulpn &&
curl --user root:root http://localhost:8000/api/v2/courses/