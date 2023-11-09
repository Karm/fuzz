#!/bin/sh
rm -rf resources/list.txt
for f in $(find resources/ -type f  \! -name "*.txt" -printf '%P\n');do
  echo "$f|$(identify -ping -format '%[width]|%[height]\n' -quiet resources/$f | head -n1)">>resources/list.txt;
done
