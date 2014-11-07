package it.unimi.dsi.webgraph.examples;

/*		 
 * Copyright (C) 2010-2014 Paolo Boldi and Sebastiano Vigna 
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


import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.WebGraphTestCase;

import org.junit.Test;

public class IntegerTriplesArcLabelledImmutableGraphTest extends WebGraphTestCase {

	@Test
	public void testEmpty() {
		ImmutableGraph g = new IntegerTriplesArcLabelledImmutableGraph( new int[][] {});
		
		assertGraph( g );
	}
	
	@Test
	public void testCycle() {
		ImmutableGraph g = new IntegerTriplesArcLabelledImmutableGraph( new int[][] {
				{ 0, 1, 2 },
				{ 1, 2, 0 },
				{ 2, 0, 1 },
				
		});
		
		assertGraph( g );
	}
	
}
