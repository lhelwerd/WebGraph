package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2007-2014 Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/** An {@link ArcListASCIIGraph} with fixed shift -1. Very useful to read
 * graphs specified as pairs of arcs with node numbering starting from one. 
 *
 *  <h2>Using {@link ArcListASCIIGraph} with MatLab-like sparse matrix files</h2>
 *  
 *  <p>The main intended usage of this class is that of interfacing easily with MatLab-like
 *  sparse matrix files. Note that for this to happen it is necessary to shift by one all
 *  indices. Assume you have a file named <samp>example.arcs</samp>:
 *  <pre>
 *  1 2
 *  2 3
 *  3 2
 *  </pre>
 *  Then, the command 
 *  <pre>
 *  java it.unimi.dsi.webgraph.BVGraph -1 -g ShiftedByOneArcListASCIIGraph dummy bvexample &lt;example.arcs
 *  </pre>
 *  will generate a {@link BVGraph} as expected (e.g, there is an arc from 0 to 1). 
 */

public final class ShiftedByOneArcListASCIIGraph extends ArcListASCIIGraph {

	protected ShiftedByOneArcListASCIIGraph( InputStream is, int shift ) throws NumberFormatException, IOException {
		super( is, shift );
	}

	public static ImmutableGraph loadSequential( CharSequence basename ) throws IOException {
		return load( basename );
	}

	public static ImmutableGraph loadSequential( CharSequence basename, ProgressLogger unused ) throws IOException {
		return load( basename );
	}
	
	public static ImmutableGraph loadOffline( CharSequence basename ) throws IOException {
		return load( basename );
	}

	public static ImmutableGraph loadOffline( CharSequence basename, ProgressLogger unused ) throws IOException {
		return load( basename );
	}

	public static ArcListASCIIGraph loadOnce( final InputStream is ) throws IOException {
		return new ArcListASCIIGraph( is, -1 );
	}

	public static ImmutableGraph load( CharSequence basename ) throws IOException {
		return load( basename, null );
	}

	public static ImmutableGraph load( CharSequence basename, ProgressLogger unused ) throws IOException {
		return new ArrayListMutableGraph( loadOnce( new FastBufferedInputStream( new FileInputStream( basename.toString() ) ) ) ).immutableView();
	}

	public static void store( ImmutableGraph graph, CharSequence basename, ProgressLogger unused ) throws IOException {
		store( graph, basename, 1 );
	}

	public static void main( final String arg[] ) throws NoSuchMethodException {
		throw new NoSuchMethodException( "Please use the main method of " + ArcListASCIIGraph.class.getSimpleName() + "." );
	}
}
