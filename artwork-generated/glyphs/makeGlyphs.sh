#!/bin/bash

set -e

cd "$(dirname "$0")" || exit 1

chmod +x graphics_utf

DEST2=white92
if [ -d "${DEST2}" ]; then
	rm -rfv "${DEST2}"
fi
mkdir "${DEST2}"

F1=./Fonts/AboriginalSerifREGULAR943.ttf
F2=./Fonts/Digohweli_1_7.ttf
F3=./Fonts/CherokeeHandoneB.ttf
F4=./Fonts/Nikwasi.ttf
F5=./Fonts/Oconostota.ttf

for x in $(seq 5024 5108); do
#for x in $(seq 5024 5025); do
h=$(printf "%04x" "$x")
i=$(printf "%04x" "$(($x+1))")
./graphics_utf -n "$h"-"$i" | while read glyph; do
ix=0
for font in "$F1" "$F2" "$F3" "$F4" "$F5"; do
#if [ ! -d "${DEST}/${h}" ]; then mkdir "${DEST}/${h}"; fi
file="${DEST}/${h}_${ix}.png"
#convert -background none \
#	-depth 24 \
#	-fill black \
#	-stroke none \
#	-font "$font" \
#	-size 400x400 \
#	-trim \
#	label:"$glyph" \
#	"$file"
#mogrify -background none -scale 200x200 "$file"

file="${DEST2}/${h}_${ix}.png"
gm convert -background none \
	-depth 32 \
	-fill white \
	-stroke none \
	-font "$font" \
	-size 1024x1024 \
	-filter Sinc \
	-trim \
	label:"$glyph" \
	"$file"
gm mogrify -gravity center -background none -scale 128x128 "$file"
#gm mogrify -operator Opacity Threshold 1 "$file"
ix=$(($ix+1))
done
done

done
