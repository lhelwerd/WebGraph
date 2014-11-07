package it.unimi.dsi.webgraph.labelling;

/*		 
 * Copyright (C) 2007-2014 Paolo Boldi 
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

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.BVGraphTest;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterators;
import it.unimi.dsi.webgraph.WebGraphTestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

public class CSSerializationTest extends WebGraphTestCase {

	private static final int[] SIZES = { 0, 1, 2, 3, 4 };
	private static final int[] WIDTHS = {  20, 21, 30, 31 };

	public String createGraph( File basename, ImmutableGraph g, int width ) throws IllegalArgumentException, SecurityException, IOException {
		final int n = g.numNodes();
		System.err.println( "Testing " + n + " nodes, width " + width+ ", basename " + basename );

		OutputBitStream labels = new OutputBitStream( basename + "-fixedlabel" + BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION );
		OutputBitStream offsets = new OutputBitStream( basename + "-fixedlabel" + BitStreamArcLabelledImmutableGraph.LABEL_OFFSETS_EXTENSION );
		Label lab;
		offsets.writeGamma( 0 );
		for( int i = 0; i < n; i++ ) {
			int bits = 0;
			for( IntIterator j = LazyIntIterators.eager( g.successors( i ) ); j.hasNext(); ) {
				int succ = j.nextInt();
				lab = new FakeCSFixedWidthIntLabel( "TEST", width, i * succ + i );
				bits += lab.toBitStream( labels, i );
			} 
			offsets.writeGamma( bits );
		}
		labels.close();
		offsets.close();

		PrintWriter pw = new PrintWriter( new FileWriter( basename + "-fixedlabel" + ImmutableGraph.PROPERTIES_EXTENSION ) );
		pw.println( ImmutableGraph.GRAPHCLASS_PROPERTY_KEY + " = " + BitStreamArcLabelledImmutableGraph.class.getName() );
		pw.println( BitStreamArcLabelledImmutableGraph.LABELSPEC_PROPERTY_KEY + " = " + FakeCSFixedWidthIntLabel.class.getName() + "(TEST," + width + ")" );
		pw.println( ArcLabelledImmutableGraph.UNDERLYINGGRAPH_PROPERTY_KEY + " = " + basename.getName() );
		pw.close();
		
		return basename + "-fixedlabel";
	}

	public void testLabels( ArcLabelledImmutableGraph alg, final int width ) {
		
		final int mask = (int)( ( 1L << width ) - 1 );

		// Sequential access, iterators
		for( ArcLabelledNodeIterator nodeIterator = alg.nodeIterator(); nodeIterator.hasNext(); ) {
			int curr = nodeIterator.nextInt();
			ArcLabelledNodeIterator.LabelledArcIterator l = nodeIterator.successors();
			int d = nodeIterator.outdegree();
			while( d-- != 0) {
				int succ = l.nextInt();
				assertEquals( curr + " -> " + succ,( curr * succ + curr ) & mask, l.label().getInt() );
			}
		}

		// Sequential access, arrays
		for( ArcLabelledNodeIterator nodeIterator = alg.nodeIterator(); nodeIterator.hasNext(); ) {
			int curr = nodeIterator.nextInt();
			int d = nodeIterator.outdegree();
			int succ[] = nodeIterator.successorArray();
			Label[] label = nodeIterator.labelArray();
			for( int i = 0; i < d; i++ ) 
				assertEquals( curr + " -> " + succ[ i ], ( curr * succ[ i ] + curr ) & mask, label[ i ].getInt() );
		}

		if ( ! alg.randomAccess() ) return;

		// Random access, iterators
		for( int curr = 0; curr < alg.numNodes(); curr++ ) {
			ArcLabelledNodeIterator.LabelledArcIterator l = alg.successors( curr );
			int d = alg.outdegree( curr );
			while( d-- != 0 ) {
				int succ = l.nextInt();
				assertEquals( curr + " -> " + succ ,( curr * succ + curr ) & mask, l.label().getInt() );
			}
		}

		// Random access, arrays
		for( int curr = 0; curr < alg.numNodes(); curr++ ) {
			int d = alg.outdegree( curr );
			int succ[] = alg.successorArray( curr );
			Label[] label = alg.labelArray( curr );
			for( int i = 0; i < d; i++ ) {
				assertEquals( curr + " -> " + succ[ i ], ( curr * succ[ i ] + curr ) & mask, label[ i ].getInt() );
			}
		}
	}
	
	@Test
	public void testLabels() throws IOException, IllegalArgumentException, SecurityException {
		for( int n: SIZES ) {
			for( int type = 0; type < 3; type++ ) {
				System.err.println( "Testing type " + type + "..." );
				final ImmutableGraph g = type == 0 ? ArrayListMutableGraph.newCompleteGraph( n, false ).immutableView() :
					type == 1 ? ArrayListMutableGraph.newCompleteBinaryIntree( n ).immutableView() :
						ArrayListMutableGraph.newCompleteBinaryOuttree( n ).immutableView();
				final File basename = BVGraphTest.storeTempGraph( g );
				for( int width: WIDTHS ) {
					final String basenameLabel = createGraph( basename, g, width );
					
					System.err.println( "Testing sequential..." );
					testLabels( BitStreamArcLabelledImmutableGraph.loadSequential( basenameLabel ), width );
					System.err.println( "Testing offline..." );
					testLabels( BitStreamArcLabelledImmutableGraph.loadOffline( basenameLabel ), width );
					testLabels( BitStreamArcLabelledImmutableGraph.load( basenameLabel ), width );
					
					new File( basenameLabel + ImmutableGraph.PROPERTIES_EXTENSION ).delete();
					new File( basenameLabel + BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION ).delete();
					new File( basenameLabel + BitStreamArcLabelledImmutableGraph.LABEL_OFFSETS_EXTENSION ).delete();
				}
				basename.delete();
				deleteGraph( basename );
			}
		}
	}
	

}
