#!/bin/sh

ulimit -s unlimited && \

# No compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 -r 0 ../datasets/cnr-2000 ../experiments/cnr-2000-none 2>&1 | tee ../experiments/cnr-2000-none.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-none 2>&1 | tee ../experiments/cnr-2000-none.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-none 2>&1 | tee ../experiments/cnr-2000-none.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 -r 0 ../datasets/in-2004 ../experiments/in-2004-none 2>&1 | tee ../experiments/in-2004-none.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-none 2>&1 | tee ../experiments/in-2004-none.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-none 2>&1 | tee ../experiments/in-2004-none.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 -r 0 ../datasets/uk-2002 ../experiments/uk-2002-none 2>&1 | tee ../experiments/uk-2002-none.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-none 2>&1 | tee ../experiments/uk-2002-none.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-none 2>&1 | tee ../experiments/uk-2002-none.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 -r 0 ../datasets/twitter-2010 ../experiments/twitter-2010-none 2>&1 | tee ../experiments/twitter-2010-none.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-none 2>&1 | tee ../experiments/twitter-2010-none.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-none 2>&1 | tee ../experiments/twitter-2010-none.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 -r 0 ../datasets/uk-2007-02 ../experiments/uk-2007-02-none 2>&1 | tee ../experiments/uk-2007-02-none.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-none 2>&1 | tee ../experiments/uk-2007-02-none.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-none 2>&1 | tee ../experiments/uk-2007-02-none.randspeedtest && \

# Gaps compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../datasets/cnr-2000 ../experiments/cnr-2000-gaps 2>&1 | tee ../experiments/cnr-2000-gaps.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-gaps 2>&1 | tee ../experiments/cnr-2000-gaps.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-gaps 2>&1 | tee ../experiments/cnr-2000-gaps.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../datasets/in-2004 ../experiments/in-2004-gaps 2>&1 | tee ../experiments/in-2004-gaps.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-gaps 2>&1 | tee ../experiments/in-2004-gaps.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-gaps 2>&1 | tee ../experiments/in-2004-gaps.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../datasets/uk-2002 ../experiments/uk-2002-gaps 2>&1 | tee ../experiments/uk-2002-gaps.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-gaps 2>&1 | tee ../experiments/uk-2002-gaps.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-gaps 2>&1 | tee ../experiments/uk-2002-gaps.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../datasets/twitter-2010 ../experiments/twitter-2010-gaps 2>&1 | tee ../experiments/twitter-2010-gaps.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-gaps 2>&1 | tee ../experiments/twitter-2010-gaps.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-gaps 2>&1 | tee ../experiments/twitter-2010-gaps.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../datasets/uk-2007-02 ../experiments/uk-2007-02-gaps 2>&1 | tee ../experiments/uk-2007-02-gaps.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-gaps 2>&1 | tee ../experiments/uk-2007-02-gaps.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-gaps 2>&1 | tee ../experiments/uk-2007-02-gaps.randspeedtest && \

