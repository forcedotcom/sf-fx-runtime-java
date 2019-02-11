#!/usr/bin/env bash
#
# Derived from the following script
# Author: Stefan Buck
# License: MIT
# https://gist.github.com/stefanbuck/ce788fee19ab6eb0b4447a85fc99f447
#
# Release a new version of sf-fx-sdk-java and sf-fx-runtime
#
# This script accepts the following parameters:
#
# * version - The version such as 0.1.0
# * github_api_token - Your github API token
#
# Example
#
# $ make-release.sh version=0.1.0 github_api_token=TOKEN

set -e
xargs=$(which gxargs || which xargs)

# Validate settings.
[ "$TRACE" ] && set -x

CONFIG=$@

for line in $CONFIG; do
  eval "$line"
done

# Obtain the directory that the script was run from
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

# Constants
GH_API="https://api.github.com"
GH_UPLOAD="https://uploads.github.com"
REPO_OWNER=cwallsfdc
REPO_NAME=sf-fx-runtime

SF_FX_RUNTIME_JAR=${SCRIPT_DIR}/../target/com.salesforce.function.runtime.jar
SF_FX_JRE=${SCRIPT_DIR}/../assets/sf-function-jre.tar.gz
TAG=com.salesforce.function.runtime-${version}

# Define variables.
GH_REPO="$GH_API/repos/${REPO_OWNER}/${REPO_NAME}"
GH_RELEASES="$GH_REPO/releases"
GH_TAGS="$GH_REPO/releases/tags/${TAG}"
AUTH="Authorization: token $github_api_token"
WGET_ARGS="--content-disposition --auth-no-challenge --no-cookie"
CURL_ARGS="-LJO#"

# Upload a file to the repo.
# Example
#
# upload_asset MyFile
function upload_asset {
    FILENAME=$1

    # Read asset tags.
    response=$(curl -sH "$AUTH" $GH_TAGS)

    echo ${response}

    # Get ID of the asset based on given filename.
    eval $(echo "$response" | grep -m 1 "id.:" | grep -w id | tr : = | tr -cd '[[:alnum:]]=')
    [ "$id" ] || { echo "Error: Failed to get release id for tag: $tag"; echo "$response" | awk 'length($0)<100' >&2; exit 1; }

    # Upload asset
    echo "Uploading ${FILENAME} to release id:$id"

    GH_ASSET="${GH_UPLOAD}/repos/${REPO_OWNER}/${REPO_NAME}/releases/$id/assets?name=$(basename ${FILENAME})"

    curl --data-binary @"$FILENAME" -H "Authorization: token $github_api_token" -H "Content-Type: application/octet-stream" ${GH_ASSET}
}

# Build the sf-fx-sdk-java jar
(cd ${SCRIPT_DIR}/../../sf-fx-sdk-java ; mvn clean deploy scm:tag -Drevision=${version});

# Build the sf-fx-runtime jar
(cd ${SCRIPT_DIR}/.. ; mvn clean deploy scm:tag -Drevision=${version} ; git push --tags);

curl -X POST -sH "$AUTH" \
--data "{ \"tag_name\": \"${TAG}\", \"target_commitish\": \"master\", \"name\": \"${TAG}\", \"body\": \"Automated Release\", \"draft\": false, \"prerelease\": true }" \
$GH_RELEASES

upload_asset ${SF_FX_RUNTIME_JAR};
upload_asset ${SF_FX_JRE};


