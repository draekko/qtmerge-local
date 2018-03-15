#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]; then
	echo "usage: $0 <mirror directory> <output directory>"
	exit
fi

DEST=$(realpath $2)

cd "$1/cache"

TS=$(date -u +%Y-%m-%d_%H.%M.%S)

echo ">> qtmerge-text-$TS.tar.bz2"
tar cjvf "$DEST/qtmerge-text-$TS.tar.bz2" refcache.json eventcache.json
echo ">> qtmerge-text-$TS-min.tar.bz2"
tar cjvf "$DEST/qtmerge-text-$TS-min.tar.bz2" refcache-min.json eventcache-min.json

