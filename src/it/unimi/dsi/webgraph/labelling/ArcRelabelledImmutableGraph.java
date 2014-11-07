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

import it.unimi.dsi.lang.ObjectParser;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.AbstractLazyIntIterator;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

/** Exhibits an arc-labelled immutable graph as another arc-labelled immutable graph changing only
 *  the kind of labels. Labels of the source graphs are mapped to labels
 *  of the exhibited graph via a suitable strategy provided at construction time.
 */
public class ArcRelabelledImmutableGraph extends ArcLabelledImmutableGraph {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( ArcRelabelledImmutableGraph.class );
	
	/** A way to convert a label into another label.
	 */
	public static interface LabelConversionStrategy {
		/** Takes a label <code>from</code> and writes its content into another label <code>to</code>.
		 *  If the types of labels are incompatible, or unapt for this strategy, an {@link IllegalArgumentException}
		 *  or a {@link ClassCastException} will be thrown.
		 * 
		 * @param from source label.
		 * @param to target label.
		 * @param source the source node of the arc labelled by the two labels.
		 * @param target the target node of the arc labelled by the two labels.
		 */
		public void convert( Label from, Label to, int source, int target );
	}
	
	/** A conversion strategy that converts between any two classes extending {@link AbstractIntLabel}.
	 */
	public static final LabelConversionStrategy INT_LABEL_CONVERSION_STRATEGY = new LabelConversionStrategy() {
		public void convert( final Label from, final Label to, final int source, final int target ) {
			( (AbstractIntLabel)to ).value = ( (AbstractIntLabel)from ).value;
		}
		
	};
	
	/** The wrapped graph. */
	private final ArcLabelledImmutableGraph wrappedGraph;
	/** The new type of labels. */
	private final Label newLabelPrototype;
	/** The conversion strategy to be used. */
	private final LabelConversionStrategy conversionStrategy;

	/** Creates a relabelled graph with given label prototype.
	 * 
	 * @param wrappedGraph the graph we are going to relabel.
	 * @param newLabelPrototype the prototype for the new type of labels.
	 * @param conversionStrategy the strategy to convert the labels of the wrapped graph into the new labels.
	 */
	public ArcRelabelledImmutableGraph( final ArcLabelledImmutableGraph wrappedGraph, final Label newLabelPrototype, final LabelConversionStrategy conversionStrategy ) {
		this.wrappedGraph = wrappedGraph;
		this.newLabelPrototype = newLabelPrototype;
		this.conversionStrategy = conversionStrategy;
	}
	
	public ArcRelabelledImmutableGraph copy() {
		return new ArcRelabelledImmutableGraph( wrappedGraph.copy(), newLabelPrototype.copy(), conversionStrategy );
	}
	
	private final class RelabelledArcIterator extends AbstractLazyIntIterator implements LabelledArcIterator {
		/** The wrapped arc iterator. */
		private final LabelledArcIterator wrappedArcIterator;
		/** The source node of the current {@link #wrappedArcIterator}. */
		private final int source;
		/** The target of the current arc. */
		private int target;
			
		public RelabelledArcIterator( final LabelledArcIterator wrappedArcIterator, final int source ) {
			this.wrappedArcIterator = wrappedArcIterator;
			this.source = source;
		}

		public Label label() {
			conversionStrategy.convert( wrappedArcIterator.label(), newLabelPrototype, source, target );
			return newLabelPrototype;
		}

		public int nextInt() {
			return target = wrappedArcIterator.nextInt();
		}
	}
	
	@Override
	public ArcLabelledNodeIterator nodeIterator( final int from ) {
		return new ArcLabelledNodeIterator() {
			/** The current node. */
			private int current = -1;
			
			ArcLabelledNodeIterator wrappedNodeIterator = wrappedGraph.nodeIterator( from );
			@Override
			public LabelledArcIterator successors() {
				return new RelabelledArcIterator( wrappedNodeIterator.successors(), current );
			}

			@Override
			public int outdegree() {
				return wrappedNodeIterator.outdegree();
			}

			public boolean hasNext() {
				return wrappedNodeIterator.hasNext();
			}
			
			@Override
			public int nextInt() {
				return current = wrappedNodeIterator.nextInt();
			}
			
		};
	}

