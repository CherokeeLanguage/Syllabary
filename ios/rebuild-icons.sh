#!/bin/bash

set -e
set -o pipefail

trap "echo ERROR; read a" ERR

cd "$(dirname "$0")"

export PNG_ICON="$(pwd)/icon-4096.png"
export PNG_LOADING="$(pwd)/loading-1080p.png"

export FILTER=Lanczos

mkdir -p data/Media.xcassets/AppIcon.appiconset 2> /dev/null || true

#copy json files over
find './data.template/' -name '*.json' | sed 's|^./data.template/||g' | while read json; do
    cp -v "./data.template/$json" "./data/$json"
done

#square app icons
find './data.template/Media.xcassets' -name '*.png' | sed 's|^./data.template/||g' | while read png; do
    geometry="$(gm identify "./data.template/$png" | cut -f 3 -d ' ' | cut -f 1 -d '+')"
    echo "Creating ./data/$png"
    gm convert "$PNG_ICON" -background white -filter "$FILTER" -geometry "$geometry" -gravity center -extent "$geometry" "./data/$png"
done

#loading screens ('splash screens')
for png in data.template/*.png; do
    geometry="$(gm identify "$png" | cut -f 3 -d ' ' | cut -f 1 -d '+')"
    png="$(basename "$png")"
    echo "Creating $png"
    gm convert "$PNG_LOADING" -background white -rotate 90 -filter "$FILTER" -geometry "$geometry" -gravity center -extent "$geometry" "./data/$png"    
done

exit 0

