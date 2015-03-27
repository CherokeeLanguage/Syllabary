#!/bin/bash

cd "$(dirname "$0")" || exit 1

DEST2=white92
if [ -d "${DEST2}" ]; then
	rm -rfv "${DEST2}"
fi
mkdir "${DEST2}"

F1=./AboriginalSerifREGULAR943.ttf
F2=./Digohweli_1_7.ttf
F3=./Fonts/CherokeeHandone.ttf
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
	-size 512x512 \
	-trim \
	label:"$glyph" \
	"$file"
gm mogrify -gravity center -background none -scale 92x92 "$file"
#mogrify -white-threshold 0% "$file"
ix=$(($ix+1))
done
done

done
