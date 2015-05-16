#!/bin/bash

export FILTER=Lanczos
export SRC=promo-graphic.png
export D="derived"

set -e

cd "$(dirname "$0")"

mkdir "${D}" 2> /dev/null || true

function process() {
	SIZE="$1"
	shift
	DEST="${D}/$1"
	shift
	XARGS="$@"
	echo gm convert -filter ${FILTER} "${SRC}" -resize "${SIZE}" -gravity center -background none -extent "${SIZE}" ${XARGS} "${DEST}"
	gm convert -filter ${FILTER} "${SRC}" ${XARGS} -resize "${SIZE}" -gravity center -background none -extent "${SIZE}" "${DEST}"
	
}

function process_white() {
	SIZE="$1"
	shift
	DEST="${D}/$1"
	shift
	XARGS="$@"
	echo gm convert -filter ${FILTER} "${SRC}" -resize "${SIZE}" -gravity center -background white -extent "${SIZE}" ${XARGS} "${DEST}"
	gm convert -filter ${FILTER} "${SRC}" ${XARGS} -resize "${SIZE}" -gravity center -background white -extent "${SIZE}" "${DEST}"
	
}

process_white 1024x500 promo-image-flat.jpg
process_white 180x120 promo-graphic-flat.jpg

process 750x1334 Default-375w-667h@2x.png -rotate 90
process 1242x2208 Default-414w-736h@3x.png -rotate 90
process 640x1136 Default-568h@2x.png -rotate 90
process 320x480 Default.png -rotate 90
process 640x960 Default@2x.png -rotate 90
process 1536x2008 Default@2x~ipad.png -rotate 90
process 768x1004 Default~ipad.png -rotate 90

