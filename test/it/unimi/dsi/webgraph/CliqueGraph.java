package it.unimi.dsi.webgraph;

/* 
   Copyright (C) 2010-2014 Sebastiano Vigna

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.


 */


/** A bidirectional chain of cliques.
 * 
 * @author Sebastiano Vigna
 */

public final class CliqueGraph extends ImmutableGraph {
	/** The number of nodes in the graph. */
	private final int n;
	/** The number of elements per clique. */
	private final int c;

	/** Creates a new bidirectional chain of cliques of given size.
	 * 
	 * @param n the overall number of nodes (will be rounded down to the nearest multiple of <code>c</code>).
	 * @param c the size of each clique.
	 */
	public CliqueGraph( int n, int c ) {
		this.n = n - n % c;
		this.c = c;
		
	}

	/** Creates a new clique of given size.
	 * 
	 * @param n the size of the clique.
	 */
	public CliqueGraph( int n ) {
		this( n, n );
	}
	
	/** Creates a new bidirectional chain of cliques of given size.
	 * 
	 * @param n the overall number of nodes (will be rounded down to the nearest multiple of <code>c</code>).
	 * @param c the size of each clique.
	 */
	public CliqueGraph( String n, String c ) {
		this( Integer.parseInt( n ), Integer.parseInt( c ) );
	}

	/** Creates a new clique of given size.
	 * 
	 * @param n the size of the clique.
	 */
	public CliqueGraph( String n ) {
		this( Integer.parseInt( n ) );
	}

	@Override
	public ImmutableGraph copy() {
		return this;
	}

	@Override
	public int numNodes() {
		return n;
	}

	@Override
	public long numArcs() {
		return (long)n * c - n + ( n != c ? 2 * ( n / c ) : 0 );
	}

	@Override
	public int outdegree( int x ) {
		return c - 1 + ( x % c == 0 && n != c ? 2 : 0 );
	}

	@Override
	public boolean randomAccess() {
		return true;
	}

	@Override
	public int[] successorArray( final int x ) {
		int[] succ = new int[ outdegree( x ) ];
		final int start = x - x % c;
		if ( succ.length == c - 1 ) {
			for( int i = 0, j = 0; i < c; i++ ) if ( start+ i != x ) succ[ j++ ] = start + i; 
		}
		else {
			succ[ 0 ] = ( x - c + n ) % n;
			for( int i = 0, j = 1; i < c; i++ ) if ( start + i != x ) succ[ j++ ] = start + i; 
			succ[ c ] = ( x + c ) % n;
		}
		
		return succ;
	}

}
