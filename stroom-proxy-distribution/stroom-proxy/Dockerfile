#**********************************************************************
# Copyright 2016 Crown Copyright
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#**********************************************************************

FROM openjdk:8u151-jre-alpine

# The mode to run stroom proxy in
ENV STROOM_PROXY_MODE="store"

# IN_DOCKER tells setup.sh to run Configure without asking for user input, i.e. using defaults.
ENV IN_DOCKER="true"

# Copy the application.
ADD . /usr/stroom-proxy

WORKDIR /usr/stroom-proxy

# update alpine and install Bash as it is not in alpine by default
# then run setup to configure the tomcat instance
RUN echo "http_proxy: $http_proxy" && \
    echo "https_proxy: $https_proxy" && \
    apk update && \
    apk upgrade && \
    apk add --no-cache bash && \
    rm -rf /var/cache/apk/* && \
    ./bin/setup.sh $STROOM_PROXY_MODE

# export 8080 for stroom proxy to listen on
EXPOSE 8080

# start Stroom and tail the log file
CMD ./bin/start.sh && mkdir -p ./instance/logs && touch ./instance/logs/stroom.log && tail -f ./instance/logs/stroom.log
