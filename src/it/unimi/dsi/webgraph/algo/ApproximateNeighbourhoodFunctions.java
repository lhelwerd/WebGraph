package it.unimi.dsi.webgraph.algo;

/*		 
 * Copyright (C) 2011-2014 Paolo Boldi and Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.stat.Jackknife;
import it.unimi.dsi.stat.Jackknife.Statistic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

/** Static methods and objects that manipulate approximate neighbourhood functions. 
 *
 * <p>A number of {@linkplain Statistic statistics} that can be used with {@link Jackknife}, such as
 * {@link #CDF}, {@link #AVERAGE_DISTANCE}, {@link #HARMONIC_DIAMETER} and {@link #SPID} are available. 
 */
public class ApproximateNeighbourhoodFunctions {

	private ApproximateNeighbourhoodFunctions() {}
	
	/** Combines several approximate neighbourhood functions for the same
	 * graph by averaging their values.
	 * 
	 * <p>Note that the resulting approximate neighbourhood function has its standard
	 * deviation reduced by the square root of the number of samples (the standard error). However,
	 * if the cumulative distribution function has to be computed instead, calling this method and dividing
	 * all values by the last value is not the best approach, as it leads to a biased estimate. 
	 * Rather, the samples should be combined using the {@linkplain Jackknife jackknife} and
	 * the {@link #CDF} statistic.
	 * 
	 * <p>If you want to obtain estimates on the standard error of each data point, please consider using
	 * the {@linkplain Jackknife jackknife} with the {@linkplain Jackknife#IDENTITY identity} statistic instead of this method.
	 * 
	 * @param anf an iterable object returning arrays of doubles representing approximate neighbourhood functions.
	 * @return a combined approximate neighbourhood functions.
	 */
	public static double[] combine( final Iterable<double[]> anf ) {
		final Object[] t = ObjectIterators.unwrap( anf.iterator() );
		final double a[][] = Arrays.copyOf( t, t.length, double[][].class );
		
		final int n = a.length;
		
		int length = 0;
		for( double[] b : a ) length = Math.max( length, b.length );
		final double[] result = new double[ length ];
		
		BigDecimal last = BigDecimal.ZERO, curr;
		
		for( int i = 0; i < length; i++ ) {
			curr = BigDecimal.ZERO;
			for( int j = 0; j < n; j++ ) curr  = curr.add( BigDecimal.valueOf(  a[ j ][ i < a[ j ].length ? i : a[ j ].length - 1 ] ) );
			if ( curr.compareTo( last ) < 0 ) curr = last;
			result[ i ] = curr.doubleValue() / n;
			last = curr;
		}
		
		return result;
	}
	
	/** Evens out several approximate neighbourhood functions for the same
	 * graph by extending them to the same length (by copying the last value). This is usually a
	 * preparatory step for the {@linkplain Jackknife jackknife}.
	 * 
	 * @param anf an iterable object returning arrays of doubles representing approximate neighbourhood functions.
	 * @return a list containing the same approximate neighbourhood functions, extended to the same length.
	 */
	public static ObjectList<double[]> evenOut( final Iterable<double[]> anf ) {
		final Object[] u = ObjectIterators.unwrap( anf.iterator() );
		final double t[][] = Arrays.copyOf( u, u.length, double[][].class );
		final int n = t.length;
		int max = 0;
		for( double[] a: t ) max = Math.max( max, a.length );

		final ObjectArrayList<double[]> result = new ObjectArrayList<double[]>( n );
		for( int i = 0; i < n; i++ ) {
			final double[] a = new double[ max ];
			System.arraycopy( t[ i ], 0, a, 0, t[ i ].length );
			for( int j = t[ i ].length; j < max; j++ ) a[ j ] = a[ j - 1 ];
			result.add( a );
		}
		
		return result;
	}
	