	@Override
	public LabelledArcIterator successors( int x ) {
		return new RelabelledArcIterator( wrappedGraph.successors( x ), x );
	}

	@Override
	public Label prototype() {
		return newLabelPrototype;
	}

	@Override
	public int numNodes() {
		return wrappedGraph.numNodes();
	}

	@Override
	public boolean randomAccess() {
		return wrappedGraph.randomAccess();
	}

	@Override
	public int outdegree( int x ) {
		return wrappedGraph.outdegree( x );
	}
	
	public static void main( String arg[] ) throws JSAPException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException {
		final SimpleJSAP jsap = new SimpleJSAP( ArcRelabelledImmutableGraph.class.getName(), 
				"Relabels a graph with given basename, with integer labels, saving it with a different basename and " +
				"using another (typically: different) type of integer labels, specified via a spec, and possibly using " +
				"a different kind of graph class.",
				new Parameter[] {
						new FlaggedOption( "underlyingGraphClass", GraphClassParser.getParser(), BVGraph.class.getName(), JSAP.NOT_REQUIRED, 'u', "underlying-graph-class", "Forces a Java immutable graph class to be used for saving the underlying graph (if the latter did not exist before)." ),
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), BitStreamArcLabelledImmutableGraph.class.getName(), JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java arc-labelled graph class to be used for saving." ),
						new UnflaggedOption( "spec", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The label spec (e.g. FixedWidthIntLabel(FOO,10))." ),
						new UnflaggedOption( "source", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the source arc-labelled graph." ),
						new UnflaggedOption( "target", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the target arc-labelled graph." ),
					}		
				);
		
		final JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) System.exit( 1 );
		final Class<?> destClass = jsapResult.getClass( "graphClass" );
		final Class<?> underlyingDestClass = jsapResult.getClass( "underlyingGraphClass" );
		final String sourceBasename = jsapResult.getString( "source" );
		final String targetBasename = jsapResult.getString( "target" );
		final String spec = jsapResult.getString( "spec" );
		final Label label = ObjectParser.fromSpec( new File( sourceBasename ).getParent(), spec, Label.class );
		
		ImmutableGraph source = ImmutableGraph.loadOffline( sourceBasename );
		if ( ! ( source instanceof ArcLabelledImmutableGraph ) ) throw new IllegalArgumentException( "The graph " + sourceBasename + " of class " + sourceBasename.getClass().getName() + " is not arc-labelled" );
		ArcLabelledImmutableGraph labSource = (ArcLabelledImmutableGraph)source;
		
		if ( ! ( labSource.prototype() instanceof AbstractIntLabel && label instanceof AbstractIntLabel ) ) throw new IllegalArgumentException( "Relabelling from command line is only allowed for int labels, not for " + labSource.prototype().getClass().getName() + " -> " + label.getClass().getName());
		ArcLabelledImmutableGraph labTarget = new ArcRelabelledImmutableGraph( labSource, label, ArcRelabelledImmutableGraph.INT_LABEL_CONVERSION_STRATEGY );

		ProgressLogger pl = new ProgressLogger( LOGGER );

		Properties prop = new Properties();
		prop.load( new FileInputStream( sourceBasename + ImmutableGraph.PROPERTIES_EXTENSION ) );
		String underlyingBasename = prop.getProperty( ArcLabelledImmutableGraph.UNDERLYINGGRAPH_PROPERTY_KEY ); // Tries to get the underlying basename
		if ( underlyingBasename == null ) 
			// If the underlying did not exist, we store it with a fixed basename variant
			underlyingDestClass.getMethod( "store", ImmutableGraph.class, CharSequence.class, ProgressLogger.class )
			.invoke( null, labTarget, underlyingBasename = targetBasename + ArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX, pl ); 		
		
		destClass.getMethod( "store", ArcLabelledImmutableGraph.class, CharSequence.class, CharSequence.class, ProgressLogger.class )
			.invoke( null, labTarget, targetBasename, underlyingBasename, pl ); 
		
	}
	

}
