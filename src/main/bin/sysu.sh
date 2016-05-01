#!/usr/bin/env bash
nohup sh run.sh community -uid 1892723783 -noscanuser -nocontent -nodetect > sysu.log &
tailf sysu.log