package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2006-2014 Paolo Boldi and Sebastiano Vigna 
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


/** This interface provides constants to be used as compression flags. */


public interface CompressionFlags {

	/** &delta; coding (see {@link it.unimi.dsi.io.OutputBitStream#writeDelta(int)}). */
	public static final int DELTA = 1;

	/** &gamma; coding (see {@link it.unimi.dsi.io.OutputBitStream#writeGamma(int)}). */
	public static final int GAMMA = 2;

	/** Golomb coding (see {@link it.unimi.dsi.io.OutputBitStream#writeGolomb(int,int)}). */
	public static final int GOLOMB = 3;

	/** Skewed Golomb coding (see {@link it.unimi.dsi.io.OutputBitStream#writeSkewedGolomb(int,int)}). */
	public static final int SKEWED_GOLOMB = 4;

	/** Unary coding (see {@link it.unimi.dsi.io.OutputBitStream#writeUnary(int)}). */
	public static final int UNARY = 5;

	/** &zeta;<sub><var>k</var></sub> coding (see {@link it.unimi.dsi.io.OutputBitStream#writeZeta(int,int)}). */
	public static final int ZETA = 6;

	/** Variable-length nibble coding (see {@link it.unimi.dsi.io.OutputBitStream#writeNibble(int)}). */
	public static final int NIBBLE = 7;

	public static final String[] CODING_NAME = { "DEFAULT", "DELTA", "GAMMA", "GOLOMB", "SKEWED_GOLOMB", "UNARY", "ZETA", "NIBBLE" };

}
