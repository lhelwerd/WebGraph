package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2010-2014 Sebastiano Vigna 
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
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.io.FastBufferedReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class BuildHostMapTest extends WebGraphTestCase {

	@Test
	public void testSimple() throws IOException {
		FastBufferedReader fbr = new FastBufferedReader( new StringReader( "http://a/b\nhttp://c\nhttp://a.b/\nhttp://c/c\nhttp://a/" ) );
		FastByteArrayOutputStream fbaos = new FastByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( fbaos );
		BuildHostMap.run( fbr, dos, null );
		dos.flush();
		DataInputStream dis = new DataInputStream( new FastByteArrayInputStream( fbaos.array ) );
		assertEquals( 0, dis.readInt() );
		assertEquals( 1, dis.readInt() );
		assertEquals( 2, dis.readInt() );
		assertEquals( 1, dis.readInt() );
		assertEquals( 0, dis.readInt() );
		dis.close();
	}
	
}
