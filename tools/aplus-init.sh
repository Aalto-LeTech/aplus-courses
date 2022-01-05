#!/bin/sh -eu

# This script was copied from the run-aplus-front container.
# It has been modified here so that the database of the local testing container
# may be modified slightly to include some course-specific settings.
# This script must be mounted in the docker-compose.yml file in order to
# do anything.
#
# Example lines from docker-compose.yml
# plus:
#   volumes:
#     - ./tools/aplus-init.sh:/srv/aplus-init.sh:ro
#     - ./tools/test-bench-course-mod.py:/srv/test-bench-course-mod.py:ro


# Start background services/tasks
#start_services
# start course updater (will exit when successful)
run_services aplus-course-update aplus-lti-services

# Custom modifications to the Aplus database
setuidgid aplus python3 /srv/simple_course.py
