#!/bin/bash

set -e

cd "$(dirname "$0")" || exit 1

ord() {
  printf '%d' "'$1"
}

chr() {
  printf \\$(printf '%03o' $1)
}

DEST=textured
if [ -d "${DEST}" ]; then
	rm -rfv "${DEST}"
fi
mkdir "${DEST}"

#inkscape -z -e "test.png" -d 90 -D test.svg

iy=0
for ch in ? ✘ ✓ ✔ ✕ ✖ ✗ ✘ ♫ ⚀ ⚁ ⚂ ⚃ ⚄ ⚅ ☐ ☑ ☒ ☓ ☠ ☹ ☺ 0 +1 +2 +3 +4 +5 -1 -2 -3 -4 -5 - _ = + +0 -0; do
	glyph="$ch"
	ix=0
	for svg in "textured-svg-src"/*.svg; do
		file="$(printf "%02d_%01d" $iy $ix)"
		cat "${svg}" | sed "s/Ꮝ/${glyph}/g" > "${DEST}/${file}.svg"
		inkscape -z -e "${DEST}/${file}.png" -d 180 -D "${DEST}/${file}.svg"
		rm "${DEST}/${file}.svg"
		gm mogrify -trim -scale 256x256 "${DEST}/${file}.png"
		ix=$(($ix+1))
	done
	iy=$(($iy+1))
done
