#!/bin/sh
find resources/ -type f  \! -name "*.txt" -printf '%P\n' > resources/list.txt
