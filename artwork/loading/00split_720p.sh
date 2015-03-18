#!/bin/sh

width=1280
height=720

cutw=320
cuth=360

for f in loading.png; do
	i=0
	for y in $(seq 0 360 $((${height}-1))); do
		for x in $(seq 0 ${cutw} $((${width}-1))); do
			g="$(echo "$f"|sed 's/.png$//')"		
			dest="p_${g}_${i}.png"
			echo gm convert -crop ${cutw}x${cuth}+${x}+${y} "$f" "${dest}"
			gm convert -crop ${cutw}x${cuth}+${x}+${y} "$f" tmp."${dest}"
			pngcrush -brute tmp."${dest}" "${dest}" | grep filesize
			rm tmp."${dest}"
			i=$((${i}+1))
		done
	done
done