# Interval compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 2 ../datasets/cnr-2000 ../experiments/cnr-2000-interval-i2 2>&1 | tee ../experiments/cnr-2000-interval-i2.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 3 ../datasets/cnr-2000 ../experiments/cnr-2000-interval-i3 2>&1 | tee ../experiments/cnr-2000-interval-i3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 4 ../datasets/cnr-2000 ../experiments/cnr-2000-interval-i4 2>&1 | tee ../experiments/cnr-2000-interval-i4.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-interval-i2 2>&1 | tee ../experiments/cnr-2000-interval-i2.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-interval-i3 2>&1 | tee ../experiments/cnr-2000-interval-i3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-interval-i4 2>&1 | tee ../experiments/cnr-2000-interval-i4.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-interval-i2 2>&1 | tee ../experiments/cnr-2000-interval-i2.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-interval-i3 2>&1 | tee ../experiments/cnr-2000-interval-i3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-interval-i4 2>&1 | tee ../experiments/cnr-2000-interval-i4.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 2 ../datasets/in-2004 ../experiments/in-2004-interval-i2 2>&1 | tee ../experiments/in-2004-interval-i2.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 3 ../datasets/in-2004 ../experiments/in-2004-interval-i3 2>&1 | tee ../experiments/in-2004-interval-i3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 4 ../datasets/in-2004 ../experiments/in-2004-interval-i4 2>&1 | tee ../experiments/in-2004-interval-i4.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-interval-i2 2>&1 | tee ../experiments/in-2004-interval-i2.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-interval-i3 2>&1 | tee ../experiments/in-2004-interval-i3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-interval-i4 2>&1 | tee ../experiments/in-2004-interval-i4.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-interval-i2 2>&1 | tee ../experiments/in-2004-interval-i2.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-interval-i3 2>&1 | tee ../experiments/in-2004-interval-i3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-interval-i4 2>&1 | tee ../experiments/in-2004-interval-i4.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 2 ../datasets/twitter-2010 ../experiments/twitter-2010-interval-i2 2>&1 | tee ../experiments/twitter-2010-interval-i2.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 3 ../datasets/twitter-2010 ../experiments/twitter-2010-interval-i3 2>&1 | tee ../experiments/twitter-2010-interval-i3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 4 ../datasets/twitter-2010 ../experiments/twitter-2010-interval-i4 2>&1 | tee ../experiments/twitter-2010-interval-i4.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-interval-i2 2>&1 | tee ../experiments/twitter-2010-interval-i2.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-interval-i3 2>&1 | tee ../experiments/twitter-2010-interval-i3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-interval-i4 2>&1 | tee ../experiments/twitter-2010-interval-i4.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-interval-i2 2>&1 | tee ../experiments/twitter-2010-interval-i2.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-interval-i3 2>&1 | tee ../experiments/twitter-2010-interval-i3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-interval-i4 2>&1 | tee ../experiments/twitter-2010-interval-i4.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 2 ../datasets/uk-2002 ../experiments/uk-2002-interval-i2 2>&1 | tee ../experiments/uk-2002-interval-i2.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 3 ../datasets/uk-2002 ../experiments/uk-2002-interval-i3 2>&1 | tee ../experiments/uk-2002-interval-i3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 4 ../datasets/uk-2002 ../experiments/uk-2002-interval-i4 2>&1 | tee ../experiments/uk-2002-interval-i4.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-interval-i2 2>&1 | tee ../experiments/uk-2002-interval-i2.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-interval-i3 2>&1 | tee ../experiments/uk-2002-interval-i3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-interval-i4 2>&1 | tee ../experiments/uk-2002-interval-i4.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-interval-i2 2>&1 | tee ../experiments/uk-2002-interval-i2.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-interval-i3 2>&1 | tee ../experiments/uk-2002-interval-i3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-interval-i4 2>&1 | tee ../experiments/uk-2002-interval-i4.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 2 ../datasets/uk-2007-02 ../experiments/uk-2007-02-interval-i2 2>&1 | tee ../experiments/uk-2007-02-interval-i2.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 3 ../datasets/uk-2007-02 ../experiments/uk-2007-02-interval-i3 2>&1 | tee ../experiments/uk-2007-02-interval-i3.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -r 0 -i 4 ../datasets/uk-2007-02 ../experiments/uk-2007-02-interval-i4 2>&1 | tee ../experiments/uk-2007-02-interval-i4.stats && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-interval-i2 2>&1 | tee ../experiments/uk-2007-02-interval-i2.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-interval-i3 2>&1 | tee ../experiments/uk-2007-02-interval-i3.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-interval-i4 2>&1 | tee ../experiments/uk-2007-02-interval-i4.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-interval-i2 2>&1 | tee ../experiments/uk-2007-02-interval-i2.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-interval-i3 2>&1 | tee ../experiments/uk-2007-02-interval-i3.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-interval-i4 2>&1 | tee ../experiments/uk-2007-02-interval-i4.randspeedtest && \

