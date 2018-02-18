#!/bin/sh

DATE=`date +%s`

if [ ! -e QCodefag.github.io ]; then
	git clone https://github.com/QCodefag/QCodefag.github.io.git
fi

# Organized q posts
cd ../QCodefag.github.io
git pull