	/** A statistic that computes the {@linkplain NeighbourhoodFunction#spid(double[]) spid}. */
	public static Jackknife.Statistic SPID = new Jackknife.Statistic() {
		@Override
		public BigDecimal[] compute( final BigDecimal[] sample, final MathContext mc ) {
			BigDecimal sumDistances = BigDecimal.ZERO;
			BigDecimal sumSquareDistances = BigDecimal.ZERO;
			for( int i = sample.length; i-- != 1; ) {
				final BigDecimal delta = sample[ i ].subtract( sample[ i - 1 ] );
				sumDistances = sumDistances.add( delta.multiply( BigDecimal.valueOf( i ) ) );
				sumSquareDistances = sumSquareDistances.add( delta.multiply( BigDecimal.valueOf( (long)i * i ) ) );
			}
			return new BigDecimal[] { sumSquareDistances.divide( sumDistances, mc ).subtract( sumDistances.divide( sample[ sample.length - 1 ], mc ) ) };
		}
	};

	/** A statistic that computes the {@linkplain NeighbourhoodFunction#averageDistance(double[]) average distance}. */
	public static Jackknife.Statistic AVERAGE_DISTANCE = new Jackknife.Statistic() {
		@Override
		public BigDecimal[] compute( final BigDecimal[] sample, final MathContext mc ) {
			BigDecimal mean = BigDecimal.ZERO;
			for( int i = sample.length; i-- != 1; ) mean = mean.add( sample[ i ].subtract( sample[ i - 1 ] ).multiply( BigDecimal.valueOf( i ) ) );
			return new BigDecimal[] { mean.divide( sample[ sample.length - 1 ], mc ) };
		}
	};

	/** A statistic that computes the {@linkplain NeighbourhoodFunction#harmonicDiameter(int, double[]) harmonic diameter}. */
	public static Jackknife.Statistic HARMONIC_DIAMETER = new Jackknife.Statistic() {
		@Override
		public BigDecimal[] compute( final BigDecimal[] sample, final MathContext mc ) {
			BigDecimal sumInverseDistances = BigDecimal.ZERO;
			for( int i = sample.length; i-- != 1; ) sumInverseDistances = sumInverseDistances.add( sample[ i ].subtract( sample[ i - 1 ] ).divide( BigDecimal.valueOf( i ), mc ) );
			return new BigDecimal[] { sample[ 0 ].multiply( sample[ 0 ] ).divide( sumInverseDistances, mc ) };
		}
	};
	
	/** A statistic that computes the {@linkplain NeighbourhoodFunction#effectiveDiameter(double[]) effective diameter}. */
	public static Jackknife.Statistic EFFECTIVE_DIAMETER = new Jackknife.AbstractStatistic() {
		@Override
		public double[] compute( final double[] sample ) {
			return new double[] { NeighbourhoodFunction.effectiveDiameter( sample ) };
		}
	};
	
	/** A statistic that divides all values of a sample (an approximate neighbourhood function)
	 * by the last value. Useful for moving from neighbourhood functions to cumulative distribution functions. */
	public static Jackknife.Statistic CDF = new Jackknife.Statistic() {
		@Override
		public BigDecimal[] compute( final BigDecimal[] sample, final MathContext mc ) {
			final BigDecimal[] result = new BigDecimal[ sample.length ];
			final BigDecimal norm = BigDecimal.ONE.divide( sample[ sample.length - 1 ], mc );
			for( int i = result.length; i-- != 0; ) result[ i ] = sample[ i ].multiply( norm );
			return result;
		}
	};

	/** A statistic that computes differences between consecutive elements of a sample (an approximate neighbourhood function)
	 * and divide them by the last value. Useful for moving from neighbourhood functions or cumulative distribution functions
	 * to probability mass functions. */
	public static Jackknife.Statistic PMF = new Jackknife.Statistic() {
		@Override
		public BigDecimal[] compute( final BigDecimal[] sample, final MathContext mc ) {
			final BigDecimal[] result = new BigDecimal[ sample.length ];
			final BigDecimal norm = BigDecimal.ONE.divide( sample[ sample.length - 1 ], mc );
			result[ 0 ] = sample[ 0 ].multiply( norm );
			for( int i = result.length - 1; i-- != 0; ) result[ i + 1 ] = sample[ i + 1 ].subtract( sample[ i ] ).multiply( norm );
			return result;
		}
	};
}
