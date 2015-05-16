#!/bin/bash

cd "$(dirname "$0")"

gm convert -background white -flatten icon.png icon-no-alpha.png

for icon in Icon.png Icon@2x.png Icon-*.png; do
	cp "$icon" "tmp.png"
	gm convert -background white -flatten "tmp.png" "$icon"
done

read a
