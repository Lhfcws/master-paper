#!/usr/bin/env bash
nohup sh run.sh community -uid 1757537265 -nodetect -norender -nocontent -noscanuser > test.log &
tailf test.log