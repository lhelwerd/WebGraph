#!/bin/bash
stdbuf -o L `which time` -f "real\t%E\nuser\t%U\nsys\t%S\nmem\t%M/4 kB" "$@" 2>&1
echo "We are interested in user+system time (both seconds) and peak memory use."
