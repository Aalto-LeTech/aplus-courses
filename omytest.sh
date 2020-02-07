#!/bin/bash

git clone https://github.com/apluslms/course-templates.git my_new_course \
 && cd my_new_course \
 && git submodule init \
 && git submodule update