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

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.lang.FlyweightPrototype;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A simple abstract class representing an immutable graph.
 *
 * <P>Subclasses of this class are used to create and access <em>immutable graphs</em>, that is,
 * graphs that are computed once for all, stored conveniently, and then accessed repeatedly.
 * Moreover, immutable graphs are usually very large&mdash;so large that two such graphs may not
 * fit into central memory (the main example being a sizable portion of the web).
 *
 * <P>A subclass of this class must implement methods to obtain the {@linkplain
 * #numNodes() number of nodes}, the {@linkplain #outdegree(int) outdegree of a
 * node} and the successors of a node (either {@link #successors(int)}
 * or {@link #successorArray(int)}). Additionally, it may provide methods to
 * obtain the {@linkplain #numNodes() number of arcs}, and a {@linkplain #basename() basename}.
 *
 * <P>This class provides {@link #equals(Object)} and {@link #hashCode()} methods that consider
 * two graph equals if they have the same size and all their successor lists are equal.
 *  
 * <H2>Iterating on successors</H2>
 * 
 * <p>Starting with WebGraph 2.0, the iterator architecture is <em>fully lazy</em>&mdash;you have no
 * <code>hasNext()</code> method. Rather, the {@link LazyIntIterator} returned by {@link #successors(int)}
 * will return -1 when no more successors are available. The idiomatic forms for enumerating successors
 * <it>via</it> iterators are
 * <pre>
 * LazyIntIterator successors = g.successors( x );
 * int d = g.outdegree( x );
 * while( d-- != 0 ) doSomething( successors.nextInt() );
 * </pre>
 * and
 * <pre>
 * LazyIntIterator successors = g.successors( x );
 * int t;
 * while( ( t = successors.nextInt() ) != -1 ) doSomething( t );
 * </pre>
 * 
 * <p>The alternative method {@link #successorArray(int)} provides an array containing the successors
 * <em>and possibly more elements</em>. Use {@link #outdegree(int)} to know how many elements are valid.
 * The efficiency of {@link #successors(int)} and {@link #successorArray(int)} may vary depending on the
 * implementation.
 *  
 * <H2>Refactoring for WebGraph 2+</H2>
 * 
 * <p>If you want to refactor code using an older version of WebGraph for WebGraph 2+, there is a trivial way:
 * each call to {@link #successors(int)} must be wrapped into an eager (i.e., Java standard) iterator. If your
 * code was something like
 * <pre>
 * IntIterator successors = g.successors( x );
 * while( successors.hasNext() ) doSomething( successors.nextInt() );
 * </pre>
 * it is sufficient to modify it as follows:
 * <pre>
 * IntIterator successors = LazyIntIterators.eager( g.successors( x ) );
 * while( successors.hasNext() ) doSomething( successors.nextInt() );
 * </pre>
 * 
 * <p>If, however, you where not using <code>hasNext()</code>, but rather the first idiomatic
 * form described above, changing the type of <code>successors</code> to {@link LazyIntIterator} will do the job.
 * 
 * 
 * <H2>Building an immutable graph</H2> 
 *
 * <P>Due to their large size, immutable
 * graphs have a peculiar serialisation scheme. Every subclass of this class
 * <strong>must</strong> implement a number of static methods that create an immutable
 * graph, given a string (usually a basename for a set of files) and, optionally, a {@link it.unimi.dsi.logging.ProgressLogger}.
 * The signatures that <strong>must</strong> be implemented are 
 * <UL>
 * <LI><code>ImmutableGraph load(CharSequence, ProgressLogger)</code>;
 * <LI><code>ImmutableGraph load(CharSequence)</code>;
 * <LI><code>ImmutableGraph loadSequential(CharSequence, ProgressLogger)</code>;
 * <LI><code>ImmutableGraph loadSequential(CharSequence)</code>;
 * <LI><code>ImmutableGraph loadOffline(CharSequence, ProgressLogger)</code>;
 * <LI><code>ImmutableGraph loadOffline(CharSequence)</code>.
 * <LI><code>ImmutableGraph loadOnce(InputStream)</code>;
 * </UL>
 * 
 * <p>Additionally, the following signatures <strong>can</strong> be implemented:
 * <UL>
 * <LI><code>ImmutableGraph loadMapped(CharSequence, ProgressLogger)</code>;
 * <LI><code>ImmutableGraph loadMapped(CharSequence)</code>;
 * </UL>
 *  
 * <P>The special semantics associated with <code>loadSequential()</code> is that the graph should be loaded
 * in a particular compact form that just allows sequential access. The special semantics associated to <code>loadOffline()</code> 
 * is that the immutable graph should be set up, and possibly some metadata could be read from disk, but no
 * actual data is loaded into memory; the class should guarantee that offline sequential access (i.e., by means
 * of {@link #nodeIterator(int)}) is still possible. In other words, in most cases {@link #nodeIterator(int)} will have to be
 * overridden by the subclasses to behave properly even in an offline setting (see {@link #nodeIterator()}).
 * The special semantics associated with <code>loadOnce()</code> is that the graph can be traversed
 * <em>just once</em> using a call to {@link #nodeIterator()}. The special semantics associated with <code>loadMapped()</code>
 * is that metadata could be read from disk, but the graph will be accessed by memory mapping; the class
 * should guarantee that random access is possible. 
 * 
 * <P>Note that a simple class may just implement all special forms of graph loading delegating to the standard
 * load method (see, e.g., {@link it.unimi.dsi.webgraph.ASCIIGraph}).
 * Specific implementations of {@link ImmutableGraph} may also decide to expose internal load methods
 * to make it easier to write load methods for subclasses 
 * (see, e.g., {@link it.unimi.dsi.webgraph.BVGraph#loadInternal(CharSequence, int, ProgressLogger) loadInternal()}).
 *
 * <P>Analogously, a subclass of this class <strong>may</strong> also implement
 * <UL>
 * <LI><code>store(ImmutableGraph, CharSequence, ProgressLogger)</code>;
 * <LI><code>store(ImmutableGraph, CharSequence)</code>.
 * </UL> 
 * 
 * These methods must store in compressed form a given immutable graph, using the default values
 * for compression parameters, etc. It is likely, however, that more 
 * of <code>store</code> methods are available, as parameters vary wildly
 * from subclass to subclass. The method {@link #store(Class, ImmutableGraph, CharSequence, ProgressLogger)}
 * invokes by reflection the methods above on the provided class.
 *
 * <P>The standard method to build a new immutable graph is creating a (possibly anonymous) class
 * that extends this class, and save it using a concrete subclass (e.g., {@link it.unimi.dsi.webgraph.BVGraph}). See
 * the source of {@link it.unimi.dsi.webgraph.Transform} for several examples.
 * 
 * <H2>Properties Conventions</H2>
 * 
 * <P>To provide a simple way to load an immutable graph without knowing in advance its class,
 * the following convention may be followed: a graph with basename <var><samp>name</samp></var> may feature
 * a Java property file <samp><var>name</var>.properties</samp> with a property <samp>graphclass</samp>
 * containing the actual class of the graph. In this case, you can use the implementation of the load/store
 * methods contained in this class, similarly to the standard Java serialisation scheme. {@link BVGraph}, for instance,
 * follows this convention, but {@link ASCIIGraph} does not.
 * 
 * <P>The reason why this convention is not enforced is that it is sometimes useful to write lightweight classes,
 * mostly for debugging purposes, whose graph representation is entirely contained in a single file (e.g., {@link ASCIIGraph}),
 * so that {@link #loadOnce(InputStream)} can be easily implemented.
 * 
 * <H2>Facilities for loading an immutable graph</H2>
 * 
 * <P>{@link ImmutableGraph} provides ready-made implementations of the load methods that work as follows: they
 * opens a property file with the given basename, and look for the <samp>graphclass</samp> property; then, they simply
 * delegates the actual load to the specified graph class by reflection.
 * 
 * <h2>Thread-safety and flyweight copies</h2>
 * 
 * <p>Implementations of this class need not be thread-safe. However, they implement the
 * {@link FlyweightPrototype} pattern: the {@link #copy()} method is
 * thread-safe and will return a lightweight copy of the graph&mdash;usually, all immutable
 * data will be shared between copies. Concurrent access to different copies is safe.
 * 
 * <p>Note that by contract {@link #copy()} is guaranteed to work only if {@link #randomAccess()}
 * returns true.
 */


public abstract class ImmutableGraph implements FlyweightPrototype<ImmutableGraph> {
	private final static Logger LOGGER = LoggerFactory.getLogger( ImmutableGraph.class );
	
	public static final String GRAPHCLASS_PROPERTY_KEY = "graphclass";
	/** The standard extension of property files. */
	public static final String PROPERTIES_EXTENSION = ".properties";

	/** A list of the methods that can be used to load a graph. They are used
	 * by {@link ImmutableGraph} and other classes to represent standard 
	 * (i.e., random access), sequential, offline and read-once graph loading. */
	
	public static enum LoadMethod {
		STANDARD,
		SEQUENTIAL,
		OFFLINE,
		ONCE,
		MAPPED;
		
		public String toMethod() {
			switch(this) {
			case STANDARD: return "load";
			case SEQUENTIAL: return "loadSequential";
			case OFFLINE: return "loadOffline";
			case ONCE: return "loadOnce";
			case MAPPED: return "loadMapped";
			default: throw new AssertionError();
			}
		}
	};
	
	/** Returns the number of nodes of this graph. 
	 *
	 * <p>Albeit this method is not optional, it is allowed that this method throws
	 * an {@link UnsupportedOperationException} if this graph has never been entirely
	 * traversed using a {@link #nodeIterator() node iterator}. This apparently bizarre
	 * behaviour is necessary to support implementations as {@link ArcListASCIIGraph}, which
	 * do not know the actual number of nodes until a traversal has been completed.
	 *
	 * @return the number of nodes.
	 */
	public abstract int numNodes();

	/** Returns the number of arcs of this graph (optional operation). 
	 *
	 * @return the number of arcs.
	 */
	public long numArcs() {
		throw new UnsupportedOperationException();
	}

	/** Checks whether this graph provides random access to successor lists. 
	 * 
	 * @return true if this graph provides random access to successor lists.
	 */
	public abstract boolean randomAccess(); 
	
	/** Returns a symbolic basename for this graph (optional operation).
	 *
	 * <P>Implementors of this class may provide a basename (usually
	 * a pathname from which various files storing the graph are stemmed).
	 * This method is optional because it is sometimes unmeaningful (e.g.,
	 * for one-off anonymous classes).
	 *
	 * @return the basename.
	 */
	public CharSequence basename() {
		throw new UnsupportedOperationException();
	}

	/** Returns a lazy iterator over the successors of a given node. The iteration terminates
	 * when -1 is returned.
	 * 
	 * <P>This implementation just wraps the array returned by {@link #successorArray(int)}. Subclasses 
	 * are encouraged to override this implementation.
	 *
	 * <p>The semantics of this method has been significantly modified in WebGraph 2.0 to take advantage of the new,
	 * faster lazy architecture.
	 * 
	 * @param x a node.
	 * @return a lazy iterator over the successors of the node.
	 */
	public LazyIntIterator successors( final int x ) {
		return LazyIntIterators.wrap( successorArray( x ), outdegree( x  ) );
	}

	/** Returns a reference to an array containing the successors of a given node.
	 * 
	 * <P>The returned array may contain more entries than the outdegree of <code>x</code>.
	 * However, only those with indices from 0 (inclusive) to the outdegree of <code>x</code> (exclusive)
	 * contain valid data.
	 * 
	 * <P>This implementation just unwraps the iterator returned by {@link #successors(int)}. Subclasses 
	 * are encouraged to override this implementation.
	 * 
	 * <p><strong>Warning</strong>: all implementations must guarantee that a distinct array is returned for
	 * each node. The caller, in turn, must treat the array as a read-only object.
	 * 
	 * 
	 * @param x a node.
	 * @return an array whose first elements are the successors of the node; the array must not
	 * be modified by the caller.
	 */
	public int[] successorArray( final int x ) {
		final int[] successor = new int[ outdegree( x ) ];
		LazyIntIterators.unwrap( successors( x ), successor );
		return successor;
	}

	/** Returns the outdegree of a node.
	 * 
	 * @param x a node.
	 * @throws IllegalStateException if called without offsets.
	 * @return the outdegree of the given node.
	 */
	public abstract int outdegree( int x );

	/** Returns a node iterator for scanning the graph sequentially, starting from the given node.
	 *
	 *  <P>This implementation just calls the random-access methods ({@link #successors(int)} and
	 *  {@link #outdegree(int)}). More specific implementations may choose to maintain some extra state
	 *  to make the enumeration more efficient.
	 *
	 *  @param from the node from which the iterator will iterate.
	 *  @return a {@link NodeIterator} for accessing nodes and successors sequentially.
	 */
	public NodeIterator nodeIterator( final int from ) {
		return new NodeIterator() {
				int curr = from - 1;
				final int n = numNodes();

				public int nextInt() {
					if ( ! hasNext() ) throw new java.util.NoSuchElementException();
					return ++curr;
				}

				public boolean hasNext() {
					return ( curr < n - 1 );
				}

				public LazyIntIterator successors() {
					if ( curr == from - 1 ) throw new IllegalStateException();
					return ImmutableGraph.this.successors( curr );
				}

				public int outdegree() {
					if ( curr == from - 1 ) throw new IllegalStateException();
					return ImmutableGraph.this.outdegree( curr );
				}
				
			};
	}
	
	/** Returns a node iterator for scanning the graph sequentially, starting from the first node.
	 *
	 *  @return a {@link NodeIterator} for accessing nodes and successors sequentially.
	 */
	public NodeIterator nodeIterator() {
		return nodeIterator( 0 );
	}

	/** Returns a flyweight copy of this immutable graph.
	 * 
	 * @return a flyweight copy of this immutable graph.
	 * @throws UnsupportedOperationException if flyweight copies are not supported:
	 * support is guaranteed only if {@link #randomAccess()} returns true.
	 * @see FlyweightPrototype
	 */

	public abstract ImmutableGraph copy();
	
	public String toString() {
		final StringBuilder s = new StringBuilder();

		long numArcs = -1;
		try {
			numArcs = numArcs();
		}
		catch( UnsupportedOperationException ignore ) {}
		
		s.append( "Nodes: " + numNodes() + "\nArcs: " + ( numArcs == -1 ? "unknown" : Long.toString( numArcs ) ) + "\n" );

		final NodeIterator nodeIterator = nodeIterator();
		LazyIntIterator successors;
		int curr;
		for ( int i = numNodes(); i-- != 0; ) {
			curr = nodeIterator.nextInt();
			s.append( "Successors of " + curr + " (degree " + nodeIterator.outdegree() + "):" );
			successors = nodeIterator.successors();
			int d = nodeIterator.outdegree();
			while ( d-- != 0 ) s.append( " " + successors.nextInt() );
			s.append( '\n' );
		}
		return s.toString();
	}

	/** Returns an iterator enumerating the outdegrees of the nodes of this graph.
	 * 
	 * @return  an iterator enumerating the outdegrees of the nodes of this graph.
	 */
	public IntIterator outdegrees() {
		return randomAccess() ? 
		new AbstractIntIterator() {
			private final int n = numNodes();
			private int next = 0;
			@Override
			public boolean hasNext() {
				return next < n;
			}
			@Override
			public int nextInt() {
				if ( ! hasNext() ) throw new NoSuchElementException();
				return outdegree( next++ ); 
			}
		} :
		new AbstractIntIterator() {
			private final NodeIterator nodeIterator = nodeIterator();
			@Override
			public boolean hasNext() {
				return nodeIterator.hasNext();
			}
			@Override
			public int nextInt() {
				nodeIterator.nextInt();
				return nodeIterator.outdegree();
			}
		};
	}
	
	
	/** Creates a new {@link ImmutableGraph} by loading a graph file from disk to memory, without 
	 *  offsets.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static ImmutableGraph loadSequential( CharSequence basename ) throws IOException {
		return load( LoadMethod.SEQUENTIAL, basename, null );
	}

	/** Creates a new {@link ImmutableGraph} by loading a graph file from disk to memory, without 
	 *  offsets.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */

	public static ImmutableGraph loadSequential( CharSequence basename, ProgressLogger pl ) throws IOException {
		return load( LoadMethod.SEQUENTIAL, basename, null, pl );
	}

	/** Creates a new {@link ImmutableGraph} by loading offline a graph file.
	 * 
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */

	public static ImmutableGraph loadOffline( CharSequence basename ) throws IOException {
		return load( LoadMethod.OFFLINE, basename, null );
	}


	/** Creates a new {@link ImmutableGraph} by loading offline a graph file.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */

	public static ImmutableGraph loadOffline( CharSequence basename, ProgressLogger pl ) throws IOException {
		return load( LoadMethod.OFFLINE, basename, null, pl );
	}


	/** Creates a new {@link ImmutableGraph} by memory-mapping a graph file.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the offsets, or <code>null</code>.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while memory-mapping the graph or reading the offsets.
	 */

	public static ImmutableGraph loadMapped( CharSequence basename, ProgressLogger pl ) throws IOException {
		return load( LoadMethod.MAPPED, basename, null, pl );
	}

	/** Creates a new {@link ImmutableGraph} by memory-mapping a graph file.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while memory-mapping the graph or reading the offsets.
	 */

	public static ImmutableGraph loadMapped( CharSequence basename ) throws IOException {
		return load( LoadMethod.MAPPED, basename, null );
	}

	
	/** Creates a new {@link ImmutableGraph} by loading a read-once graph from an input stream.
	 * 
	 * <p>This implementation just throws a {@link UnsupportedOperationException}. There
	 * is no way to write a generic implementation, because there is no way to know
	 * in advance the class that should read the graph.
	 * 
	 * @param is an input stream containing the graph.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 * @throws UnsupportedOperationException if this graph class does not support read-once graphs.
	 */

	public static ImmutableGraph loadOnce( final InputStream is ) throws IOException {
		throw new UnsupportedOperationException( "This class does not support read-once loading" );
	}

	
	/** Creates a new {@link ImmutableGraph} by loading a graph file from disk to memory, with 
	 *  all offsets, using no progress logger.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */


	public static ImmutableGraph load( CharSequence basename ) throws IOException {
		return load( LoadMethod.STANDARD, basename, null );
	}

	/** Creates a new {@link ImmutableGraph} by loading a graph file from disk to memory, with 
	 *  all offsets, using a progress logger.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */

	public static ImmutableGraph load( CharSequence basename, ProgressLogger pl ) throws IOException {
		return load( LoadMethod.STANDARD, basename, null, pl );
	}

	private static final ProgressLogger UNUSED = new ProgressLogger();

	/** Creates a new {@link ImmutableGraph} using the given method and no progress logger.
	 *
	 * @param method the load method.
	 * @param basename the basename of the graph, if <code>method</code> is not {@link LoadMethod#ONCE}.
	 * @param is an input stream the containing the graph, if <code>method</code> is {@link LoadMethod#ONCE}.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */	
	private static ImmutableGraph load( LoadMethod method, CharSequence basename, InputStream is ) throws IOException {
		return load( method, basename, is, UNUSED );
	}

	/** Creates a new immutable graph by loading a graph file from disk to memory, delegating the 
	 *  actual loading to the class specified in the <samp>graphclass</samp> property within the property
	 *  file (named <samp><var>basename</var>.properties</samp>). The exact load method to be used 
	 *  depends on the <code>method</code> argument.
	 * 
	 * <P>This method uses the properties convention described in the {@linkplain ImmutableGraph introduction}.
	 *
	 * @param method the method to be used to load the graph.
	 * @param basename the basename of the graph, if <code>method</code> is not {@link LoadMethod#ONCE}.
	 * @param is an input stream the containing the graph, if <code>method</code> is {@link LoadMethod#ONCE}.
	 * @param pl the progress logger; it can be <code>null</code>.
	 * @return an {@link ImmutableGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */	
	protected static ImmutableGraph load( LoadMethod method, CharSequence basename, InputStream is, ProgressLogger pl ) throws IOException {
		final FileInputStream propertyFile = new FileInputStream( basename + PROPERTIES_EXTENSION );
		final Properties properties = new Properties();
		String graphClassName;
		properties.load( propertyFile );
		propertyFile.close();

		if ( ( graphClassName = properties.getProperty( GRAPHCLASS_PROPERTY_KEY ) ) == null ) throw new IOException( "The property file for " + basename + " does not contain a graphclass property" );

		// Small kludge to fix old usage of toString() instead of getName();
		if ( graphClassName.startsWith( "class " ) ) graphClassName = graphClassName.substring( 6 );

		// Small kludge to try to load small graphs created with the big version.
		if ( graphClassName.startsWith( "it.unimi.dsi.big.webgraph" ) ) {
			final String standardGraphClassName = graphClassName.replace( "it.unimi.dsi.big.webgraph", "it.unimi.dsi.webgraph" );
			LOGGER.warn( "Replacing class " + graphClassName + " with " + standardGraphClassName );
			graphClassName = standardGraphClassName;
		}
		
		final Class<?> graphClass;
		ImmutableGraph graph = null;

		try {
			graphClass = Class.forName( graphClassName );
			
			if ( method == LoadMethod.ONCE ) graph = (ImmutableGraph)graphClass.getMethod( method.toMethod(), InputStream.class ).invoke( null, is );
			else {
				if ( pl == UNUSED ) graph = (ImmutableGraph)graphClass.getMethod( method.toMethod(), CharSequence.class ).invoke( null, basename );
				else graph = (ImmutableGraph)graphClass.getMethod( method.toMethod(), CharSequence.class, ProgressLogger.class ).invoke( null, basename, pl );				
			}
		} catch ( InvocationTargetException e ) {
			if ( e.getCause() instanceof IOException ) throw (IOException) e.getCause();
			throw new RuntimeException( e );
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}

		return graph;
	}


	/** Stores an immutable graph using a specified subclass and a progress logger.
	 * 
	 * <P>This method is a useful shorthand that invoke by reflection the store method of a given subclass.
	 * Note, however, that usually a subclass will provide more refined store methods with more parameters.
	 *
	 * @param graphClass the subclass of {@link ImmutableGraph} that should store the graph.
	 * @param graph the graph to store.
	 * @param basename the basename.
	 * @param pl a progress logger, or <code>null</code>.
	 */

	public static void store( final Class<?> graphClass, final ImmutableGraph graph, final CharSequence basename, final ProgressLogger pl ) throws IOException {
		if ( ! ImmutableGraph.class.isAssignableFrom( graphClass ) ) throw new ClassCastException( graphClass.getName() + " is not a subclass of ImmutableGraph" );
		try {
			if ( pl == UNUSED ) graphClass.getMethod( "store", ImmutableGraph.class, CharSequence.class ).invoke( null, graph, basename );
			else graphClass.getMethod( "store", ImmutableGraph.class, CharSequence.class, ProgressLogger.class ).invoke( null, graph, basename, pl );
		} catch ( InvocationTargetException e ) {
			if ( e.getCause() instanceof IOException ) throw (IOException) e.getCause();
			throw new RuntimeException( e );
		} catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}
	
	/** Stores an immutable graph using a specified subclass.
	 * 
	 * @param graphClass the subclass of {@link ImmutableGraph} that should store the graph.
	 * @param graph the graph to store.
	 * @param basename the basename.
	 * @see #store(Class, ImmutableGraph, CharSequence, ProgressLogger)
	 */

	public static void store( final Class<?> graphClass, final ImmutableGraph graph, final CharSequence basename ) throws IOException {
		store( graphClass, graph, basename, UNUSED );
	}
	
	/** Compare this immutable graph to another object.
	 *
	 * @return true iff the given object is an immutable graph of the same size, and
	 * the successor list of every node of this graph is equal to the successor list of the corresponding node of <code>o</code>.
	 */

	public boolean equals( final Object o ) {
		if ( ! (o instanceof ImmutableGraph) ) return false;
		final ImmutableGraph g = (ImmutableGraph) o;
		int n = numNodes();
		if ( n != g.numNodes() ) return false;
		final NodeIterator i = nodeIterator(), j = g.nodeIterator();
		int[] s, t;
		int d;
		while( n-- != 0 ) { 
			i.nextInt();
			j.nextInt();
			if ( ( d = i.outdegree() ) != j.outdegree() ) return false;
			s = i.successorArray();
			t = j.successorArray();
			while( d-- != 0 ) if ( s[ d ] != t[ d ] ) return false;
		}
			
		return true;
	}
	
	/** Returns a hash code for this immutable graph.
	 *
	 * @return a hash code for this immutable graph.
	 */

	public int hashCode() {
		int n = numNodes(), h = -1;
		final NodeIterator i = nodeIterator();
		int[] s;
		int d;
		while( n-- != 0 ) { 
			h = h * 31 + i.nextInt();
			s = i.successorArray();
			d = i.outdegree();
			while( d-- != 0 ) h = h * 31 + s[ d ];
		}

		return h;
	}
	
}
