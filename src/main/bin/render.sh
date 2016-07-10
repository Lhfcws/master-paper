#!/usr/bin/env bash
nohup sh run.sh community -uid 1892723783 -noscanuser -nocontent -notag -nocommtag > render.log &
tailf render.log
