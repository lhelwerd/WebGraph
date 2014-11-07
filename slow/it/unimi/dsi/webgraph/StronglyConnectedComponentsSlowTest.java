package it.unimi.dsi.webgraph;


import static org.junit.Assert.assertEquals;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponentsTarjan;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponentsTest;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;

import org.junit.Test;

public class StronglyConnectedComponentsSlowTest extends WebGraphTestCase {

	@Test
	public void testLarge() throws IOException {
		String path = getGraphPath( "cnr-2000" );
		ImmutableGraph g = ImmutableGraph.load( path ); 
		final StronglyConnectedComponentsTarjan componentsRecursive = StronglyConnectedComponentsTarjan.compute( g, true, new ProgressLogger() );
		final StronglyConnectedComponents componentsIterative = StronglyConnectedComponents.compute( g, true, new ProgressLogger() );
		assertEquals( componentsRecursive.numberOfComponents, componentsIterative.numberOfComponents );
		StronglyConnectedComponentsTest.sameComponents( g.numNodes(), componentsRecursive, componentsIterative );
		deleteGraph( path );
	}

}