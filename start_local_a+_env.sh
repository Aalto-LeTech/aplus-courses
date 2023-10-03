#!/bin/bash

# preparing and compiling the A+ env components (https://apluslms.github.io/guides/quick/)
git clone --depth 1 https://github.com/apluslms/aplus-manual.git my_new_course &&
cd my_new_course &&
wget https://github.com/mikefarah/yq/releases/download/v4.16.2/yq_linux_amd64 && chmod +x yq_linux_amd64 &&
./yq_linux_amd64 eval -i '.services.plus.volumes += ["../tools/aplus-init.sh:/srv/aplus-init.sh:ro", "../tools/simple_course.py:/srv/simple_course.py:ro"]' docker-compose.yml &&
git submodule init &&
git submodule update &&
chmod +x docker-compile.sh && ./docker-compile.sh &&
# modifying the initial script (just cutting off the last part of it), so it would run in the background
head -104 docker-up.sh > docker-up-custom.sh &&
# adding an execution permission to the newly created script
chmod +x docker-up-custom.sh &&
./docker-up-custom.sh &&
# wait for all the components of A+ to start
i=0 &&

until curl --output /dev/null --silent --head --fail http://localhost:8000; do
      if [[ ${i} -eq 100 ]];then
        echo "Could not start A+"
        exit 1
      fi
    sleep 5
    i=$((i+1))
done