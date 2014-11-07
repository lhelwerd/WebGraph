package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2003-2014 Paolo Boldi and Sebastiano Vigna 
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.lang.ObjectParser;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;


/** An {@link ImmutableGraph} that corresponds to graphs stored in a human-readable
 *  ASCII format where each line contains the list of successors of a given node. 
 *  
 *  <p>The file format is as follows: the graph is stored in a file named <samp><var>basename</var>.graph-txt</samp>.
 *  The first line contains the number of nodes, <var>n</var>. Then, <var>n</var> lines follow, the <var>i</var>-th
 *  line containing the successors of node <var>i</var> in increasing order
 *  (nodes are numbered from 0 to <var>n</var>&minus;1).
 *  Successors are separated by a single space.
 *
 *  <P>Contrarily to other classes, the load methods of this class <strong>do not always return instances of this class</strong>.
 *  In particular, {@link #loadOffline(CharSequence)} and {@link #loadOnce(InputStream)} <em>will</em> return an instance of this class for
 *  offline access. The instance will not provide random access, but sequential access will be backed by
 *  the original text file and only one array of successor will be loaded in core memory at any time.
 *  
 *  <p>The {@link #load(CharSequence)} method, on the other hand, will return an instance of 
 *  {@link it.unimi.dsi.webgraph.ArrayListMutableGraph} built by copying an offline instance of this class.
 *  
 *  <h2>Using {@link ASCIIGraph} to convert your data</h2>
 *  
 *  <p>A simple (albeit rather inefficient) way to import data into WebGraph is using ASCII graphs. Suppose you
 *  create the following file, named <samp>example.graph-txt</samp>:
 *  <pre>
 *  2
 *  1
 *  0 1
 *  </pre>
 *  Then, the command 
 *  <pre>
 *  java it.unimi.dsi.webgraph.BVGraph -g ASCIIGraph example bvexample
 *  </pre>
 *  will produce a compressed graph in {@link it.unimi.dsi.webgraph.BVGraph} format
 *  with basename <samp>bvexample</samp>. Even more convenient is the {@link #loadOnce(InputStream)}
 *  method, which reads from an input stream an ASCII graph and exposes it for a single traversal. It
 *  can be used, for instance, with the main method of {@link it.unimi.dsi.webgraph.BVGraph} to
 *  generate somehow an ASCII graph and store it in compressed form on the fly. The previous
 *  example could be then rewritten as
 *  <pre>
 *  java it.unimi.dsi.webgraph.BVGraph -1 -g ASCIIGraph dummy bvexample &lt;example.graph-txt
 *  </pre>
 */


public class ASCIIGraph extends ImmutableSequentialGraph {
	/** The standard extension of an ASCII graph. */
	private static final String ASCII_GRAPH_EXTENSION = ".graph-txt";

	private static final Logger LOGGER = LoggerFactory.getLogger( ASCIIGraph.class );
	
	/** Number of nodes. */
	private final int n;
	/** The file containing the graph, or <code>null</code> for a read-once ASCII graph. */
	private final CharSequence graphFile;
	/** A fast buffered reader containing the description of an ASCII graph (except for the number of nodes) for a read-once ASCII graph; <code>null</code>, otherwise. */
	private final FastBufferedReader fbr;
	
	protected ASCIIGraph( final CharSequence graphFile ) throws NumberFormatException, IOException {
		this.graphFile = graphFile;

		final BufferedReader bufferedReader = new BufferedReader( new FileReader( graphFile.toString() + ASCII_GRAPH_EXTENSION ) );
		n = Integer.parseInt( bufferedReader.readLine() );
		bufferedReader.close();
		fbr = null;
		if ( n < 0 ) throw new IllegalArgumentException( "Number of nodes must be nonnegative" );
	}

	/** Creates a read-once ASCII graph. Instances created using this constructor can be
	 * only accessed using a single call to {@link #nodeIterator(int)}.
	 * 
	 * @param is an input stream containing an ASCII graph.
	 */
	
	public ASCIIGraph( final InputStream is ) throws NumberFormatException, IOException {
		graphFile = null;
		fbr = new FastBufferedReader( new InputStreamReader( is, "ASCII" ) );
		n = Integer.parseInt( fbr.readLine( new MutableString() ).toString() );
		if ( n < 0 ) throw new IllegalArgumentException( "Number of nodes must be nonnegative" );
	}

	public int numNodes() {
		return n;
	}
	
