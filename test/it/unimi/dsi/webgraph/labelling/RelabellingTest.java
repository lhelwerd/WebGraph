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
import it.unimi.dsi.webgraph.WebGraphTestCase;
import it.unimi.dsi.webgraph.examples.IntegerTriplesArcLabelledImmutableGraph;

import org.junit.Test;

public class RelabellingTest extends WebGraphTestCase {

	@Test
	public void testIntRelabelling() {
		// Take a graph and convert from gamma to fixed-width
		ArcLabelledImmutableGraph gorig = new IntegerTriplesArcLabelledImmutableGraph( new int[][] 
		     { 
				{ 0, 1, 203 }, { 0, 2, 104 }, { 1, 3, 102 }
		     } );
		ArcLabelledImmutableGraph gfixed = new ArcRelabelledImmutableGraph( gorig, new FixedWidthIntLabel( "FOO", 15 ), ArcRelabelledImmutableGraph.INT_LABEL_CONVERSION_STRATEGY );
		assertGraph( gorig );
		assertGraph( gfixed );
		assertEquals( gorig, gfixed );
		
		// Convert its labels to lists, digitwise; e.g. 203-> [2,0,3]...
		ArcLabelledImmutableGraph glist = new ArcRelabelledImmutableGraph( gorig, new FixedWidthIntListLabel( "FOO", 15 ), new ArcRelabelledImmutableGraph.LabelConversionStrategy() {
			public void convert( Label from, Label to, int source, int target ) {
				String sValue = Integer.toString( ( (AbstractIntLabel)from ).value );
				int[] s = new int[ sValue.length() ];
				for ( int i = 0; i < sValue.length(); i++ ) s[ i ] = sValue.charAt( i ) - '0';
				( (AbstractIntListLabel)to ).value = s;
			}
		});
		// ...and then back to integer, but backwards; e.g. [2,0,3] -> 302...
		ArcLabelledImmutableGraph grevert = new ArcRelabelledImmutableGraph( glist, new FixedWidthIntLabel( "FOO", 15 ), new ArcRelabelledImmutableGraph.LabelConversionStrategy() {
			public void convert( Label from, Label to, int source, int target ) {
				int[] v = ( (AbstractIntListLabel)from ).value;
				int tot = 0;
				for ( int i = v.length - 1; i >= 0; i-- ) 
					tot = tot * 10 + v[ i ];
				( (AbstractIntLabel)to ).value = tot;
			}
		});
		assertGraph( glist );
		assertGraph( grevert );
		// Check the result is correct
		assertEquals( grevert, new IntegerTriplesArcLabelledImmutableGraph( new int[][] 
             {		
				{ 0, 1, 302 }, { 0, 2, 401 }, { 1, 3, 201 }
		     } ));
	}
	

}
