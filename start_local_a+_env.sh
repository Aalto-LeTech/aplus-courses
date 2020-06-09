#!/bin/bash

# preparing and compiling the A+ env components (https://apluslms.github.io/guides/quick/)
git clone https://github.com/Aalto-LeTech/course-templates.git my_new_course &&
cd my_new_course &&
git submodule init &&
git submodule update &&
./docker-compile.sh &&
# modifying the initial script (just cutting off the last part of it), so it would run in the background
head -97 docker-up.sh > docker-up-custom.sh &&
# adding an execution permission to the newly created script
chmod +x docker-up-custom.sh &&
./docker-up-custom.sh &&
# wait for all the components of A+ to start
sleep 2m &&
# REMOVE NEXT 3 (THREE) LINES AFTER CONFIGURING ACTUAL INTEGRATION TESTS!
ls -al &&
docker ps &&
netstat -tulpn &&
# this is an example call to one of the components (for details check https://apluslms.github.io/guides/quick/)
curl --user root:root http://localhost:8000/api/v2/courses/
# groups API endpoint (populated)
curl --user root:root http://localhost:8000/api/v2/courses/1/groups/