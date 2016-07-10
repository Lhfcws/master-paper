#!/usr/bin/env bash
#nohup sh run.sh community -uid 1892723783 -noscanuser -nocontent -nodetect > sysu.log &
nohup sh run.sh community -uid 1994626454 -noscanuser -nocontent -notag -nocommtag > sysu.log &
tailf sysu.log