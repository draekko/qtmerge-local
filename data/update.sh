#!/bin/sh

DATE=`date +%s`

if [ ! -e Trump ]; then
	mkdir -p Trump/2017
	mkdir -p Trump/2018
	mkdir -p Q

	git clone https://github.com/QCodefag/QCodefag.github.io.git
fi

# Trump twitter archive
cd Trump
if [ ! -e 2017/2017* ]; then
	wget -c -v http://trumptwitterarchive.com/data/realdonaldtrump/2017.json -O 2017/2017-${DATE}.json
fi
wget -c -v http://trumptwitterarchive.com/data/realdonaldtrump/2018.json -O 2018/2018-${DATE}.json

# Raw q posts
cd ../Q
wget -c -v https://pastebin.com/raw/3YwyKxJE -O posts-${DATE}.txt

# Organized q posts
cd ../QCodefag.github.io
git pull
