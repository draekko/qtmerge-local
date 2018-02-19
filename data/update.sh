#!/bin/sh

DATE=`date +%s`

if [ ! -e QCodefag.github.io ]; then
	git clone https://github.com/QCodefag/QCodefag.github.io.git
fi

if [ ! -e qanonmap.github.io ]; then
    git clone https://github.com/qanonmap/qanonmap.github.io.git
fi

cd QCodefag.github.io
git pull

cd ../qanonmap.github.io
git pull
