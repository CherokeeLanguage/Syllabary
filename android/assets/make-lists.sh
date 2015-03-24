#!/bin/bash

cd "$(dirname "$0")"

cp /dev/null dir.txt

P="$(basename "$0")"
C="$(pwd)"

find . | sed 's:^./::' | while read x; do
	if [ x"$x" = x"" ]; then
		continue
	fi	
	if [ "$x" = "." ]; then
		continue
	fi	
	if [ "$x" = "dir.txt" ]; then
		continue
	fi
	if [ "$x" = "$P" ]; then
		continue
	fi
	if [ -d "${x}" ]; then
		echo "d:${x}" >> dir.txt
	else
		echo "f:${x}" >> dir.txt
	fi
done

exit 0
