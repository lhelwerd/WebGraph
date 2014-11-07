package it.unimi.dsi.webgraph.labelling;

/*		 
 * Copyright (C) 2007-2014 Paolo Boldi and Sebastiano Vigna 
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

/** A way to merge two labels into one; the actual merge is performed by the {@link #merge(Label, Label)}
 *  method. Usually, strategies require that the two labels provided are of
 *  the same kind (i.e., instances of the same {@link it.unimi.dsi.webgraph.labelling.Label}
 *  class). Moreover, some strategies only accept label of a certain type,
 *  and throw an {@link java.lang.IllegalArgumentException} if the type
 *  is wrong.  
 * 
 */
public interface LabelMergeStrategy {

	/** Merges two given labels; either label may be <code>null</code>, but not
	 *  both. Implementing classes may decide to throw an {@link IllegalArgumentException}
	 *  if the labels provided are not of the same type, or not of a 
	 *  specific type.
	 * 
	 * @param first the first label to be merged.
	 * @param second the second label to be merged.
	 * @return the resulting label (note that the returned label may be reused by the
	 *  implementing class, so users are invited to make a {@link Label#copy()}
	 *  of it if they need to keep the label in between calls).
	 */
	public Label merge( Label first, Label second );
	
}
