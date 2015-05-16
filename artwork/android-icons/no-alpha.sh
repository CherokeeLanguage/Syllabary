#!/bin/bash

cd "$(dirname "$0")"

gm convert -background white -flatten icon.png icon-no-alpha.png

read a
