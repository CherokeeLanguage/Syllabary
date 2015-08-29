#!/bin/bash

SIZE=2048
diskutil erasevolume HFS+ 'RoboVM RAM Disk' $(hdiutil attach -nomount ram://$((SIZE * 2048)))


