package it.unimi.dsi.webgraph.algo;

/*		 
 * Copyright (C) 2010-2014 Paolo Boldi & Sebastiano Vigna 
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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.stat.Jackknife;
import it.unimi.dsi.webgraph.WebGraphTestCase;

import java.util.Arrays;

import org.junit.Test;

public class ApproximateNeighbourhoodFunctionsTest extends WebGraphTestCase {

	@Test
	public void testCombine() {
		final double[] a = { 1.0, 2.0 };
		final double[] b = { 0.5 };
		
		double[] combine = ApproximateNeighbourhoodFunctions.combine( ObjectArrayList.wrap( new double[][] { a, b } ) );
		assertEquals( 2, combine.length );
		assertEquals( 1.5 / 2, combine[ 0 ], 0 );
		assertEquals( 2.5 / 2, combine[ 1 ], 0 );

		final double[] c = { 1, 0.5 };

		combine = ApproximateNeighbourhoodFunctions.combine( ObjectArrayList.wrap( new double[][] { c, c, c } ) );
		assertEquals( 2, combine.length );
		assertEquals( 1, combine[ 0 ], 0 );
		assertEquals( 1, combine[ 1 ], 0 );
	}

	@Test
	public void testEvenOut() {
		final double[] a = { 1, 2 };
		final double[] b = { .5 };
		final double[] c = { .4, .4, .6 };
		
		ObjectList<double[]> evenOut = ApproximateNeighbourhoodFunctions.evenOut( ObjectArrayList.wrap( new double[][] { a, b, c } ) );
		assertArrayEquals( evenOut.get( 0 ), new double[] { 1, 2, 2 }, 0 );
		assertArrayEquals( evenOut.get( 1 ), new double[] { .5, .5, .5 }, 0 );
		assertArrayEquals( evenOut.get( 2 ), new double[] { .4, .4, .6 }, 0 );
	}

	@Test
	public void testStatistics() {
		final double[][] s = { { 1, 2, 3 }, { 1, 2, 3 } };
		
		Jackknife jackknife = Jackknife.compute( Arrays.asList( s ), ApproximateNeighbourhoodFunctions.CDF );
		assertArrayEquals( new double[] { 1./3, 2./3, 1 }, jackknife.estimate, 1E-50 );
		jackknife = Jackknife.compute( Arrays.asList( s ), ApproximateNeighbourhoodFunctions.PMF );
		assertArrayEquals( new double[] { 1./3, 1./3, 1./3 }, jackknife.estimate, 1E-50 );
		jackknife = Jackknife.compute( Arrays.asList( s ), ApproximateNeighbourhoodFunctions.AVERAGE_DISTANCE );
		assertArrayEquals( new double[] { 1 }, jackknife.estimate, 1E-50 );
		jackknife = Jackknife.compute( Arrays.asList( s ), ApproximateNeighbourhoodFunctions.SPID );
		assertArrayEquals( new double[] { 2./3 }, jackknife.estimate, 1E-15 );
	}
}
