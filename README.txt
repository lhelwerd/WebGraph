How to set up and use the WebGraph framework

In order to compile the WebGraph framework and set it up so that it can be run,
the following steps should be followed:

- Download and extract Apache Maven binaries from
  http://maven.apache.org/download.cgi
- Download the dependencies tarball from http://webgraph.di.unimi.it and
  extract it.
- Retrieve the WebGraph framework source code with extensions from this
  repository. This version contains the copy list and copy flags compression
  formats, as well as additional flags for the other compression schemes.
- Compile the JAR file of the framework using Maven by running "mvn install"
  within the WebGraph root directory.
- Copy the target/webgraph-3.4.2.jar file it to the same location as the JAR
  files from the dependencies.

In order to use Maven on a computer where it is not yet in the PATH, one
can run mvn.sh provided in the repository. This sets up a link to the Maven
root directory. Note that it only keeps the path setting for the current
session by default. Instructions on adding the paths to a .bashrc file, for
example, are given by the script. Otherwise, one should either run the script
in a bash with ". ./mvn.sh" and run mvn afterwards, or with "./mvn.sh install"
(i.e., in place of mvn).

The framework has different modes of operations that allow compressing,
decompressing and testing graph files in different formats. For example, the
following command recompresses a dataset with other parameters:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -o -m 1 ../sets/uk-2002 ../sets/uk-fast

This command creates an offsets file and alters the maximum reference chain
length of the given dataset.

The following parameters can be used to alter the implemented compression
algorithms:
- -m determines the maximum length of a reference chain that we can make
  during reference compression. -1 is essentially the same as allowing any
  length of reference chains, while 0 skips reference compression, which is
  used by copy list, copy blocks and copy flags compression.
- -w is the window size. 0 skips all reference compression, which is used by
  copy list, copy blocks and copy flags compression.
- -i is interval minimum length. 0 skips interval compression.
- -r is residual compression. This is a boolean parameter, where the value
  0 skips gaps compression, and 1 performs gaps compression, which is the
  default.
- -b determines which copy compression is used. 0 is copy list, 1 is copy
  blocks (default) and 2 is copy flags. This is only used if the maximum
  reference length (-m) and window size (-w) are not 0.

As we can see, we can set certain parameters to 0 to make sure that their
respective compression schemes are not used. We can therefore test the algorithms separately. To be specific, we have the following parameters for the
given compression formats:

- No compression algorithm: -m 0 -w 0 -i 0 -r 0
- Gaps compression: -m 0 -w 0 -i 0
- Interval compression: -m 0 -w 0 -r 0
- Copy blocks: -i 0 -r 0 -b 1
- Copy list: -i 0 -r 0 -b 0
- Copy flags: -i 0 -r 0 -b 2

For instance, this command creates a graph file with only gaps compression:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../sets/uk-2002 ../sets/uk-gaps

The compressed files are binary files, with special representations for numeric
values that improve compression compared to plain text formats. The framework
can also decompress the graphs and store them in a readable format. There are
two major plain text formats that the framework supports. The first plain text
format is the naive adjacency list format:
java -cp "*" it.unimi.dsi.webgraph.ASCIIGraph -g BVGraph ../sets/uk-2002 ../sets/uk-raw

The second format is a naive listing of pairs of edges:
java -cp "*" it.unimi.dsi.webgraph.ArcListASCIIGraph -g BVGraph ../sets/uk-2002 ../sets/uk.edges

One can also import a graph from these flat files, by swapping around the
"ASCIIGraph" or "ArcListASCIIGraph" class with the "BVGraph" class. This
requires that those files are sorted correctly and do not contain non-edge
data such as comments. For example:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -g ArcListASCIIGraph ../sets/huge.txt.e ../sets/huge
Here, one can again add specific compression flags to tune which compression
and which settings to use. Note that this will almost always giv increased
compression since it is no longer stored as ASCII text but as binary codes,
skewing the comparison.

The WebGraph framework also provides a speed test module, which has been
adapted to use CPU time instead of wall-clock time. The speed test has two
different modes in which it can operate. By default, the speed test performs
sequential testing, which times how long it takes to expand the whole
compressed graph. The following command can be used to run a sequential speed
test:
java -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../sets/uk-gaps

We can also perform random access testing. This mode can be started by giving
the parameter -r. This parameter requires a sample size as value, which
determines how many randomly chosen nodes we use in the test in order to
measure and average the random access times. This command can be used to run
a random access speed test:
java -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 100000 ../sets/uk-gaps

Finally, if the WebGraph framework runs out of memory, one can increase the
Java heap size using the -Xmx command line flag and the Java thread stack size
using -Xss.
