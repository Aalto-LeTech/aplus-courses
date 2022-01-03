#!/bin/bash

# preparing and compiling the A+ env components (https://apluslms.github.io/guides/quick/)
git clone https://github.com/apluslms/a-plus.git &&
git clone https://github.com/Aalto-LeTech/course-templates.git my_new_course &&
cd my_new_course &&
git submodule init &&
git submodule update &&
./docker-compile.sh &&
# modifying the initial script (just cutting off the last part of it), so it would run in the background
head -102 docker-up.sh > docker-up-custom.sh &&
# adding an execution permission to the newly created script
chmod +x docker-up-custom.sh &&
./docker-up-custom.sh &&
# wait for all the components of A+ to start
until curl --output /dev/null --silent --head --fail http://localhost:8000; do
    sleep 5
done