# Copy list compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 1 -m 1 ../datasets/cnr-2000 ../experiments/cnr-2000-copylist-w1m1 2>&1 | tee ../experiments/cnr-2000-copylist-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 3 -m 3 ../datasets/cnr-2000 ../experiments/cnr-2000-copylist-w3m3 2>&1 | tee ../experiments/cnr-2000-copylist-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 7 -m -1 ../datasets/cnr-2000 ../experiments/cnr-2000-copylist-w7m-1 2>&1 | tee ../experiments/cnr-2000-copylist-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copylist-w1m1 2>&1 | tee ../experiments/cnr-2000-copylist-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copylist-w3m3 2>&1 | tee ../experiments/cnr-2000-copylist-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copylist-w7m-1 2>&1 | tee ../experiments/cnr-2000-copylist-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copylist-w1m1 2>&1 | tee ../experiments/cnr-2000-copylist-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copylist-w3m3 2>&1 | tee ../experiments/cnr-2000-copylist-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copylist-w7m-1 2>&1 | tee ../experiments/cnr-2000-copylist-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 1 -m 1 ../datasets/in-2004 ../experiments/in-2004-copylist-w1m1 2>&1 | tee ../experiments/in-2004-copylist-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 3 -m 3 ../datasets/in-2004 ../experiments/in-2004-copylist-w3m3 2>&1 | tee ../experiments/in-2004-copylist-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 7 -m -1 ../datasets/in-2004 ../experiments/in-2004-copylist-w7m-1 2>&1 | tee ../experiments/in-2004-copylist-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copylist-w1m1 2>&1 | tee ../experiments/in-2004-copylist-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copylist-w3m3 2>&1 | tee ../experiments/in-2004-copylist-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copylist-w7m-1 2>&1 | tee ../experiments/in-2004-copylist-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copylist-w1m1 2>&1 | tee ../experiments/in-2004-copylist-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copylist-w3m3 2>&1 | tee ../experiments/in-2004-copylist-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copylist-w7m-1 2>&1 | tee ../experiments/in-2004-copylist-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 1 -m 1 ../datasets/twitter-2010 ../experiments/twitter-2010-copylist-w1m1 2>&1 | tee ../experiments/twitter-2010-copylist-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 3 -m 3 ../datasets/twitter-2010 ../experiments/twitter-2010-copylist-w3m3 2>&1 | tee ../experiments/twitter-2010-copylist-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 7 -m -1 ../datasets/twitter-2010 ../experiments/twitter-2010-copylist-w7m-1 2>&1 | tee ../experiments/twitter-2010-copylist-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copylist-w1m1 2>&1 | tee ../experiments/twitter-2010-copylist-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copylist-w3m3 2>&1 | tee ../experiments/twitter-2010-copylist-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copylist-w7m-1 2>&1 | tee ../experiments/twitter-2010-copylist-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copylist-w1m1 2>&1 | tee ../experiments/twitter-2010-copylist-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copylist-w3m3 2>&1 | tee ../experiments/twitter-2010-copylist-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copylist-w7m-1 2>&1 | tee ../experiments/twitter-2010-copylist-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 1 -m 1 ../datasets/uk-2002 ../experiments/uk-2002-copylist-w1m1 2>&1 | tee ../experiments/uk-2002-copylist-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 3 -m 3 ../datasets/uk-2002 ../experiments/uk-2002-copylist-w3m3 2>&1 | tee ../experiments/uk-2002-copylist-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 7 -m -1 ../datasets/uk-2002 ../experiments/uk-2002-copylist-w7m-1 2>&1 | tee ../experiments/uk-2002-copylist-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copylist-w1m1 2>&1 | tee ../experiments/uk-2002-copylist-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copylist-w3m3 2>&1 | tee ../experiments/uk-2002-copylist-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copylist-w7m-1 2>&1 | tee ../experiments/uk-2002-copylist-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copylist-w1m1 2>&1 | tee ../experiments/uk-2002-copylist-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copylist-w3m3 2>&1 | tee ../experiments/uk-2002-copylist-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copylist-w7m-1 2>&1 | tee ../experiments/uk-2002-copylist-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 1 -m 1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copylist-w1m1 2>&1 | tee ../experiments/uk-2007-02-copylist-w1m1.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 3 -m 3 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copylist-w3m3 2>&1 | tee ../experiments/uk-2007-02-copylist-w3m3.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 0 -w 7 -m -1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copylist-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copylist-w7m-1.stats && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copylist-w1m1 2>&1 | tee ../experiments/uk-2007-02-copylist-w1m1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copylist-w3m3 2>&1 | tee ../experiments/uk-2007-02-copylist-w3m3.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copylist-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copylist-w7m-1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copylist-w1m1 2>&1 | tee ../experiments/uk-2007-02-copylist-w1m1.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copylist-w3m3 2>&1 | tee ../experiments/uk-2007-02-copylist-w3m3.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copylist-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copylist-w7m-1.randspeedtest && \

