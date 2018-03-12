#!/usr/bin/env /bin/bash

#OPTS="--dry-run"

echo "======================================================="

if [ -z "$1" ]; then
	echo "Specify path to repository checkout"
else

	cd "$1" && \
	git commit ${OPTS:-} -a -m "QT Update" && \
	git push ${OPTS:-}

fi

echo "======================================================="
