package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2013-2014 Sebastiano Vigna 
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

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;


/** An adapter exposing an {@link ImmutableGraph} that can be filled incrementally using
 * a family of {@linkplain #add(int[], int, int) addition methods} that make it possible to specify
 * the list of successors of each node in increasing order. At the end of the process, the user
 * must add the special marker list {@link #END_OF_GRAPH}.
 * 
 * <p>The class provides a single
 * call to {@link #nodeIterator()}: once the returned {@link NodeIterator} has been exhausted, {@link #numNodes()} will return the number of nodes, 
 * which will be equal to the number of calls to addition methods.
 * 
 * <p>The class works using a producer/consumer patten: in a typical usage, the thread invoking the
 * addition method will be different from the thread performing the traversal, as in 
 * <pre class= code>
 *	final IncrementalImmutableSequentialGraph g = new IncrementalImmutableSequentialGraph();
 *	ExecutorService executor = Executors.newSingleThreadExecutor();
 *	final Future<Void> future = executor.submit( new Callable&lt;Void>() {
 *		public Void call() throws IOException {
 *			BVGraph.store( g, basename );
 *			return null;
 *		}
 *	} );
 *	
 *	// Do one add() for each node, to specify the successors
 *
 *	g.add( IncrementalImmutableSequentialGraph.END_OF_GRAPH );
 *	future.get();
 *	executor.shutdown();
 *</pre>
 */

public class IncrementalImmutableSequentialGraph extends ImmutableSequentialGraph {
	/** A marker for the end of the graph. */
	public static int[] END_OF_GRAPH = new int[ 0 ]; 
	
	/** The number of nodes (known after a traversal). */
	private int n;
	/** The queue connecting the add methods and node iterator successor mehotds. */
	private final ArrayBlockingQueue<int[]> successorQueue;
	
	public IncrementalImmutableSequentialGraph() {
		n = -1;
		this.successorQueue = new ArrayBlockingQueue<int[]>( 100 );
	}
	
	@Override
	public int numNodes() {
		if ( n == -1 ) throw new UnsupportedOperationException( "The number of nodes is unknown (you need to complete a traversal)" );
		return n;
	}
	
	public NodeIterator nodeIterator() {
		if ( n != -1 ) throw new IllegalStateException();
		return new NodeIterator() {
			int i = 0;
			private int[] currentSuccessor;
			private int[] nextSuccessor;
			@Override
			public boolean hasNext() {
				if ( nextSuccessor == END_OF_GRAPH ) return false;
				if ( nextSuccessor != null ) return true;
				
				try {
					nextSuccessor = successorQueue.take();
				}
				catch ( InterruptedException e ) {
					throw new RuntimeException( e.getMessage(), e );
				}
				
				final boolean end = nextSuccessor == END_OF_GRAPH;
				if ( end ) n = i;
				return ! end;
			}
			
			@Override
			public int nextInt() {
				if ( ! hasNext() ) throw new NoSuchElementException();
				currentSuccessor = nextSuccessor;
				nextSuccessor = null;
				return i++;
			}
			
			@Override
			public int outdegree() {
				if ( currentSuccessor == null ) throw new IllegalStateException();
				return currentSuccessor.length;
			}
			
			@Override
			public int[] successorArray() {
				if ( currentSuccessor == null ) throw new IllegalStateException();
				return currentSuccessor;
			}
		};
	}

	/** Adds a new node having as successors contained in the specified array fragment.
	 * 
	 * 	
	 * <p>The array must be sorted in increasing order.
	 * 
	 * @param successor an array.
	 * @param offset the first valid entry in <code>successor</code>.
	 * @param length the number of valid entries.
	 */
	public void add( final int[] successor, final int offset, final int length ) throws InterruptedException {
		successorQueue.put( Arrays.copyOfRange( successor, offset, offset + length ) );
	}

	/** Adds a new node having as successors contained in the specified array.
	 * 
	 * <p>The array must be sorted in increasing order.
	 * 
	 * @param successor an array.
	 */
	public void add( final int[] successor ) throws InterruptedException {
		successorQueue.put( successor );
	}
}
