#!/bin/sh

echo "Clearing out build/distributions/stroom-proxy"
rm -rf ./build/distributions/stroom-proxy

echo "unzipping distribution"
distFile=./build/distributions/stroom-proxy-distribution-*.zip
if [ ! -f $distFile ]; then
    echo "No distribution zip file present"
    exit 1
fi

unzip $distFile -d ./build/distributions

# Stop/remove any existing containers/images
docker stop stroom-proxy
docker rm stroom-proxy
docker rmi stroom-proxy

# Allow for running behind a proxy or not
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

docker build ${proxyArg1} ${proxyArg2} --tag=stroom-proxy:latest build/distributions/stroom-proxy

# Run the image in store mode
docker run -p 9080:9080 --name=stroom-proxy -e STROOM_PROXY_MODE="store" stroom-proxy
