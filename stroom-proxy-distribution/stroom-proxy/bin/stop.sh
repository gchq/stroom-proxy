#!/bin/bash

export BIN_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${BIN_DIR}/common.sh

stroom-echo "${INSTALL_DIR} - Stop"

stroom-init-check
stroom-get-lock

TOMCAT_PID=`jps -v | grep ${CATALINA_HOME} | cut -f1 -d' ' | tr '\n' ' '`
TOMCAT_INSTANCE_COUNT=`echo ${TOMCAT_PID} | wc -w`  

# Case 1 - Already Stopped
if [ -z "${TOMCAT_PID}" ]; then

	stroom-echo "Tomcat is already stopped"
	
	stroom-rm-lock
	exit 0
	
fi

# Case 2 - Multiple jvm's running - this should not happen but if it does we kill them all
if [ "${TOMCAT_INSTANCE_COUNT}" -gt 1 ]; then

	stroom-echo "Multiple matching Tomcats found will kill them all !! ${TOMCAT_PID}"
	kill -9 ${TOMCAT_PID}

	stroom-rm-lock
	exit 0
fi


stroom-echo "Stopping Tomcat pid ${TOMCAT_PID}"

# Kill any java options not needed for a stop
JAVA_OPTS=
${CATALINA_HOME}/bin/shutdown.sh &> /dev/null

TEST_RUNNING=`ps -p ${TOMCAT_PID} | grep ${TOMCAT_PID}`

RETRY=180
while [ -n "${TEST_RUNNING}" -a "${RETRY}" -gt 0 ]
do
	if [ "${RETRY}" -lt 2 ]; then
		stroom-echo "jstack trace"
		jstack ${TOMCAT_PID} &
		sleep 10
		stroom-echo "Tomcat failed to stop so kill -9 ${TOMCAT_PID}"
		kill -9 ${TOMCAT_PID}
	else 
		stroom-echo "Waiting for ${TOMCAT_PID} to stop. ${RETRY}"
		sleep 1
		TEST_RUNNING=`ps -p ${TOMCAT_PID} | grep ${TOMCAT_PID}`
	fi
	RETRY=$(($RETRY -1))
done

stroom-rm-lock
