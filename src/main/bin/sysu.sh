#!/usr/bin/env bash
nohup sh run.sh community -uid 1892723783 -nocommtag -notag -nocontent > sysu.log &
tailf sysu.log