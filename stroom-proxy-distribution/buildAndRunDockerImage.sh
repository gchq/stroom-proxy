#!/bin/sh

echo "Clearing out target/stroom-proxy"
rm -rf ./target/stroom-proxy

echo "unzipping distribution"
distFile=target/stroom-proxy-distribution-*-bin.zip
if [ ! -f $distFile ]; then
    echo "No distribution zip file present"
    exit 1
fi

unzip $distFile -d target

#stop/remove any existing containers/images
docker stop stroom-proxy
docker rm stroom-proxy
docker rmi stroom-proxy

#Allow for running behind a proxy or not
if [ -z $HTTP_PROXY ]; then
    proxyArg1=""
else
    proxyArg1="--build-arg http_proxy=$HTTP_PROXY"
fi

if [ -z $HTTPS_PROXY ]; then
    proxyArg2=""
else
    proxyArg2="--build-arg https_proxy=$HTTPS_PROXY"
fi

echo "proxyArg1: $proxyArg1"
echo "proxyArg2: $proxyArg2"

docker build ${proxyArg1} ${proxyArg2} --tag=stroom-proxy:latest target/stroom-proxy

#This command assumes that the database jdbc url, username and password are all defined in properties in ~/.stroom/stroom.conf else
#add something like the following to the run comman
docker run -p 8080:8080 --name=stroom-proxy -eSTROOM_PROXY_TYPE="store"