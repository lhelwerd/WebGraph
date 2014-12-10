How to compile the WebGraph framework

- Download and extract maven binaries from http://maven.apache.org/download.cgi
- Download the dependencies tarball from http://webgraph.di.unimi.it/ and 
  extract it at a reasonable place
- Compile the JAR file using maven: mvn install
- Copy the target/webgraph-3.4.2.jar file it to the same location as the
  dependencies JAR files.

In order to use maven on an installation where it is not yet in the PATH, one can run mvn.sh to set up a link to the maven root directory, although it will only keep the path for the current session. Instructions on adding the paths to a .bashrc file, for example, are given by the script.

How to run the WebGraph framework

The framework has different modes of operations that allow compressing,
decompressing and testing graph files in different formats.

Recompress with other parameters:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -o -m 1 ../sets/uk-2007-05@100000 ../sets/uk-2007-05-fast
This command creates an offsets file and alters the maximum reference chain
length of the given dataset.

The following parameters can be used to alter the implemented compression
algorithms:
-m is reference length
-w is window size: 0 skips reference compression (copy list/blocks/flags)
-i is interval minimum length: 0 skips interval compression
-r is residual compression: 0 skips gaps compression, 1 performs gaps compression (default)
-b is copy block compression: 0 is copy list, 1 is copy blocks (default), 2 is copy flags. Only used if reference length and window size are not 0.

As we can see, we can set parameters to 0 to make sure that they are not used. Thus we can test the algorithms separately.

This, we have the following parameters for specific compression formats:

None: -m 0 -w 0 -i 0 -r 0
Gaps compression: -m 0 -w 0 -i 0
Interval: -m 0 -w 0 -r 0
Copy blocks: -i 0 -r 0 -b 1
Copy list: -i 0 -r 0 -b 0
Copy flags: -i 0 -r 0 -b 2

Example:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -m 0 -w 0 -i 0 ../sets/uk-2002 ../sets/uk-gaps

Create successors list: This makes a naive adjacency list format.
java -cp "*" it.unimi.dsi.webgraph.ASCIIGraph -g BVGraph ../sets/uk-2007-05@100000 ../sets/raw

Create edges list: This creates a naive flat format listing pairs of edges.
java -cp "*" it.unimi.dsi.webgraph.ArcListASCIIGraph -g BVGraph ../sets/uk-2007-05@100000 ../sets/edges.list

Speed test:
By default, the speed test performs sequential testing:
java -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph ../sets/uk-list

Random access testing: -r, requires a sample size parameter
java -cp "*" it.unimi.dsi.webgraph.test.SpeedTest -g BVGraph -r 100000 ../sets/uk-list

Make sure there's also an .offsets file for the random access testing.
