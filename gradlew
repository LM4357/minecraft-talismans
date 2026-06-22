#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#  Gradle startup script for UN*X
#
##############################################################################

# Set local scope for the variables with windows NT shell
if [ "${OS}" = "Windows_NT" ] ; then
    setlocal
fi

DIRNAME=$(dirname "$0")
if [ "${DIRNAME}" = "." ] || [ "${DIRNAME}" = ".." ] ; then
    DIRNAME=$(pwd)
fi

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "${DIRNAME}" && pwd)

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

# Find java.exe
if [ -n "${JAVA_HOME}" ] ; then
    if [ -x "${JAVA_HOME}/bin/java" ] ; then
        JAVA_EXE="${JAVA_HOME}/bin/java"
    else
        echo "ERROR: JAVA_HOME is set to an invalid directory: ${JAVA_HOME}" >&2
        echo "" >&2
        echo "Please set the JAVA_HOME variable in your environment to match the" >&2
        echo "location of your Java installation." >&2
        exit 1
    fi
else
    JAVA_EXE=$(which java)
    if [ -z "${JAVA_EXE}" ] ; then
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        echo "" >&2
        echo "Please set the JAVA_HOME variable in your environment to match the" >&2
        echo "location of your Java installation." >&2
        exit 1
    fi
fi

# Check if java version is at least 8
JAVA_VERSION=$(${JAVA_EXE} -version 2>&1 | awk -F '\"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "${JAVA_VERSION}" -lt 8 ] ; then
    echo "ERROR: Java version 8 or higher is required." >&2
    echo "Current Java version: $(${JAVA_EXE} -version 2>&1)" >&2
    exit 1
fi

# Increase the maximum file descriptors if we can
if [ "${OS}" != "Windows_NT" ] ; then
    ulimit -n 1024 2>/dev/null
fi

# Setup the command line

CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

# Execute Gradle
"${JAVA_EXE}" ${DEFAULT_JVM_OPTS} ${JAVA_OPTS} ${GRADLE_OPTS} "-Dorg.gradle.appname=${APP_BASE_NAME}" -classpath "${CLASSPATH}" org.gradle.wrapper.GradleWrapperMain "$@"

# Exit with the same code as the Gradle process
EXIT_CODE=$?
if [ "${EXIT_CODE}" = "0" ] ; then
    if [ "${OS}" = "Windows_NT" ] ; then
        endlocal
    fi
    exit 0
else
    if [ "${OS}" = "Windows_NT" ] ; then
        endlocal
    fi
    exit ${EXIT_CODE}"