#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"


java -jar $DIR/sf-fx-runtime-java-main/target/sf-fx-runtime-java-main-1.0-SNAPSHOT-jar-with-dependencies.jar "${1}"
