#!/usr/bin/env bash
nohup sh run.sh community -uid 1892723783 -noscanuser -nocontent -notag -norender > sysu.log &
#nohup sh run.sh community -uid 1994626454 -noscanuser -nocontent -notag -nocommtag > sysu.log &
tailf sysu.log