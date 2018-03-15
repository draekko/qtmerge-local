#!/usr/bin/env bash

cd anonsw.github.io/8chjs

uglifyjs --compress -m reserved=[\$,anonsw] -o q-min.js q-main.js

echo -e "$(cat q-header.js)\n$(cat q-main.js)" > q.js
echo -e "$(cat q-header.js)\n$(cat q-min.js)" > q-min.js
