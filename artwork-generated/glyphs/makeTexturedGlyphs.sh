#!/bin/bash

set -e

cd "$(dirname "$0")" || exit 1

#chmod +x graphics_utf

DEST=textured
if [ -d "${DEST}" ]; then
	rm -rfv "${DEST}"
fi
mkdir "${DEST}"

#inkscape -z -e "test.png" -d 90 -D test.svg

for x in 63 0x25CF $(seq 5024 5108); do
	h=$(printf "%04x" "$x")
	i=$(printf "%04x" "$(($x+1))")
	glyph="$(echo -e '\u'${h})"
	ix=0
	for svg in "textured-svg-src"/*.svg; do
		cat "${svg}" | sed "s/Ꮝ/${glyph}/g" > "${DEST}/${h}_${ix}.svg"
		inkscape -z -e "${DEST}/${h}_${ix}.png" -d 90 -D "${DEST}/${h}_${ix}.svg"
		rm "${DEST}/${h}_${ix}.svg"
		gm mogrify -trim -scale 128x128 "${DEST}/${h}_${ix}.png"
		ix=$(($ix+1))
	done
done
