#!/bin/sh

if [ $# -ne 2 ]; then
    echo "ERROR: invalid arguments"
    echo "usage: multiCurl.sh N http://localhost:9080/stroom/datafeed"
    echo "where N is the number of times you want some data to be curled into stroom-proxy"
    exit 1
fi
numTimes=$1
url=$2

for i in `seq 1 $numTimes`; do
    feedHeader="FEED_$(( $i % 2 ))"
    systemHeader="SYSTEM_$(( $i % 3 ))"
    environmentHeader="ENVIRONMENT_$(( $i % 4 ))"

    echo "id:$i-`date --rfc-3339=ns`" | curl --data-binary @- "http://localhost:9080/stroom/datafeed" -H "Feed:$feedHeader" -H "System:$systemHeader" -H "Environment:$environmentHeader"
done;