# Copy blocks compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 1 -m 1 ../datasets/cnr-2000 ../experiments/cnr-2000-copyblocks-w1m1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 3 -m 3 ../datasets/cnr-2000 ../experiments/cnr-2000-copyblocks-w3m3 2>&1 | tee ../experiments/cnr-2000-copyblocks-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 7 -m -1 ../datasets/cnr-2000 ../experiments/cnr-2000-copyblocks-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyblocks-w1m1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyblocks-w3m3 2>&1 | tee ../experiments/cnr-2000-copyblocks-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyblocks-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyblocks-w1m1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyblocks-w3m3 2>&1 | tee ../experiments/cnr-2000-copyblocks-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyblocks-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyblocks-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 1 -m 1 ../datasets/in-2004 ../experiments/in-2004-copyblocks-w1m1 2>&1 | tee ../experiments/in-2004-copyblocks-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 3 -m 3 ../datasets/in-2004 ../experiments/in-2004-copyblocks-w3m3 2>&1 | tee ../experiments/in-2004-copyblocks-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 7 -m -1 ../datasets/in-2004 ../experiments/in-2004-copyblocks-w7m-1 2>&1 | tee ../experiments/in-2004-copyblocks-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyblocks-w1m1 2>&1 | tee ../experiments/in-2004-copyblocks-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyblocks-w3m3 2>&1 | tee ../experiments/in-2004-copyblocks-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyblocks-w7m-1 2>&1 | tee ../experiments/in-2004-copyblocks-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyblocks-w1m1 2>&1 | tee ../experiments/in-2004-copyblocks-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyblocks-w3m3 2>&1 | tee ../experiments/in-2004-copyblocks-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyblocks-w7m-1 2>&1 | tee ../experiments/in-2004-copyblocks-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 1 -m 1 ../datasets/twitter-2010 ../experiments/twitter-2010-copyblocks-w1m1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 3 -m 3 ../datasets/twitter-2010 ../experiments/twitter-2010-copyblocks-w3m3 2>&1 | tee ../experiments/twitter-2010-copyblocks-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 7 -m -1 ../datasets/twitter-2010 ../experiments/twitter-2010-copyblocks-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyblocks-w1m1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyblocks-w3m3 2>&1 | tee ../experiments/twitter-2010-copyblocks-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyblocks-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyblocks-w1m1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyblocks-w3m3 2>&1 | tee ../experiments/twitter-2010-copyblocks-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyblocks-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyblocks-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 1 -m 1 ../datasets/uk-2002 ../experiments/uk-2002-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2002-copyblocks-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 3 -m 3 ../datasets/uk-2002 ../experiments/uk-2002-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2002-copyblocks-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 7 -m -1 ../datasets/uk-2002 ../experiments/uk-2002-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2002-copyblocks-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2002-copyblocks-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2002-copyblocks-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2002-copyblocks-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2002-copyblocks-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2002-copyblocks-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2002-copyblocks-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 1 -m 1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w1m1.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 3 -m 3 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w3m3.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 1 -w 7 -m -1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w7m-1.stats && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w1m1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w3m3.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w7m-1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyblocks-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w1m1.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyblocks-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w3m3.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyblocks-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyblocks-w7m-1.randspeedtest && \

