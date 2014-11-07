How to run the WebGraph framework

- Download the dependencies tarball from http://webgraph.di.unimi.it/ and 
  extract it at a reasonable place
- Compile the JAR file using mvn (mvn install) and copy it to the dependency 
  location.

Recompress with other parameters:
java -cp "*" it.unimi.dsi.webgraph.BVGraph -o -m 1 ../sets/uk-2007-05@100000 ../sets/uk-2007-05-fast

-m is reference length
-w is window size : 0 skips reference compression (copy list or blocks)
-i is interval minimum length : 0 skips interval compression
We might be able to set to 0 or -1 to make sure they are not used: Algorithms tested separately?

Create successors list: (Naive Adjacency List)
(Basenames)
java -cp "*" it.unimi.dsi.webgraph.ASCIIGraph -g BVGraph ../sets/uk-2007-05@100000 ../sets/raw

Create edges list: (Naive flat format)
java -cp "*" it.unimi.dsi.webgraph.ArcListASCIIGraph -g BVGraph ../sets/uk-2007-05@100000 ../sets/edges.list

Specific compression formats:

Gaps compression: -m 0 -w 0 -i 0
Interval(+gaps): -m 0 -w 0
