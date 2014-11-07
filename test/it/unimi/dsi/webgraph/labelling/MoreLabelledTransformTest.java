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
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.WebGraphTestCase;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreLabelledTransformTest extends WebGraphTestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger( MoreLabelledTransformTest.class );

	@Test
	public void testTransform() throws IOException, IllegalArgumentException, SecurityException {
		File f = File.createTempFile( "test", "transform" );
		f.delete();
		f.mkdir();
		f.deleteOnExit();
		System.out.println( f );
		ProgressLogger pl = new ProgressLogger( LOGGER );
		pl.logInterval = 1;
		
		// Creates an arc-labelled graph
		int[][] arcs;
		ArrayListMutableGraph under = new ArrayListMutableGraph( 6, arcs = new int[][] {
				{ 0, 3 }, { 1, 3 }, { 1, 4 }, { 2, 4 }, { 5, 4 }
		});
		BVGraph.store( under.immutableView(), new File( f, "original" + BitStreamArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX ).toString() );
		OutputBitStream obs = new OutputBitStream( new File( f, "original" + BitStreamArcLabelledImmutableGraph.LABELS_EXTENSION ).toString() );
		OutputBitStream labobs = new OutputBitStream( new FileOutputStream( new File( f, "original" + BitStreamArcLabelledImmutableGraph.LABEL_OFFSETS_EXTENSION ).toString() ) );
		long prev = 0;
		int curr = -1;
		for ( int[] arc: arcs ) {
			while ( arc[ 0 ] != curr ) {
				labobs.writeGamma( (int)( obs.writtenBits() - prev ) );
				prev = obs.writtenBits();
				curr++;
			}
			new FixedWidthIntLabel( "fake", 8, arc[ 0 ] * arc[ 1 ] ).toBitStream( obs, arc[ 0 ] );
		}
		labobs.writeGamma( (int)( obs.writtenBits() - prev ) );
		obs.close();
		labobs.close();
		String graphBasename = new File( f, "original" ).toString();
		PrintWriter pw = new PrintWriter( graphBasename + ArcLabelledImmutableGraph.PROPERTIES_EXTENSION );
		pw.println( BitStreamArcLabelledImmutableGraph.UNDERLYINGGRAPH_PROPERTY_KEY + "=original" + BitStreamArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX );
		pw.println( ArcLabelledImmutableGraph.GRAPHCLASS_PROPERTY_KEY + "=" + BitStreamArcLabelledImmutableGraph.class.getName() );
		pw.println( BitStreamArcLabelledImmutableGraph.LABELSPEC_PROPERTY_KEY + "=" + FixedWidthIntLabel.class.getName() + "(fake,8,0)" );
		pw.close();
		
		// We transpose it
		ArcLabelledImmutableGraph graph = ArcLabelledImmutableGraph.load( graphBasename, pl );
		ArcLabelledImmutableGraph gT = Transform.transposeOffline( graph, 2, null, new ProgressLogger() );
		String baseNameT = graphBasename + "t";
		BVGraph.store( gT, baseNameT + "-underlying" );
		BitStreamArcLabelledImmutableGraph.store( gT, baseNameT, baseNameT + "-underlying" );
		
		// We reload the transpose
		gT = ArcLabelledImmutableGraph.load( baseNameT, pl );
		
		// We merge it with the original one
		LabelMergeStrategy mergeStrategy = null;
		ArcLabelledImmutableGraph gU = Transform.union( graph, gT, mergeStrategy);
		assertGraph( gU );
		String baseNameU = graphBasename + "u";
		BVGraph.store( gU, baseNameU + "-underlying" );
		BitStreamArcLabelledImmutableGraph.store( gU, baseNameU, baseNameU + "-underlying" );
		
		// We reload it
		gU = BitStreamArcLabelledImmutableGraph.load( baseNameU, pl );
		System.out.println( gU );
		
		// Here is what we expect to find
		int[][] expectedSuccessors = new int[][] {
				{ 3 }, // successors of 0
				{ 3, 4 }, // successors of 1
				{ 4 }, // successors of 2
				{ 0, 1 }, // successors of 3
				{ 1, 2, 5 }, // successors of 4
				{ 4 }, // successors of 5
		};
		int[][] expectedLabels = new int[][] {
				{ 0 }, // successors of 0
				{ 3, 4 }, // successors of 1
				{ 8 }, // successors of 2
				{ 0, 3 }, // successors of 3
				{ 4, 8, 20 }, // successors of 4
				{ 20 }, // successors of 5
		};
		ArcLabelledNodeIterator nit = gU.nodeIterator();
		while ( nit.hasNext() ) {
			int node = nit.nextInt();
			assertEquals( expectedSuccessors[ node ].length, nit.outdegree() );
			LabelledArcIterator ait = nit.successors();
			int d = nit.outdegree();
			int k = 0;
			while ( d-- != 0 ) {
				assertEquals( expectedSuccessors[ node ][ k ], ait.nextInt() );
				assertEquals( expectedLabels[ node ][ k ], ait.label().getInt() );
				k++;
			}
		}
		
		// Same test, but with iterators requested randomly
		for ( int node = gU.numNodes() - 1; node >= 0; node-- ) {
			LabelledArcIterator ait = gU.successors( node );
			assertEquals( expectedSuccessors[ node ].length, gU.outdegree( node ) );
			int k = 0;
			int d = gU.outdegree( node );
			while ( d-- != 0 ) {
				assertEquals( expectedSuccessors[ node ][ k ], ait.nextInt() );
				assertEquals( expectedLabels[ node ][ k ], ait.label().getInt() );
				k++;
			}
		}

		
	}
	

}