# Copy flags compression
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 1 -m 1 ../datasets/cnr-2000 ../experiments/cnr-2000-copyflags-w1m1 2>&1 | tee ../experiments/cnr-2000-copyflags-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 3 -m 3 ../datasets/cnr-2000 ../experiments/cnr-2000-copyflags-w3m3 2>&1 | tee ../experiments/cnr-2000-copyflags-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 7 -m -1 ../datasets/cnr-2000 ../experiments/cnr-2000-copyflags-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyflags-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyflags-w1m1 2>&1 | tee ../experiments/cnr-2000-copyflags-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyflags-w3m3 2>&1 | tee ../experiments/cnr-2000-copyflags-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/cnr-2000-copyflags-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyflags-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyflags-w1m1 2>&1 | tee ../experiments/cnr-2000-copyflags-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyflags-w3m3 2>&1 | tee ../experiments/cnr-2000-copyflags-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/cnr-2000-copyflags-w7m-1 2>&1 | tee ../experiments/cnr-2000-copyflags-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 1 -m 1 ../datasets/in-2004 ../experiments/in-2004-copyflags-w1m1 2>&1 | tee ../experiments/in-2004-copyflags-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 3 -m 3 ../datasets/in-2004 ../experiments/in-2004-copyflags-w3m3 2>&1 | tee ../experiments/in-2004-copyflags-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 7 -m -1 ../datasets/in-2004 ../experiments/in-2004-copyflags-w7m-1 2>&1 | tee ../experiments/in-2004-copyflags-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyflags-w1m1 2>&1 | tee ../experiments/in-2004-copyflags-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyflags-w3m3 2>&1 | tee ../experiments/in-2004-copyflags-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/in-2004-copyflags-w7m-1 2>&1 | tee ../experiments/in-2004-copyflags-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyflags-w1m1 2>&1 | tee ../experiments/in-2004-copyflags-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyflags-w3m3 2>&1 | tee ../experiments/in-2004-copyflags-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/in-2004-copyflags-w7m-1 2>&1 | tee ../experiments/in-2004-copyflags-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 1 -m 1 ../datasets/twitter-2010 ../experiments/twitter-2010-copyflags-w1m1 2>&1 | tee ../experiments/twitter-2010-copyflags-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 3 -m 3 ../datasets/twitter-2010 ../experiments/twitter-2010-copyflags-w3m3 2>&1 | tee ../experiments/twitter-2010-copyflags-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 7 -m -1 ../datasets/twitter-2010 ../experiments/twitter-2010-copyflags-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyflags-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyflags-w1m1 2>&1 | tee ../experiments/twitter-2010-copyflags-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyflags-w3m3 2>&1 | tee ../experiments/twitter-2010-copyflags-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/twitter-2010-copyflags-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyflags-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyflags-w1m1 2>&1 | tee ../experiments/twitter-2010-copyflags-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyflags-w3m3 2>&1 | tee ../experiments/twitter-2010-copyflags-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/twitter-2010-copyflags-w7m-1 2>&1 | tee ../experiments/twitter-2010-copyflags-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 1 -m 1 ../datasets/uk-2002 ../experiments/uk-2002-copyflags-w1m1 2>&1 | tee ../experiments/uk-2002-copyflags-w1m1.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 3 -m 3 ../datasets/uk-2002 ../experiments/uk-2002-copyflags-w3m3 2>&1 | tee ../experiments/uk-2002-copyflags-w3m3.stats && \
./memtime.sh java -Xss64M -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 7 -m -1 ../datasets/uk-2002 ../experiments/uk-2002-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2002-copyflags-w7m-1.stats && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyflags-w1m1 2>&1 | tee ../experiments/uk-2002-copyflags-w1m1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyflags-w3m3 2>&1 | tee ../experiments/uk-2002-copyflags-w3m3.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2002-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2002-copyflags-w7m-1.seqspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyflags-w1m1 2>&1 | tee ../experiments/uk-2002-copyflags-w1m1.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyflags-w3m3 2>&1 | tee ../experiments/uk-2002-copyflags-w3m3.randspeedtest && \
java -Xss64M -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2002-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2002-copyflags-w7m-1.randspeedtest && \

./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 1 -m 1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyflags-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w1m1.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 3 -m 3 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyflags-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyflags-w3m3.stats && \
./memtime.sh java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.BVGraph -i 0 -r 0 -b 2 -w 7 -m -1 ../datasets/uk-2007-02 ../experiments/uk-2007-02-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w7m-1.stats && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyflags-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w1m1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyflags-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyflags-w3m3.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../experiments/uk-2007-02-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w7m-1.seqspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyflags-w1m1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w1m1.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyflags-w3m3 2>&1 | tee ../experiments/uk-2007-02-copyflags-w3m3.randspeedtest && \
java -Xss64M -Xmx12G -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 10000 ../experiments/uk-2007-02-copyflags-w7m-1 2>&1 | tee ../experiments/uk-2007-02-copyflags-w7m-1.randspeedtest