	@Override
	public NodeIterator nodeIterator( final int from ) {
		if ( from < 0 || from > n ) throw new IllegalArgumentException();
		try {
			@SuppressWarnings("resource")
			final FastBufferedReader fbr = this.fbr != null ? this.fbr : new FastBufferedReader( new FileReader( graphFile + ASCII_GRAPH_EXTENSION ) );
			final MutableString s = new MutableString();
			// We skip up to from, but we skip the first line only if this is not a read-once scan (in that case the constructor has read the first line).
			for ( int i = from + ( this.fbr != null ? 0 : 1 ); i-- != 0; )
				fbr.readLine( s );

			final StreamTokenizer st = new StreamTokenizer( fbr );
			st.eolIsSignificant( true );
			st.parseNumbers();

			return new NodeIterator() {
				int i = from;

				IntArrayList successors = new IntArrayList();

				public boolean hasNext() {
					return i < n;
				}

				public int[] successorArray() {
					return successors.elements();
				}
				
				public int nextInt() {
					if ( ! hasNext() ) throw new NoSuchElementException();
					successors.clear();
					int tokenType, dep;

					try {
						do {
							tokenType = st.nextToken();
							if ( tokenType == StreamTokenizer.TT_NUMBER ) {
								successors.add( dep = (int)st.nval );
								if ( dep < 0 || dep >= n )
									throw new IOException( "The value " + dep + " is not a node index at line " + st.lineno() );
							}
							else if ( tokenType != StreamTokenizer.TT_EOL ) {
								throw new IOException( "Unexpected token " + st.toString() );
							}
						} while ( tokenType != StreamTokenizer.TT_EOL );
					}
					catch ( IOException e ) {
						throw new RuntimeException( e );
					}

					return i++;
				}

				@Override
				public int outdegree() {
					return successors.size();
				}

			};
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	public static ImmutableGraph loadSequential( CharSequence basename ) throws IOException {
		return loadOffline( basename );
	}

	public static ASCIIGraph loadSequential( CharSequence basename, ProgressLogger unused ) throws IOException {
		return loadOffline( basename, unused );
	}
	
	public static ASCIIGraph loadOffline( CharSequence basename ) throws IOException {
		return loadOffline( basename, (ProgressLogger)null );
	}

	public static ASCIIGraph loadOffline( CharSequence basename, ProgressLogger unused ) throws IOException {
		return new ASCIIGraph( basename );
	}

	public static ASCIIGraph loadOnce( final InputStream is ) throws IOException {
		return new ASCIIGraph( is );
	}

	public static ImmutableGraph load( CharSequence basename ) throws IOException {
		return load( basename, (ProgressLogger)null );
	}

	public static ImmutableGraph load( CharSequence basename, ProgressLogger unused ) throws IOException {
		return new ArrayListMutableGraph( loadOffline( basename ) ).immutableView();
	}

	public static void store( ImmutableGraph graph, CharSequence basename, @SuppressWarnings("unused") ProgressLogger unused ) throws IOException {
		store( graph, basename );
	}

	public static void store( ImmutableGraph graph, CharSequence basename ) throws IOException {
		store( graph, 0, basename );
	}
	
	public static void store( ImmutableGraph graph, final int shift, CharSequence basename ) throws IOException {
		final PrintStream ps = new PrintStream( new FastBufferedOutputStream( new FileOutputStream( basename + ASCII_GRAPH_EXTENSION ) ), false, Charsets.US_ASCII.toString() );
		int n = graph.numNodes();
		LazyIntIterator successors;

		ps.println( n );
		for ( NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator.hasNext(); ) {
			nodeIterator.nextInt();
			int d = nodeIterator.outdegree();
			successors = nodeIterator.successors();
			while ( d-- != 0 ) ps.print( ( successors.nextInt() + shift ) + " " );
			ps.println();
		}
		ps.close();
	}

	public static void main( String args[] ) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, JSAPException, ClassNotFoundException, InstantiationException  {
		String sourceBasename, destBasename;
		Class<?> graphClass;

		SimpleJSAP jsap = new SimpleJSAP( ASCIIGraph.class.getName(), "Reads a graph with a given basename, or a given spec, and writes it out in ASCII format with another basename",
				new Parameter[] {
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph" ),
						new FlaggedOption( "shift", JSAP.INTEGER_PARSER, null, JSAP.NOT_REQUIRED, 'S', "shift", "A shift that will be added to each node index." ),
						new Switch( "spec", 's', "spec", "The source is not a basename but rather a spec of the form ImmutableGraphClass(arg,arg,...)." ),
						new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ),
						new UnflaggedOption( "sourceBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the source graph, or a source spec if --spec was given; it is immaterial when --once is specified." ),
						new UnflaggedOption( "destBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the destination graph" ),
					}		
				);
				
		JSAPResult jsapResult = jsap.parse( args );
		if ( jsap.messagePrinted() ) System.exit( 1 );
		
		graphClass = jsapResult.getClass( "graphClass" );
		sourceBasename = jsapResult.getString( "sourceBasename" );
		destBasename = jsapResult.getString( "destBasename" );
		final boolean spec = jsapResult.getBoolean( "spec" );

		final ProgressLogger pl = new ProgressLogger( LOGGER, jsapResult.getLong( "logInterval" ), TimeUnit.MILLISECONDS );

		if ( graphClass != null && spec ) {
			System.err.println( "Options --graphClass and --spec are incompatible" );
			return;
		}

		ImmutableGraph graph;
		if ( !spec )
			graph = graphClass != null 
			? (ImmutableGraph)graphClass.getMethod( "loadSequential", CharSequence.class, ProgressLogger.class ).invoke( null, sourceBasename, pl )
			: ImmutableGraph.loadSequential( sourceBasename, pl );
		else
			graph = ObjectParser.fromSpec( sourceBasename, ImmutableGraph.class, GraphClassParser.PACKAGE );
		if ( jsapResult.userSpecified( "shift" ) ) ASCIIGraph.store( graph, jsapResult.getInt( "shift" ), destBasename );
		else ASCIIGraph.store( graph, destBasename );
	}
}
