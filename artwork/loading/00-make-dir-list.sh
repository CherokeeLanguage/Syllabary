#!/bin/sh

set -e

cd "$(dirname "$0")" || exit 1

cp /dev/null 00-dir.list
ls -1 *.png > 00-dir.list

exit 0
