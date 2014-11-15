package it.unimi.dsi.webgraph;

/*		 
 * Copyright (C) 2003-2014 Paolo Boldi and Sebastiano Vigna 
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

import it.unimi.dsi.Util;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.io.FastMultiByteArrayInputStream;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.io.ByteBufferInputStream;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.NullOutputStream;
import it.unimi.dsi.io.OutputBitStream;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.lang.ObjectParser;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.sux4j.util.EliasFanoMonotoneLongBigList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.channels.FileChannel.MapMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;


/** An immutable graph represented using the techniques described in 
 * &ldquo;<a href="http://vigna.dsi.unimi.it/papers.php#BoVWFI">The WebGraph Framework I: Compression Techniques</i></a>&rdquo;, by Paolo Boldi and
 * Sebastiano Vigna, in <i>Proc&#46; of the Thirteenth World&ndash;Wide Web
 * Conference</i>, pages 595&minus;601, 2004, ACM Press.
 *
 * <P>This class provides a flexible and configurable way to store and
 * access web graphs in a compressed form.  Its main method can load an
 * {@link ImmutableGraph} and compress it. The resulting compressed {@link
 * BVGraph} is described by a <em>graph file</em> (with extension
 * <samp>.graph</samp>), an <em>offset file</em> (with extension
 * <samp>.offsets</samp>) and a <em>property file</em> (with extension
 * <samp>.properties</samp>). The latter, not surprisingly, is a Java property file.
 * Optionally, an <em>offset big-list file</em> (with extension
 * <samp>.obl</samp>) can be created to load graphs faster.
 *
 * <p>As a rule of thumb, random access is faster using {@link #successors(int)}, whereas
 * while iterating using a {@link NodeIterator} it is better to use {@link NodeIterator#successorArray()}.
 *
 * <h2>The Graph File</h2>
 *
 * <P>This class stores a graph as an <a href="http://dsiutils.dsi.unimi.it/docs/it/unimi/dsi/io/InputBitStream.html">bit stream</a>. The bit stream format
 * depends on a number of parameters and encodings that can be mixed
 * orthogonally. The parameters are:
 * 
 * <ul>
 *
 * <li>the <em>window size</em>, a nonnegative integer;
 * <li>the <em>maximum reference count</em>, a positive integer (it is meaningful only when the window is nonzero);
 * <li>the <em>minimum interval length</em>, an integer larger than or equal to two, or 0, which is interpreted as infinity.
 *
 * </ul>
 *
 * <H3>Successor Lists</H3>
 * 
 * <P>The graph file is a sequence of successor lists, one for each node. 
 * The list of node <var>x</var> can be thought of as a sequence of natural numbers (even though, as we will
 * explain later, this sequence is further coded suitably as a sequence of bits):
 * <OL STYLE="list-style-type: lower-alpha">
 *  <LI>The outdegree of the node; if it is zero, the list ends here.
 *  <LI>If the window size is not zero, the <em>reference part</em>, that is:
 *    <OL><LI>a nonnegative integer, the <em>reference</em>, which never exceeds the window size; if the reference
 *       is <var>r</var>, the list of successors will be specified as a modified version of the list of successors
 *       of <var>x</var>&minus;<var>r</var>; if <var>r</var> is 0, then the list of successors will be specified 
 *       explicitly;
 *      <LI>if <var>r</var> is nonzero:
 *         <OL STYLE="list-style-type: lower-roman">
 *            <LI>a natural number <var>b</var>, the <em>block count</em>;
 *            <LI>a sequence of <var>b</var> natural numbers <var>B</var><sub>1</sub>, &hellip;, <var>B</var><sub>b</sub>, called the <em>copy-block list</em>; only the
 *            first number can be zero.
 *        </OL>
 *
 *    </OL>
 *  <LI>Then comes the <em>extra part</em>, specifying some more entries that the list of successors contains (or all of them, if
 *   <var>r</var> is zero), that is:
 *    <OL>
 *      <LI>If the minimum interval length is finite,
 *       <OL STYLE="list-style-type: lower-roman">
 *          <LI>an integer <var>i</var>, the <em>interval count</em>;
 *          <LI>a sequence of <var>i</var> pairs, whose first component is the left extreme of an interval,
 *              and whose second component is the length of the interval (i.e., the number of integers contained in the interval).
 *       </OL>
 *      <li>Finally, the list of <em>residuals</em>, which contain all successors not specified by previous methods.
 *    </OL>
 *  </OL>
 *       
 *    <P>The above data should be interpreted as follows: 
 *    <ul>
 *      <li>The reference part, if present (i.e., if both the window size and the reference are strictly positive), specifies
 *          that part of the list of successors of node <var>x</var>&minus;<var>r</var> should be copied; the successors of
 *          node <var>x</var>&minus;<var>r</var> that should be copied are described in the copy-block list; more precisely, one should copy
 *          the first <var>B</var><sub>1</sub> entries of this list, discard the next <var>B</var><sub>2</sub>, copy
 *          the next <var>B</var><sub>3</sub> etc. (the last remaining elements of the list of successors will be copied if <var>b</var> is
 *          even, and discarded if <var>b</var> is odd).
 *      <li>The extra part specifies additional successors (or all of them, if the reference part is absent); the extra part is not present
 *          if the number of successors that are to be copied according to the reference part already coincides with the outdegree of <var>x</var>;
 *          the successors listed in the extra part are given in two forms:
 *          <ul>
 *            <li>some of them are specified as belonging to (integer) intervals, if the minimum interval length is finite; 
 *              the interval count indicates how many intervals,
 *              and the intervals themselves are listed as pairs (left extreme, length);
 *            <li>the residuals are the remaining "scattered" successors.
 *          </ul>
 *    </ul>
 *
 * 
 * <H3>How Successor Lists Are Coded</H3>
 *
 * <P>As we said before, the list of integers corresponding to each successor list should be coded into a sequence of bits.
 * This is (ideally) done in two phases: we first modify the sequence in a suitable manner (as explained below) so to obtain
 * another sequence of integers (some of them might be negative). Then each single integer is coded, using a coding that can
 * be specified as an option; the integers that may be negative are first turned into natural numbers using {@link Fast#int2nat(int)}. 
 *
 * <OL>
 *  <LI>The outdegree of the node is left unchanged, as well as the reference and the block count;
 *  <LI>all blocks are decremented by 1, except for the first one;
 *  <LI>the interval count is left unchanged;
 *  <LI>all interval lengths are decremented by the minimum interval length;
 *  <LI>the first left extreme is expressed as its difference from <var>x</var> (it will be negative if the first extreme is
 *     less than <var>x</var>); the remaining left extremes are expressed as their distance from the previous right extreme
 *     plus 2 (e.g., if the interval is [5..11] and the previous one was [1..3], then the left extreme 5 is expressed as
 *     5-(3+2)=5-5=0);
 *  <LI>the first residual is expressed as its difference from <var>x</var> (it will be negative if the first residual is
 *     less than <var>x</var>); the remaining residuals are expressed as decremented differences from the previous residual.
 *  </OL>
 *
 * <H2>The Offset File</H2>
 *
 * <P>Since the graph is stored as a bit stream, we must have some way to know where each successor list starts. 
 * This information is stored in the offset file, which contains the bit offset of each successor list (in particular,
 * the offset of the first successor list will be zero). As a commodity, the offset file contains an additional
 * offset pointing just after the last successor list (providing, as a side-effect, the actual bit length of the graph file).
 * Each offset (except for the first one) is stored as a suitably coded difference from the previous offset.
 *
 * <p>The list of offsets can be additionally stored as a serialised {@link EliasFanoMonotoneLongBigList} 
 * using a suitable command-line option. If the serialised big list is detected, it is loaded instead of parsing the offset list. 
 *
 * <H2>The Property File</H2>
 * 
 * <P>This file contains self-explaining entries that are necessary to correctly decode the graph and offset files, and
 * moreover give some statistical information about the compressed graph (e.g., the number of bits per link).
 * <dl>
 * <dt><samp>nodes
 * <dd>the number of nodes of the graph.
 * <dt><samp>nodes
 * <dd>the number of arcs of the graph.
 * <dt><samp>version
 * <dd>a version number.
 * <dt><samp>graphclass
 * <dd>the name of the class that should load this graph ({@link ImmutableGraph} convention).
 * <dt><samp>bitsperlink
 * <dd>the number of bits per link (overall graph size in bits divided by the number of arcs).
 * <dt><samp>bitspernode
 * <dd>the number of bits per node (overall graph size in bits divided by the number of nodes).
 * <dt><samp>compratio
 * <dd>the ratio between the graph size and the information-theoretical lower bound (the binary logarithm of the number of subsets of size <samp>arcs</samp> out of a universe of <samp>nodes</samp><sup>2</sup> elements).
 * <dt><samp>compressionflags
 * <dd>flags specifying the codes used for the components of the compression algorithm.
 * <dt><samp>zetak
 * <dd>if &zeta; codes are selected for residuals, the parameter <var>k</var>.
 * <dt><samp>windowsize
 * <dd>the window size.
 * <dt><samp>maxref
 * <dd>the maximum reference count.
 * <dt><samp>minintervallength
 * <dd>the minimum length of an interval.
 * <dt><samp>avgdist
 * <dd>the average distance of a reference.
 * <dt><samp>avgref
 * <dd>the average length of reference chains.
 * <dt><samp>bitsfor*
 * <dd>number of bits used by a specific compoenent of the algorithm (the sum is the number of bits used to store the graph).
 * <dt><samp>avgbitsfor*
 * <dd>number of bits used by a specific compoenent of the algorithm, divided by the number of nodes (the sum is the number of bits per node).
 * <dt><samp>*arcs
 * <dd>the number of arcs stored by each component of the algorithm (the sum is the number of arcs).
 * <dt><samp>*expstats
 * <dd>frequencies of the floor of the logarithm of successor gaps and residual gaps, separated by a comma; the statistics include the gap between each node
 * and its first successor, after it has been passed through {@link Fast#int2nat(int)}, but discarding zeroes (which happen in 
 * very rare circumstance, and should be considered immaterial).
 * <dt><samp>*avg[log]gap
 * <dd>the average of the gaps (or of their logarithm) of successors and residuals: note that this data is computed from the exponential statistics above, and
 * thus it is necessarily approximate.
 * <dd>
 * </dl>
 *
 * <H2>How The Graph File Is Loaded Into Memory</H2>
 *
 * <P>The natural way of using a graph file is to load it into a byte array and
 * then index its bits using the suitable offset. This class will use a byte
 * array for graphs smaller than {@link Integer#MAX_VALUE} bytes,
 * and a {@link it.unimi.dsi.fastutil.io.FastMultiByteArrayInputStream}
 * otherwise: in the latter case, expect a significant slowdown (as
 * an {@link it.unimi.dsi.io.InputBitStream} can wrap directly
 * a byte array). 
 * 
 * <P>Offsets are loaded using an {@link EliasFanoMonotoneLongBigList},
 * which occupies exponentially less space than the graph itself (unless
 * your graph is pathologically sparse). There is of course a cost involved in 
 * accessing the list with respect to accessing an array of longs.
 * 
 * <p>Note that by default the {@link EliasFanoMonotoneLongBigList} instance is
 * created from scratch using the file of offsets. This is a long and tedious
 * process, in particular with large graphs. The main method of this class
 * has an option that will generate such a list once for all and serialise it in a file with
 * extension <samp>.obl</samp>. The list will be quickly deserialised
 * if its modification date is later than that of the offset file.
 * 
 * <P>Optionally, this class may load no offsets at all (see {@link BVGraph#loadSequential(CharSequence)}. In this case, the only
 * way to access the graph is by creating a {@link #nodeIterator()}.
 *
 * <H2>Not Loading the Graph File at All</H2>
 * 
 * <P>For some applications (such as transposing a graph) it is not necessary to load the graph
 * file in memory. Since this class is able to enumerate the links of a graph without using random
 * access, it is possible not to load in memory any information at all, and obtain iterators that
 * directly read from the graph file. To obtain this effect, you must call {@link #loadOffline(CharSequence)}.
 *
 * <H2>Memory&ndash;Mapping a Graph</H2>
 * 
 * <p>Another interesting alternative is memory mapping. When using {@link BVGraph#loadMapped(CharSequence)},
 * the graph will be mapped into memory, and the offsets loaded. The graph will provide random access and behave
 * as if it was loaded into memory, but of course the access will be slower.
 */

@SuppressWarnings("resource")
public class BVGraph extends ImmutableGraph implements CompressionFlags {

	private static final Logger LOGGER = LoggerFactory.getLogger( BVGraph.class );
	/** The offset step parameter corresponding to sequential load. */
	public static final int SEQUENTIAL = 0;
	/** The offset step parameter corresponding to offline load. */
	public static final int OFFLINE = -1;
	
	/** The standard extension for the graph bit stream. */
	public static final String GRAPH_EXTENSION = ".graph";
	/** The standard extension for the graph-offsets bit stream. */
	public static final String OFFSETS_EXTENSION = ".offsets";
	/** The standard extension for the cached {@link LongBigList} containing the graph offsets. */
	public static final String OFFSETS_BIG_LIST_EXTENSION = ".obl";
	/** The standard extension for the stream of node outdegrees. */
	public static final String OUTDEGREES_EXTENSION = ".outdegrees";
	/** The buffer size we use for most operations. */
	private static final int STD_BUFFER_SIZE = 1024 * 1024;

	/** This number classifies the present graph format. When new features require introducing binary incompatibilities,
		this number is bumped so to ensure that old classes do not try to read graphs they cannot understand. */
	public final static int BVGRAPH_VERSION = 0;

	/** The initial length of an array that will contain a successor list. */
	protected static final int INITIAL_SUCCESSOR_LIST_LENGTH = 1024;
	
	/** A special value for {@link #minIntervalLength} interpreted as meaning that the minimum interval length is infinity. */
	protected static final int NO_INTERVALS = 0;

	/** The basename of the graph. This may be <code>null</code>, but trying to load the graph with an offset
	 * step of -1 will cause an exception. */
	protected CharSequence basename;
	
	/** The number of nodes of the graph. */
	protected int n;

	/** The number of arcs of the graph. */
	protected long m;

	/** When {@link #offsetType} is not -1, whether this graph is directly loaded into 
	 * {@link #graphMemory}, or rather wrapped in a {@link it.unimi.dsi.fastutil.io.FastMultiByteArrayInputStream}
	 * specified by {@link #graphStream}. */
	protected boolean isMemory;

	/** When {@link #offsetType} is not -1, whether this graph is directly loaded into 
	 * {@link #graphMemory}, or rather memory-mapped. */
	protected boolean isMapped;

	/** The byte array storing the compressed graph, if {@link #isMemory} is true and {@link #offsetType} is not -1.
	 *  
	 * <P>This variable is loaded with a copy of the graph file, or with
	 * a rearrangement of the latter, depending on whether {@link #offsetType} is smaller than or equal to one. If
	 * {@link #offsetType} is -1, this variable is <code>null</code>, and node iterators are generated by opening
	 * streams directly on the graph file. */
	protected byte graphMemory[];

	/** The multi-byte array input stream storing the compressed graph, if {@link #isMemory} is false, {@link #isMapped} is false and {@link #offsetType} is not -1. 
	 * 
	 * <P>It is loaded with a copy of the graph file. If
	 * {@link #offsetType} is -1, this variable is <code>null</code>, and node iterators are generated by opening
	 * streams directly on the graph file. */
	protected FastMultiByteArrayInputStream graphStream;

	/** The memory-mapped input stream storing the compressed graph, if {@link #isMapped} is true. 
	 * 
	 * <P>It is loaded with a copy of the graph file. If
	 * {@link #offsetType} is -1, this variable is <code>null</code>, and node iterators are generated by opening
	 * streams directly on the graph file. */
	protected ByteBufferInputStream mappedGraphStream;

	/** This variable is <code>null</code> iff {@link #offsetType} is zero or less
	 * (implying that offsets have not been loaded).  Otherwise, it is an
	 * Elias&ndash;Fano monotone list containing the pointers of 
	 * the bit streams of one each {@link #offsetType} nodes. */
	protected LongBigList offsets;
	
	/** The offset type: 2 is memory-mapping, 1 is normal random-access loading, 0 means that we do not want to load offsets at all, -1 that
	 * the we do not want even load the graph file. */
	protected int offsetType;

	/** If not -1, the node whose degree is cached in {@link #cachedOutdegree}. */
	protected int cachedNode = Integer.MIN_VALUE;
	/** If {@link #cachedNode} is not {@link Integer#MIN_VALUE}, its cached outdegree. */
	protected int cachedOutdegree;
	/** If {@link #cachedNode} is not {@link Integer#MIN_VALUE}, the position immediately after the coding of the outdegree of {@link #cachedNode}. */
	protected long cachedPointer;

	/** The maximum reference count. */
	protected int maxRefCount = DEFAULT_MAX_REF_COUNT;

	/** Default backward reference maximum length. */
	public final static int DEFAULT_MAX_REF_COUNT = 3;

	/** The window size. Zero means no references. */
	protected int windowSize = DEFAULT_WINDOW_SIZE;

	/** Default window size. */
	public final static int DEFAULT_WINDOW_SIZE = 7;

	/** The minimum interval length. */
	protected int minIntervalLength = DEFAULT_MIN_INTERVAL_LENGTH;

	/** Default minimum interval length. */
	public final static int DEFAULT_MIN_INTERVAL_LENGTH = 4;

	/** The format for residual compression. 0 means none, 1 means gaps. */
	protected int residualCompression = DEFAULT_RESIDUAL_COMPRESSION;

	/** Default residual compression format. */
	public final static int DEFAULT_RESIDUAL_COMPRESSION = 1;

	/** The format for blocks compression. 0 means list, 1 means blocks. */
	protected int blocksCompression = DEFAULT_BLOCKS_COMPRESSION;

	/** Default blocks compression format. */
	public final static int DEFAULT_BLOCKS_COMPRESSION = 1;

	/** The value of <var>k</var> for &zeta;<sub><var>k</var></sub> coding (for residuals). */
	protected int zetaK = DEFAULT_ZETA_K;

	/** Default value of <var>k</var>. */
	public final static int DEFAULT_ZETA_K = 3;

	/** Flag: write outdegrees using &gamma; coding (default). */
	public static final int OUTDEGREES_GAMMA = GAMMA;

	/** Flag: write outdegrees using &delta; coding. */
	public static final int OUTDEGREES_DELTA = DELTA;

	/** Flag: write copy-block lists using &gamma; coding (default). */
	public static final int BLOCKS_GAMMA = GAMMA << 4;

	/** Flag: write copy-block lists using &delta; coding. */
	public static final int BLOCKS_DELTA = DELTA << 4;

	/** Flag: write residuals using &gamma; coding. */
	public static final int RESIDUALS_GAMMA = GAMMA << 8;

	/** Flag: write residuals using &zeta;<sub><var>k</var></sub> coding (default). */
	public static final int RESIDUALS_ZETA = ZETA << 8;

	/** Flag: write residuals using &delta; coding. */
	public static final int RESIDUALS_DELTA = DELTA << 8;

	/** Flag: write residuals using variable-length nibble coding. */
	public static final int RESIDUALS_NIBBLE = NIBBLE << 8;

	/** Flag: write residuals using &golomb; coding. */
	public static final int RESIDUALS_GOLOMB = GOLOMB << 8;

	/** Flag: write references using &gamma; coding. */
	public static final int REFERENCES_GAMMA = GAMMA << 12;

	/** Flag: write references using &delta; coding. */
	public static final int REFERENCES_DELTA = DELTA << 12;

	/** Flag: write references using unary coding (default). */
	public static final int REFERENCES_UNARY = UNARY << 12;

	/** Flag: write block counts using &gamma; coding (default). */
	public static final int BLOCK_COUNT_GAMMA = GAMMA << 16;

	/** Flag: write block counts using &delta; coding. */
	public static final int BLOCK_COUNT_DELTA = DELTA << 16;

	/** Flag: write block counts using unary coding. */
	public static final int BLOCK_COUNT_UNARY = UNARY << 16;

	/** Flag: write offsets using &gamma; coding (default). */
	public static final int OFFSETS_GAMMA = GAMMA << 20;

	/** Flag: write offsets using &delta; coding. */
	public static final int OFFSETS_DELTA = DELTA << 20;

	/** The coding for outdegrees. By default, we use &gamma; coding. */
	protected int outdegreeCoding = GAMMA;

	/** The coding for copy-block lists. By default, we use &gamma; coding. */
	protected int blockCoding = GAMMA;

	/** The coding for residuals. By default, we use &zeta; coding. */
	protected int residualCoding = ZETA;

	/** The coding for references. By default, we use unary coding. */
	protected int referenceCoding = UNARY;

	/** The coding for block counts. By default, we use &gamma; coding. */
	protected int blockCountCoding = GAMMA;

	/** The coding for offsets. By default, we use &gamma; coding. */
	protected int offsetCoding = GAMMA;

	/** The compression flags used. */
	private int flags = 0;

	/** The number of arcs copied during a call to {@link #storeInternal(ImmutableGraph, CharSequence, ProgressLogger)}. */
	private long copiedArcs;
	
	/** The number of arcs that have been intervalised during a call to {@link #storeInternal(ImmutableGraph, CharSequence, ProgressLogger)}. */
	private long intervalisedArcs;
	
	/** The number of arcs that are represented explicitly. */
	private long residualArcs;
	
	private final static boolean STATS = false;
	@SuppressWarnings("unused")
	private final static boolean DEBUG = false;
	private final static boolean ASSERTS = false;

	private PrintWriter offsetStats, outdegreeStats, blockCountStats, blockStats, intervalCountStats, referenceStats, leftStats, lenStats, residualStats, residualCountStats;

	public BVGraph copy() {
		final BVGraph result = new BVGraph();
		result.basename = basename;
		result.n = n;
		result.m = m;
		result.isMemory = isMemory;
		result.isMapped = isMapped;
		result.graphMemory = graphMemory;
		result.graphStream = graphStream != null ? new FastMultiByteArrayInputStream( graphStream ) : null;
		result.mappedGraphStream = mappedGraphStream != null ? mappedGraphStream.copy() : null;
		result.offsets = offsets;
		result.maxRefCount = maxRefCount;
		result.windowSize = windowSize;
		result.minIntervalLength = minIntervalLength;
		result.offsetType = offsetType;
		result.residualCompression = residualCompression;
		result.blocksCompression = blocksCompression;
		result.zetaK = zetaK;
		result.outdegreeCoding = outdegreeCoding;
		result.blockCoding = blockCoding;
		result.residualCoding = residualCoding;
		result.referenceCoding = referenceCoding;
		result.blockCountCoding = blockCountCoding;
		result.offsetCoding = offsetCoding;
		result.flags = flags;
		result.outdegreeIbs = offsetType <= 0 ? null : isMemory ? new InputBitStream( graphMemory ): new InputBitStream( isMapped ? mappedGraphStream.copy() : new FastMultiByteArrayInputStream( graphStream ), 0 );
		return result;
	}

	protected BVGraph() {}

	public int numNodes() {
		return n;
	}

	public long numArcs() {
		return m;
	}
	
	@Override
	public boolean randomAccess() {
		return offsets != null;
	}

	public CharSequence basename() {
		return basename;
	}

	/** Returns the maximum reference count of this graph. 
	 *
	 * @return the maximum reference count.
	 */
	public int maxRefCount() {
		return maxRefCount;
	}

	/** Returns the window size of this graph. 
	 *
	 * @return the window size.
	 */
	public int windowSize() {
		return windowSize;
	}

	/** Returns the residual compression format of this graph. 
	 *
	 * @return the residual compression.
	 */
	public int residualCompression() {
		return residualCompression;
	}

	/** Returns the blocks compression format of this graph. 
	 *
	 * @return the blocks compression.
	 */
	public int blocksCompression() {
		return blocksCompression;
	}

	/* This family of protected methods is used throughout the class to read data
	from the graph file following the codings indicated by the compression
	flags. */

	/** Reads an offset difference from the given stream. 
	 *
	 * @param ibs an offset-file input bit stream.
	 * @return the next offset difference.
	 */
	protected final long readOffset( final InputBitStream ibs ) throws IOException {
		switch( offsetCoding ) {
		case GAMMA: return ibs.readLongGamma(); 
		case DELTA: return ibs.readLongDelta(); 
		default: throw new UnsupportedOperationException( "The required offset coding (" + offsetCoding + ") is not supported." );
		}
	}

	/** Writes an offset difference to the given stream. 
	 *
	 * @param obs an offset-file output bit stream.
	 * @param x an offset difference to be stored in the stream.
	 * @return the number of bits written.
	 */
	protected final int writeOffset( final OutputBitStream obs, final long x ) throws IOException {
		switch( offsetCoding ) {
		case GAMMA: return obs.writeLongGamma( x ); 
		case DELTA: return obs.writeLongDelta( x ); 
		default: throw new UnsupportedOperationException( "The required offset coding (" + offsetCoding + ") is not supported." );
		}
	}

	/** Reads an outdegree from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next outdegree.
	 */
	protected final int readOutdegree( final InputBitStream ibs ) throws IOException {
		switch( outdegreeCoding ) {
		case GAMMA: return ibs.readGamma(); 
		case DELTA: return ibs.readDelta(); 
		default: throw new UnsupportedOperationException( "The required outdegree coding (" + outdegreeCoding + ") is not supported." );
		}
	}

	/** Reads an outdegree from the given stream at a given offset. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @param offset the offset at which the stream must be positioned.
	 * @return the next outdegree.
	 */
	protected final int readOutdegree( final InputBitStream ibs, final long offset ) throws IOException {
		ibs.position( offset );
		return readOutdegree( ibs );
	}

	/** Writes an outdegree to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param d an outdegree to be stored in the stream.
	 * @return the number of bits written.
	 */
	protected final int writeOutdegree( final OutputBitStream obs, final int d ) throws IOException {
		switch( outdegreeCoding ) {
		case GAMMA: return obs.writeGamma( d ); 
		case DELTA: return obs.writeDelta( d ); 
		default: throw new UnsupportedOperationException( "The required outdegree coding (" + outdegreeCoding + ") is not supported." );
		}
	}

	/** Reads a reference from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next reference.
	 */
	protected final int readReference( final InputBitStream ibs ) throws IOException {
		final int ref;

		switch( referenceCoding ) {
		case UNARY: ref = ibs.readUnary(); break;
		case GAMMA: ref = ibs.readGamma(); break;
		case DELTA: ref = ibs.readDelta(); break;
		default: throw new UnsupportedOperationException( "The required reference coding (" + referenceCoding + ") is not supported." );
		}
		if ( ref > windowSize ) throw new IllegalStateException( "The required reference (" + ref + ") is incompatible with the window size (" + windowSize + ")" );
		return ref;
	}
	
	/** Writes a reference to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param ref the reference.
	 * @return the number of bits written.
	 */
	protected final int writeReference( final OutputBitStream obs, final int ref ) throws IOException {

		if ( ref > windowSize ) throw new IllegalStateException( "The required reference (" + ref + ") is incompatible with the window size (" + windowSize + ")" );
		switch( referenceCoding ) {
		case UNARY: return obs.writeUnary( ref );
		case GAMMA: return obs.writeGamma( ref );
		case DELTA: return obs.writeDelta( ref );
		default: throw new UnsupportedOperationException( "The required reference coding (" + referenceCoding + ") is not supported." );
		}
	}


	/** Reads a block count from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next block count.
	 */
	protected final int readBlockCount( final InputBitStream ibs ) throws IOException {
		switch( blockCountCoding ) {
		case UNARY: return ibs.readUnary();
		case GAMMA: return ibs.readGamma();
		case DELTA: return ibs.readDelta();
		default: throw new UnsupportedOperationException( "The required block count coding (" + blockCountCoding + ") is not supported." );
		}
	}

	/** Writes a block count to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param count the block count.
	 * @return the number of written bits.
	 */
	protected final int writeBlockCount( final OutputBitStream obs, final int count ) throws IOException {
		switch( blockCountCoding ) {
		case UNARY: return obs.writeUnary( count );
		case GAMMA: return obs.writeGamma( count );
		case DELTA: return obs.writeDelta( count );
		default: throw new UnsupportedOperationException( "The required block count coding (" + blockCountCoding + ") is not supported." );
		}
	}


	/** Reads a block from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next block.
	 */
	protected final int readBlock( final InputBitStream ibs ) throws IOException {
		switch( blockCoding ) {
		case UNARY: return ibs.readUnary();
		case GAMMA: return ibs.readGamma();
		case DELTA: return ibs.readDelta();
		default: throw new UnsupportedOperationException( "The required block coding (" + blockCoding + ") is not supported." );
		}
	}

	/** Writes a block to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param block the block.
	 * @return the number of written bits.
	 */
	protected final int writeBlock( final OutputBitStream obs, final int block ) throws IOException {
		switch( blockCoding ) {
		case UNARY: return obs.writeUnary( block );
		case GAMMA: return obs.writeGamma( block );
		case DELTA: return obs.writeDelta( block );
		default: throw new UnsupportedOperationException( "The required block coding (" + blockCoding + ") is not supported." );
		}
	}
	
	/** Reads a residual from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next residual.
	 */
	protected final int readResidual( final InputBitStream ibs ) throws IOException {
		switch( residualCoding ) {
		case GAMMA: return ibs.readGamma();
		case ZETA: return ibs.readZeta( zetaK );
		case DELTA: return ibs.readDelta();
		case GOLOMB: return ibs.readGolomb( zetaK );
		case NIBBLE: return ibs.readNibble();
		default: throw new UnsupportedOperationException( "The required residuals coding (" + residualCoding + ") is not supported." );
		}
	}

	/** Reads a long residual from the given stream. 
	 *
	 * @param ibs a graph-file input bit stream.
	 * @return the next residual.
	 */
	protected final long readLongResidual( final InputBitStream ibs ) throws IOException {
		switch( residualCoding ) {
		case GAMMA: return ibs.readLongGamma();
		case ZETA: return ibs.readLongZeta( zetaK );
		case DELTA: return ibs.readLongDelta();
		case GOLOMB: return ibs.readLongGolomb( zetaK );
		case NIBBLE: return ibs.readLongNibble();
		default: throw new UnsupportedOperationException( "The required residuals coding (" + residualCoding + ") is not supported." );
		}
	}

	/** Writes a residual to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param residual the residual.
	 * @return the number of written bits.
	 */
	protected final int writeResidual( final OutputBitStream obs, final int residual ) throws IOException {
		switch( residualCoding ) {
		case GAMMA: return obs.writeGamma( residual );
		case ZETA: return obs.writeZeta( residual, zetaK );
		case DELTA: return obs.writeDelta( residual );
		case GOLOMB: return obs.writeGolomb( residual, zetaK );
		case NIBBLE: return obs.writeNibble( residual );
		default: throw new UnsupportedOperationException( "The required residuals coding (" + residualCoding + ") is not supported." );
		}
	}

	/** Writes a residual to the given stream. 
	 *
	 * @param obs a graph-file output bit stream.
	 * @param residual the residual.
	 * @return the number of written bits.
	 */
	protected final int writeResidual( final OutputBitStream obs, final long residual ) throws IOException {
		switch( residualCoding ) {
		case GAMMA: return obs.writeLongGamma( residual );
		case ZETA: return obs.writeLongZeta( residual, zetaK );
		case DELTA: return obs.writeLongDelta( residual );
		case GOLOMB: return (int)obs.writeLongGolomb( residual, zetaK );
		case NIBBLE: return obs.writeLongNibble( residual );
		default: throw new UnsupportedOperationException( "The required residuals coding (" + residualCoding + ") is not supported." );
		}
	}

	/** A bit stream wrapping {@link #graphMemory}, or {@link #graphStream}, used <em>only</em> by {@link #outdegree(int)} and {@link #outdegreeInternal(int)}. */
	private InputBitStream outdegreeIbs;

	/* The code of the following two methods must be kept in sync. */

	public int outdegree( final int x ) throws IllegalStateException {
		if ( x == cachedNode ) return cachedOutdegree; 
		if ( x < 0 || x >= n ) throw new IllegalArgumentException( "Node index out of range: " + x );

		/* Computing the outdegree is a most basic operation. Thus, it must be always
		   possible to compute the outdegree of a node independently of any other state
		   in a BVGraph. To this purpose, we have special-purpose input bit stream that
		   is used just to read outdegrees. */

		try {
			// Without offsets, we just give up.
			if ( offsetType <= 0 ) throw new IllegalStateException( "You cannot compute the outdegree of a random node without offsets" );
			// We just position and read.
			outdegreeIbs.position( offsets.getLong( cachedNode = x ) );
			cachedOutdegree = readOutdegree( outdegreeIbs );
			cachedPointer = outdegreeIbs.position();
			return cachedOutdegree;
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	private int outdegreeInternal( final int x ) throws IOException {
		if ( x == cachedNode ) return cachedOutdegree; 
		// We just position and read.
		outdegreeIbs.position( offsets.getLong( cachedNode = x ) );
		cachedOutdegree = readOutdegree( outdegreeIbs );
		cachedPointer = outdegreeIbs.position();
		return cachedOutdegree;
	}


	/** Returns an iterator over the successors of a given node.
	 * 
	 * @param x a node.
	 * @return an iterator over the successors of the node.
	 */
	public LazyIntIterator successors( final int x ) {
		// We just call successors(int, InputBitStream, int[][], int[], int[]) with
		// a newly created input bit stream and null elsewhere.
		if ( x < 0 || x >= n ) throw new IllegalArgumentException( "Node index out of range: " + x );
		if ( offsetType <= 0 ) throw new UnsupportedOperationException( "Random access to successor lists is not possible with sequential or offline graphs" );
		final InputBitStream ibs = isMemory ? new InputBitStream( graphMemory ) : new InputBitStream( isMapped ? mappedGraphStream.copy() : new FastMultiByteArrayInputStream( graphStream ), 0 );
		return successors( x, ibs, null, null );
	}


	/** An iterator returning the offsets. */
	private final static class OffsetsLongIterator extends AbstractLongIterator {
		private final InputBitStream offsetIbs;
		private final int n;
		private long off;
		private int i;
		private BVGraph g;

		private OffsetsLongIterator( final BVGraph g, final InputBitStream offsetIbs ) {
			this.offsetIbs = offsetIbs;
			this.g = g;
			this.n = g.numNodes();
		}

		public boolean hasNext() {
			return i <= n;
		}

		@Override
		public long nextLong() {
			i++;
			try {
				return off = g.readOffset( offsetIbs ) + off;
			}
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}
	}


	/** An iterator returning the residuals of a node. */
	private final static class ResidualIntIterator extends AbstractLazyIntIterator {
		/** The graph associated to this iterator. */
		private final BVGraph g;
		/** The input bit stream from which residuals will be read. */
		private final InputBitStream ibs;
		/** The last residual returned. */
		private int next;
		/** The number of remaining residuals. */
		private int remaining;
		/** Residual compression format. */
		private int residualCompression;
		
		private ResidualIntIterator( final BVGraph g, final InputBitStream ibs, final int residualCount, final int x ) {
			this.g = g;
			this.remaining = residualCount;
			this.residualCompression = g.residualCompression();
			this.ibs = ibs;
			try {
				if ( residualCompression == 1 ) {
					this.next = (int)( x  + Fast.nat2int( g.readLongResidual( ibs ) ) );
				}
				else {
					this.next = (int)g.readLongResidual( ibs );
				}
			}
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}

		private void readNext() throws IOException {
			if ( residualCompression == 1 ) {
				next += g.readResidual( ibs ) + 1;
			}
			else {
				next = g.readResidual( ibs );
			}
		}

		public int nextInt() {
			if ( remaining == 0 ) return -1;
			try {
				final int result = next;
				if ( --remaining != 0 ) readNext();
				return result;
			} 
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}

		@Override
		public int skip( int n ) {
			if ( n >= remaining ) {
				n = remaining;
				remaining = 0;
				return n;
			}
			try {
				for ( int i = n; i-- != 0; ) readNext();
				remaining -= n;
				return n;
			}
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}

	}



	/** Given an {@link InputBitStream} wrapping a graph file, returns an iterator over the
	 * successors of a given node <code>x</code>.
	 * 
	 * <P>This method can be used in two different ways:
	 * <OL><LI>by providing a node and an input bit stream wrapping a graph file, it is possible
	 * to access the successor list of the node (provided that offsets have been loaded);
	 * <LI>by providing additional data, which essentially are used to keep some state
	 * about the graph, it is possible to perform an efficient sequential visit of all
	 * successor lists (even when no offsets were loaded).
	 * </OL>
	 * 
	 * <P>This method may modify the offset and the outdegree caches if <code>window</code> is <code>null</code>.
	 * 
	 * @param x a node.
	 * @param ibs an input bit stream wrapping a graph file. After this method returns, the state of <code>ibs</code> is undefined:
	 *   however, after the iterator returned is exhausted, <code>ibs</code> will positioned just after the successor list of <code>x</code>.
	 * @param window either <code>null</code>, or a double array with the following meaning: <code>window[(x-i) mod windowSize]</code> 
	 *   contains, for all <code>i</code> between 1 (inclusive) and {@link #windowSize} (exclusive), the list of successors
	 *   of node <code>x</code>&minus;<code>i</code>. If <code>window</code> is not <code>null</code> then <code>ibs</code>
	 *   must be positioned before the successor list of <code>x</code>. This parameter will not be modified. 
	 * @param outd if <code>window</code> is not <code>null</code>, this is an array with as many elements
	 *   as {@link #windowSize}; <code>outd[(x-i) mod windowSize]</code> contains the outdegree of node <code>x</code>
	 *   &minus;<code>i</code> for <code>i</code> greater than 0; at the end, this will be true also for <code>i</code> equal to 0.
	 * @return an iterator over the successors of <code>x</code>.
	 * @throws IllegalStateException if <code>window</code> is <code>null</code> and {@link #offsetType} is 0.
	 * 	 
	 */
	protected LazyIntIterator successors( final int x, final InputBitStream ibs, final int window[][], final int outd[] ) throws IllegalStateException {
		final int ref, refIndex;
		int i, extraCount, blockCount = 0;
		int[] block = null, left = null, len = null;
		int[] flagBlocks = null;

		if ( x < 0 || x >= n ) throw new IllegalArgumentException( "Node index out of range:" + x );

		try {
			final int d;
			final int cyclicBufferSize = windowSize + 1;
			//long nextOffset = -1;
			
			if ( window == null ) {
				d = outdegreeInternal( x );
				ibs.position( cachedPointer );
			}
			else d = outd[ x % cyclicBufferSize ] = readOutdegree( ibs );
			
			if ( d == 0 ) return LazyIntIterators.EMPTY_ITERATOR;
			
			// We read the reference only if the actual window size is larger than one (i.e., the one specified by the user is larger than 0).
			if ( windowSize > 0 ) ref = readReference( ibs );
			else ref = -1;

			refIndex = ( x - ref + cyclicBufferSize ) % cyclicBufferSize; // The index in window[] of the node we are referring to (it makes sense only if ref>0).

			if ( ref > 0 ) { // This catches both no references at all and no reference specifically for this node.
				if ( blocksCompression == 1 ) {
					if ( ( blockCount = readBlockCount( ibs ) ) !=  0 ) block = new int[ blockCount ];

					int copied = 0, total = 0; // The number of successors copied, and the total number of successors specified in some copy block.
					for( i = 0; i < blockCount; i++ ) {
						block[ i ] = readBlock( ibs ) + ( i == 0 ? 0 : 1 );
						total += block[ i ];
						if ( i % 2 == 0 ) copied += block[ i ];
					}
					// If the block count is even, we must compute the number of successors copied implicitly.
					//if ( window == null ) nextOffset = offsets.getLong( x - ref );
					if ( blockCount % 2 == 0 ) copied += ( window != null ? outd[ refIndex ] : outdegreeInternal( x - ref ) ) - total;
					extraCount = d - copied;
				}
				else if ( blocksCompression == 2 ) {
					// Copy flags compression reading
					i = 0;
					int count = 0;
					int copyListLength = ( window != null ? outd[ refIndex ] : outdegreeInternal( x - ref ) );
					ArrayList<Integer> blockList = new ArrayList<Integer>();
					ArrayList<Integer> flagList = new ArrayList<Integer>();
					// The number of successors copied.
					int copied = 0;
					int flag = 0;
					int lastFlag = 0;
					for ( int c = 0; c < copyListLength; c++ ) {
						flag = ibs.readInt(2);
						if ( lastFlag != flag && count > 0 ) {
							blockList.add( count );
							flagList.add( lastFlag );
							if ( lastFlag != 1 ) {
								copied += count;
							}
							count = 0;
							i++;
						}
						lastFlag = flag;
						count++;
					}
					if ( count > 0 ) {
						blockList.add( count );
						flagList.add( flag );
						if ( flag != 1 ) {
							copied += count;
						}
					}
					blockCount = blockList.size();
					extraCount = d - copied;
					block = new int[ blockCount ];
					flagBlocks = new int[ blockCount ];
					for ( i = 0; i < blockCount; i++ ) {
						block[i] = blockList.get(i).intValue();
						flagBlocks[i] = flagList.get(i).intValue();
					}
				}
				else {
					// Copy list compression reading
					i = 0;
					boolean isOne = true;
					int count = 0;
					int copyListLength = ( window != null ? outd[ refIndex ] : outdegreeInternal( x - ref ) );
					ArrayList<Integer> blockList = new ArrayList<Integer>();
					// The number of successors copied.
					int copied = 0;
					int bit = 0;
					for ( int c = 0; c < copyListLength; c++ ) {
						bit = ibs.readBit();
						if ( i == 0 && count == 0 && bit == 0 && isOne ) {
							// case: bit is zero at start
							blockList.add( 0 );
							i++;
							isOne = false;
						}
						else if ( (isOne && bit == 0) || (!isOne && bit == 1) ) {
							// case: end of "block"
							isOne = !isOne;
							blockList.add( count );
							if ( i % 2 == 0 ) {
								copied += count;
							}
							count = 0;
							i++;
						}
						count++;
					}
					blockCount = blockList.size();
					if ( blockCount % 2 == 0 ) {
						copied += count;
					}
					extraCount = d - copied;
					block = new int[ blockCount ];
					for ( i = 0; i < blockCount; i++ ) {
						block[i] = blockList.get(i).intValue();
					}
				}
			}
			else extraCount = d;
			
			int intervalCount = 0; // Number of intervals

			if ( extraCount > 0 ) {

				// Prepare to read intervals, if any
				if ( minIntervalLength != NO_INTERVALS && ( intervalCount = ibs.readGamma() ) != 0 ) {

					int prev = 0; // Holds the last integer in the last interval.
					left = new int[ intervalCount ];
					len = new int[ intervalCount ];

					// Now we read intervals
					left[ 0 ] = prev = (int)( Fast.nat2int( ibs.readLongGamma() ) + x );
					len[ 0 ] = ibs.readGamma() + minIntervalLength;

					prev += len[ 0 ];
					extraCount -= len[ 0 ];

					for ( i = 1; i < intervalCount; i++ ) {
						left[ i ] = prev = ibs.readGamma() + prev + 1;
						len[ i ] = ibs.readGamma() + minIntervalLength;
						prev += len[ i ];
						extraCount -= len[ i ];
					}
				}
			}
				
			final int residualCount = extraCount; // Just to be able to use an anonymous class.

			final LazyIntIterator residualIterator = residualCount == 0 ? null : new ResidualIntIterator( this, ibs, residualCount, x );
			
			// The extra part is made by the contribution of intervals, if any, and by the residuals iterator.
			final LazyIntIterator extraIterator = intervalCount == 0 
				? residualIterator 
				: ( residualCount == 0 
					? (LazyIntIterator)new IntIntervalSequenceIterator( left, len )
					: (LazyIntIterator)new MergedIntIterator( new IntIntervalSequenceIterator( left, len ), residualIterator )
					);

			final LazyIntIterator refIterator = ref <= 0
				? null
				: (window != null 
				? LazyIntIterators.wrap( window[ refIndex ], outd[ refIndex ] )
				: 
				// This is the recursive lazy part of the construction.
				successors( x - ref, isMemory ? new InputBitStream( graphMemory ) : new InputBitStream( isMapped ? mappedGraphStream.copy() : new FastMultiByteArrayInputStream( graphStream ), 0 ), null, null )
			);
				
			final LazyIntIterator blockIterator = ref <= 0
				? null 
				: (flagBlocks == null
				? new MaskedIntIterator(
										// ...block for masking copy and...
										block, 
										// ...the reference list (either computed recursively or stored in window)...
										refIterator
										)
				: new FlaggedIntIterator( block, flagBlocks, refIterator )
			);
			
			if ( ref <= 0 ) return extraIterator;
			else return extraIterator == null
					 ? blockIterator
					 : (LazyIntIterator)new MergedIntIterator( blockIterator, extraIterator );
			
		}
		catch ( IOException e ) {
			LOGGER.error( "Exception while accessing node " + x, e );
			throw new RuntimeException( e );
		}
	}

	
	private class BVGraphNodeIterator extends NodeIterator {
		@SuppressWarnings("hiding")
		final private int n = numNodes();
		/** Our bit stream. */
		final InputBitStream ibs;
		/** We keep the size of the cyclic buffer (the window size + 1 ) in a local variable. */
		final private int cyclicBufferSize = windowSize + 1;
		/** At any time, window will be ready to be passed to {@link BVGraph#successors(int, InputBitStream, int[][], int[], int[])} */ 
		final private int window[][] = new int[ cyclicBufferSize ][ INITIAL_SUCCESSOR_LIST_LENGTH ];
		/** At any time, outd will be ready to be passed to {@link BVGraph#successors(int, InputBitStream, int[][], int[], int[])} */ 
		final private int outd[] = new int[ cyclicBufferSize ];
		/** The index of the node from which we started iterating. */
		final private int from;
		/** The index of the node just before the next one. */
		private int curr;

		public BVGraphNodeIterator( final InputBitStream ibs, final int from ) throws IOException {
			if ( from < 0 || from > n ) throw new IllegalArgumentException( "Node index out of range: " + from );
			this.from = from;
			this.ibs = ibs;
			if ( from != 0 ) {
				if ( offsetType <= 0 ) throw new IllegalStateException( "You cannot iterate from a chosen node without offsets" );
				
				int pos;
				for( int i = 1; i < Math.min( from + 1, cyclicBufferSize ); i++ ) {
					pos = ( from - i + cyclicBufferSize ) % cyclicBufferSize;
					outd[ pos ] = BVGraph.this.outdegreeInternal( from - i );
					System.arraycopy( BVGraph.this.successorArray( from - i ), 0, window[ pos ] = IntArrays.grow( window[ pos ], outd[ pos ], 0 ), 0, outd[ pos ] );
				}
				ibs.position( offsets.getLong( from ) ); // We must fix the bit stream position so that we are *before* the outdegree.
			}
			curr = from - 1;
		}

		/** At each call, we build the successor iterator (making a call to {@link BVGraph#successors(int, InputBitStream, int[][], int[])},
		 *  and we completely iterate over it, filling the appropriate entry in <code>window</code>. */
		public int nextInt() {
			if ( ! hasNext() ) throw new NoSuchElementException();

			final int currIndex = ++curr % cyclicBufferSize;
			final LazyIntIterator i = BVGraph.this.successors( curr, ibs, window, outd );

			final int d = outd[ currIndex ];
			if ( window[ currIndex ].length < d ) window[ currIndex ] = new int[ d ];
			final int[] w = window[ currIndex ];
			for( int j = 0; j < d; j++ ) w[ j ] = i.nextInt();

			return curr;
		}

		public boolean hasNext() {
			return ( curr < n - 1 );
		}

		public LazyIntIterator successors() {
			if ( curr == from - 1 ) throw new IllegalStateException();

			final int currIndex = curr % cyclicBufferSize;
			return LazyIntIterators.wrap( window[ currIndex ], outd[ currIndex ] );
		}

		public int[] successorArray() {
			if ( curr == from - 1 ) throw new IllegalStateException();	

			return window[ curr % cyclicBufferSize ];
		}

		public int outdegree() {
			if ( curr == from - 1 ) throw new IllegalStateException();
			return outd[ curr % cyclicBufferSize ];
		}

		protected void finalize() throws Throwable {
			try {
				ibs.close();
			}
			finally {
				super.finalize();
			}
		}
	};


	/** This method returns a node iterator for scanning the graph sequentially, starting from the given node.
	 *  It keeps track of a sliding window of {@link #windowSize()} previous successor lists
	 *  to speed up the iteration of graphs with significant referentiation.
	 * 
	 *  @param from the node from which the iterator will iterate.
	 *  @return a {@link NodeIterator} for accessing nodes and successors sequentially.
	 */

	public NodeIterator nodeIterator( final int from ) {
		try {
			return offsetType == -1 
				? new BVGraphNodeIterator( new InputBitStream( new FileInputStream( basename + GRAPH_EXTENSION ), STD_BUFFER_SIZE ), from )
				: new BVGraphNodeIterator( isMemory ? new InputBitStream( graphMemory ) : new InputBitStream( isMapped ? mappedGraphStream.copy() : new FastMultiByteArrayInputStream( graphStream ), 0 ), from );
		} catch ( FileNotFoundException e ) {
			throw new IllegalStateException( "The graph file \"" + basename + GRAPH_EXTENSION + "\" cannot be found" );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	

	/* The following private methods handle the flag mask. They are the only methods which replicate
	 * the shifting logic specified in the flag-mask definition.
	 */

	/** Sets the {@link #flags} attribute to the given value, and updates appropriately the 
	 *  individual coding attributes (<code>g&hellip;Coding</code>).
	 *  
	 *  <P>If a certain bit-slot within <code>flags</code> is not specified (i.e., 0) the corresponding 
	 *  coding variable is left unchanged, making the assumption that it is the default value (this condition
	 *  is anyway not checked for).
	 * 	  
	 * @param flags a mask of flags as specified by the constants of this class.
	 */
	private void setFlags( final int flags ) {
		this.flags = flags;
		if ( ( flags & 0xF ) != 0 ) outdegreeCoding = flags & 0xF;
		if ( ( ( flags >>> 4 ) & 0xF ) != 0 ) blockCoding = ( flags >>> 4 ) & 0xF;
		if ( ( ( flags >>> 8 ) & 0xF ) != 0 ) residualCoding = ( flags >>> 8 ) & 0xF;
		if ( ( ( flags >>> 12 ) & 0xF ) != 0 ) referenceCoding = ( flags >>> 12 ) & 0xF;
		if ( ( ( flags >>> 16 ) & 0xF ) != 0 ) blockCountCoding = ( flags >>> 16 ) & 0xF;
		if ( ( ( flags >>> 20 ) & 0xF ) != 0 ) offsetCoding = ( flags >>> 20 ) & 0xF;
	}

	/** Produces a string representing the values coded in the given flag mask.
	 * 
	 * @param flags a flag mask.
	 * @return a string representing the flag mask.
	 */
	private static MutableString flags2String( final int flags ) {
		MutableString s = new MutableString();

		if ( ( flags & 0xF ) != 0 ) s.append( " | " ).append( "OUTDEGREES_" ).append( CompressionFlags.CODING_NAME[ flags & 0xF ] );
		if ( ( ( flags >>> 4 ) & 0xF ) != 0 ) s.append( " | " ).append( "BLOCKS_" ).append( CompressionFlags.CODING_NAME[ ( flags >>> 4 ) & 0xF ] );
		if ( ( ( flags >>> 8 ) & 0xF ) != 0 ) s.append( " | " ).append( "RESIDUALS_" ).append( CompressionFlags.CODING_NAME[ ( flags >>> 8 ) & 0xF ] );
		if ( ( ( flags >>> 12 ) & 0xF ) != 0 ) s.append( " | " ).append( "REFERENCES_" ).append( CompressionFlags.CODING_NAME[ ( flags >>> 12 ) & 0xF ] );
		if ( ( ( flags >>> 16 ) & 0xF ) != 0 ) s.append( " | " ).append( "BLOCK_COUNT_" ).append( CompressionFlags.CODING_NAME[ ( flags >>> 16 ) & 0xF ] );
		if ( ( ( flags >>> 20 ) & 0xF ) != 0 ) s.append( " | " ).append( "OFFSETS_" ).append( CompressionFlags.CODING_NAME[ ( flags >>> 20 ) & 0xF ] );

		if ( s.length() != 0 ) s.delete( 0, 3 );
		return s;
	}

	/** Produces a flag mask corresponding to a given string.
	 * 
	 * @param flagString a flag string.
	 * @return the flag mask.
	 * @throws IOException if the flag string is malformed.
	 */
	private static int string2Flags( final String flagString ) throws IOException {
		int flags = 0;
		
		if ( flagString != null && flagString.length() != 0 ) {
			String f[] = flagString.split( "\\|" );
			for( int i = 0; i < f.length; i++ ) {
				try {
					flags |= BVGraph.class.getField( f[ i ].trim() ).getInt( BVGraph.class );
				}
				catch ( Exception notFound ) {
					throw new IOException( "Compression flag " + f[ i ] + " unknown." );
				}
			}
		}
		return flags;
	}


	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory.
	 *
	 * @param basename the basename of the graph.
	 * @param offsetType the desired offset type (2 is memory mapping, 1 is normal random-access loading, 0 means that we do not want to load offsets at all, -1 that
	 * the we do not want even load the graph file).
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph load( final CharSequence basename, final int offsetType, final ProgressLogger pl ) throws IOException {
		return new BVGraph().loadInternal( basename, offsetType, pl );
	}


	
	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory, with no progress logger.
	 *
	 * @param basename the basename of the graph.
	 * @param offsetType the desired offset type (2 is memory mapping, 1 is normal random-access loading, 0 means that we do not want to load offsets at all, -1 that
	 * the we do not want even load the graph file).
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph load( CharSequence basename, int offsetType ) throws IOException {
		return BVGraph.load( basename, offsetType, null );
	}

	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory, with no progress logger and
	 *  all offsets.
	 *
	 * @param basename the basename of the graph.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph load( CharSequence basename ) throws IOException {
		return BVGraph.load( basename, 1 );
	}

	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory, with 
	 *  all offsets.
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph load( CharSequence basename, ProgressLogger pl ) throws IOException {
		return BVGraph.load( basename, 1, pl );
	}

	/** Creates a new {@link BVGraph} by memory-mapping a graph file.
	 * 
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the offsets, or <code>null</code>.
	 * @return an {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while memory-mapping the graph or reading the offsets.
	 */
	public static BVGraph loadMapped( CharSequence basename, ProgressLogger pl ) throws IOException {
		return BVGraph.load( basename, 2, pl );
	}

	/** Creates a new {@link BVGraph} by memory-mapping a graph file.
	 * 
	 * @param basename the basename of the graph.
	 * @return an {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while memory-mapping the graph or reading the offsets.
	 */
	public static BVGraph loadMapped( CharSequence basename ) throws IOException {
		return BVGraph.loadMapped( basename, null );
	}

	
	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory, without offsets. 
	 *
	 * @param basename the basename of the graph.
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph loadSequential( CharSequence basename, ProgressLogger pl ) throws IOException {
		return BVGraph.load( basename, 0, pl );
	}


	/** Creates a new {@link BVGraph} by loading a compressed graph file from disk to memory, with no progress logger and
	 * without offsets.
	 * 
	 * @param basename the basename of the graph.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	public static BVGraph loadSequential( CharSequence basename ) throws IOException {
		return BVGraph.loadSequential( basename, null );
	}



	/** Creates a new {@link BVGraph} by loading just the metadata of a compressed graph file.
	 * 
	 * @param basename the basename of the graph.
	 * @param pl a progress logger, or <code>null</code>.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the metadata.
	 */
	public static BVGraph loadOffline( CharSequence basename, ProgressLogger pl ) throws IOException {
		return BVGraph.load( basename, -1, pl );
	}


	
	/** Creates a new {@link BVGraph} by loading just the metadata of a compressed graph file.
	 * 
	 * @param basename the basename of the graph.
	 * @return a {@link BVGraph} containing the specified graph.
	 * @throws IOException if an I/O exception occurs while reading the metadata.
	 */
	public static BVGraph loadOffline( CharSequence basename ) throws IOException {
		return BVGraph.loadOffline( basename, (ProgressLogger)null );
	}

	

	/** Loads a compressed graph file from disk into this graph. Note that this method should
	 *  be called <em>only</em> on a newly created graph.
	 *
	 * @param basename the basename of the graph.
	 * @param offsetType the desired offset type (2 is memory-mapping, 1 is normal random-access loading, 0 means that we do not want to load offsets at all, -1 that
	 * the we do not want even load the graph file).
	 * @param pl a progress logger used while loading the graph, or <code>null</code>.
	 * @return this graph.
	 * @throws IOException if an I/O exception occurs while reading the graph.
	 */
	protected BVGraph loadInternal( final CharSequence basename, int offsetType, final ProgressLogger pl ) throws IOException {

		// First of all, we read the property file to get the relevant data.
		final FileInputStream propertyFile = new FileInputStream( basename + PROPERTIES_EXTENSION );
		final Properties properties = new Properties();
		properties.load( propertyFile );
		propertyFile.close();

		this.offsetType = offsetType;
		this.basename = new MutableString( basename );

		// Soft check--we accept big stuff, too.
		if ( ! getClass().getName().equals( properties.getProperty( ImmutableGraph.GRAPHCLASS_PROPERTY_KEY ).replace( "it.unimi.dsi.big.webgraph", "it.unimi.dsi.webgraph" ) ) )
			throw new IOException( "This class (" + this.getClass().getName() + ") cannot load a graph stored using class \"" + properties.getProperty( ImmutableGraph.GRAPHCLASS_PROPERTY_KEY ) + "\"" );

		// We parse the properties and perform some consistency check and assignments.
		setFlags( string2Flags( properties.getProperty( "compressionflags" ) ) );
		if ( properties.getProperty( "version" ) == null ) throw new IOException( "Missing format version information" );
		else if ( Integer.parseInt( properties.getProperty( "version" ) ) > BVGRAPH_VERSION ) throw new IOException( "This graph uses format " + properties.getProperty( "version" ) + ", but this class can understand only graphs up to format " + BVGRAPH_VERSION );;

		final long nodes = Long.parseLong( properties.getProperty( "nodes" ) ); 
		if ( nodes > Integer.MAX_VALUE ) throw new IllegalArgumentException( "The standard version of WebGraph cannot handle graphs with " + nodes + " (>2^31) nodes" ); 
		n = (int)nodes; 
		m = Long.parseLong( properties.getProperty( "arcs" ) );
		windowSize = Integer.parseInt( properties.getProperty( "windowsize" ) );
		maxRefCount = Integer.parseInt( properties.getProperty( "maxrefcount" ) );
		minIntervalLength = Integer.parseInt( properties.getProperty( "minintervallength" ) );
		if ( properties.getProperty( "residualcompression" ) != null ) {
			residualCompression = Integer.parseInt( properties.getProperty( "residualcompression" ) );
		}
		if ( properties.getProperty( "blockscompression" ) != null ) {
			blocksCompression = Integer.parseInt( properties.getProperty( "blockscompression" ) );
		}
		if ( properties.getProperty( "zetak" ) != null ) zetaK = Integer.parseInt( properties.getProperty( "zetak" ) );

		if ( offsetType < -1 || offsetType > 2 ) throw new IllegalArgumentException( "Illegal offset type " + offsetType );
		final InputBitStream offsetIbs = offsetType > 0 ? new InputBitStream( new FileInputStream( basename + OFFSETS_EXTENSION ), STD_BUFFER_SIZE ) : null;

		if ( offsetType >= 0 ) {
			final FileInputStream fis = new FileInputStream( basename + GRAPH_EXTENSION );

			if ( offsetType == 2 ) {
				mappedGraphStream = ByteBufferInputStream.map( fis.getChannel(), MapMode.READ_ONLY );
				isMapped = true;
			}
			else {
				// read the whole graph into memory
				if ( pl != null ) {
					pl.itemsName = "bytes";
					pl.start( "Loading graph..." );
				}

				if ( fis.getChannel().size() <= Integer.MAX_VALUE ) {
					graphMemory = new byte[ ( int ) fis.getChannel().size() ];
					BinIO.loadBytes( fis, graphMemory );
					fis.close();
					isMemory = true;
				}
				else graphStream = new FastMultiByteArrayInputStream( fis, fis.getChannel().size() );

				if ( pl != null ) {
					pl.count = isMemory ? graphMemory.length : graphStream.length;
					pl.done();
				}
			}
		}
		
		if ( offsetType == 1 || offsetType == 2 ) {
			// read offsets, if required

			if ( pl != null ) {
				pl.itemsName = "deltas";
				pl.start( "Loading offsets..." );
			}

			// We try to load a cached big list.
			final File offsetsBigListFile = new File( basename + OFFSETS_BIG_LIST_EXTENSION );
			if ( offsetsBigListFile.exists() ) {
				if ( new File( basename + OFFSETS_EXTENSION ).lastModified() > offsetsBigListFile.lastModified() ) LOGGER.warn( "A cached long big list of offsets was found, but the corresponding offsets file has a later modification time" );
				else try {
					offsets = (LongBigList)BinIO.loadObject( offsetsBigListFile );
				}
				catch ( ClassNotFoundException e ) {
					LOGGER.warn( "A cached long big list of offsets was found, but its class is unknown", e );
				}	
			}
			if ( offsets == null ) offsets = new EliasFanoMonotoneLongBigList( n + 1, ( isMapped ? mappedGraphStream.length() : isMemory ? graphMemory.length : graphStream.length ) * Byte.SIZE + 1, new OffsetsLongIterator( this, offsetIbs ) );

			if ( pl != null ) {
				pl.count = n + 1;
				pl.done();
				if ( offsets instanceof EliasFanoMonotoneLongBigList ) pl.logger().info( "Pointer bits per node: " + Util.format( ((EliasFanoMonotoneLongBigList)offsets).numBits() / ( n + 1.0 ) ) );
			}
		}

		if ( offsetIbs != null ) offsetIbs.close();

		// We finally create the outdegreeIbs and, if needed, the two caches
		if ( offsetType >= 0 ) outdegreeIbs = isMemory ? new InputBitStream( graphMemory ): new InputBitStream( isMapped ? mappedGraphStream.copy() : new FastMultiByteArrayInputStream( graphStream ), 0 );

		return this;
	}



	/** This method tries to express an increasing sequence of natural numbers <code>x</code> as a union of an increasing
	 *  sequence of intervals and an increasing sequence of residual elements. More precisely, this intervalization works
	 *  as follows: first, one looks at <code>x</code> as a sequence of intervals (i.e., maximal sequences of consecutive
	 *  elements); those intervals whose length is &ge; <code>minInterval</code> are stored in the lists <code>left</code>
	 *  (the list of left extremes) and <code>len</code> (the list of lengths; the length of an integer interval is the
	 *  number of integers in that interval). The remaining integers, called <em>residuals</em> are stored in the
	 *  <code>residual</code> list.
	 * 
	 *  <P>Note that the previous content of <code>left</code>, <code>len</code> and <code>residual</code> is lost.
	 *
	 *  @param x the list to be intervalized (an increasing list of natural numbers).
	 *  @param minInterval the least length that a maximal sequence of consecutive elements must have in order for it to
	 *   be considered as an interval.
	 *  @param left the resulting list of left extremes of the intervals.
	 *  @param len the resulting list of interval lengths.
	 *  @param residuals the resulting list of residuals.
	 *  @return the number of intervals.
	 */
	protected static int intervalize( final IntArrayList x, final int minInterval, final IntArrayList left, final IntArrayList len, final IntArrayList residuals ) {
		int nInterval = 0;
		int vl = x.size();
		int v[] = x.elements();
		int i, j;

		left.clear(); len.clear(); residuals.clear();
		for( i = 0; i < vl; i++ ) {
			j = 0;
			if ( i < vl - 1 && v[ i ] + 1 == v[ i + 1 ] ) {
				do j++; while( i + j < vl - 1 && v[ i + j ] + 1 == v[ i + j + 1 ] );
				j++;
				// Now j is the number of integers in the interval.
				if ( j >= minInterval ) {
					left.add( v[ i ] );
					len.add( j );
					nInterval++;
					i += j - 1;
				}
			}
			if ( j < minInterval ) residuals.add( v[ i ] );
		}
		return nInterval;
	}




	/** Scratch variables used by the {@link #diffComp(OutputBitStream, int, int, int[], int, int[], int, boolean)} method. */
	private IntArrayList extras = new IntArrayList(), blocks = new IntArrayList(), residuals = new IntArrayList(),
		left = new IntArrayList(), len = new IntArrayList(),
		blockFlags = new IntArrayList();

	/** Compresses differentially the given list. This method is given a node (with index <code>currNode</code>) called the
	 * current node, with its successor list (contained in the array <code>currList[0..currLen-1]</code>), and another node
	 * (with index <code>currNode</code>&minus;<code>ref</code>), called the reference node, with its successor list (contained in the array
	 * <code>refList[0..refLen-1]</code>). This method produces, onto the given output bit stream, the compressed successor
	 * list of the current node using the reference node given (except for the outdegree); the number of bits written is returned. 
	 *
	 * Note that <code>ref</code> may be zero, in which case no differential compression is made.
	 *
	 * @param obs an output bit stream where the compressed data will be stored.
	 * @param currNode the index of the node this list of outlinks refers to.
	 * @param ref the distance from the reference list.
	 * @param refList the reference list.
	 * @param refLen the length of the reference list.
	 * @param currList the current list.
	 * @param currLen the current list length.
	 * @param forReal if true, we are really writing data (i.e., <code>obs</code> is not just a bit count stream).
	 * @return the number of bits written.
	 */
	private int diffComp( final OutputBitStream obs, final int currNode, final int ref, final int refList[], int refLen, final int currList[], final int currLen, final boolean forReal ) throws IOException {
		// Bits already written onto the output bit stream
		final long writtenBitsAtStart = obs.writtenBits();

		// We build the list of blocks copied and skipped (alternatively) from the previous list.
		int i, j = 0, k = 0, prev = 0, currBlockLen = 0, currBlockFlag = 0, t;
		boolean copying = true;

		if ( ref == 0 ) refLen = 0; // This guarantees that we will not try to differentially compress when ref == 0.

		extras.clear();
		blocks.clear();
		blockFlags.clear();

		// j is the index of the next successor of the current node we must examine
		// k is the index of the next successor of the reference node we must examine
		// copying is true iff we are producing a copy block (instead of an ignore block)
		// currBlockLen is the number of entries (in the reference list) we have already copied/ignored (in the current block)
		while( j < currLen && k < refLen ) {
			if ( copying ) { // First case: we are currectly copying entries from the reference list
				// Copy flags compression processing
				// block flags: 0 = exact ref, 1 = none, 2 = ref+1, 3 = ref+2
				if ( blocksCompression == 2 && currList[j] == refList[k]+1 && (k+1 >= refLen || currList[j] != refList[k+1]) && (k+2 >= refLen || currList[j] != refList[k+2]) ) {
					if ( currBlockFlag != 2 && currBlockLen > 0 ) {
						blocks.add( currBlockLen );
						blockFlags.add( currBlockFlag );
						currBlockLen = 0;
					}
					currBlockFlag = 2;
					j++;
					k++;
					currBlockLen++;
					if ( forReal ) copiedArcs++;
				}
				else if ( blocksCompression == 2 && currList[j] == refList[k]+2 && (k+1 >= refLen || (currList[j] != refList[k+1] && currList[j] != refList[k+1]+1)) && (k+2 >= refLen || (currList[j] != refList[k+1] && currList[j] != refList[k+2]+1)) ) {
					if ( currBlockFlag != 3 && currBlockLen > 0 ) {
						blocks.add( currBlockLen );
						blockFlags.add( currBlockFlag );
						currBlockLen = 0;
					}
					currBlockFlag = 3;
					j++;
					k++;
					currBlockLen++;
					if ( forReal ) copiedArcs++;
				}
				else if ( currList[ j ] > refList[ k ] ) {
					/* If while copying we trespass the current element of the reference list,
					   we must stop copying. */
					if ( blocksCompression != 2 || currBlockLen > 0 ) {
						blocks.add( currBlockLen );
						blockFlags.add( currBlockFlag );
					}
					copying = false;
					currBlockFlag = 1;
					currBlockLen = 0;
					if ( blocksCompression == 2 ) {
						currBlockLen = 1;
						k++;
					}
				}
				else if ( currList[ j ] < refList[ k ] ) {
					/* If while copying we find a non-matching element of the reference list which
					   is larger than us, we can just add the current element to the extra list
					   and move on. j gets increased. */
					extras.add( currList[ j++ ] );
				}
				else { // currList[ j ] == refList[ k ]
					/* If the current elements of the two lists are equal, we just increase the block length. 
					   both j and k get increased. */
					if ( blocksCompression == 2 && currBlockFlag != 0 && currBlockLen > 0 ) {
						blocks.add( currBlockLen );
						blockFlags.add( currBlockFlag );
						currBlockLen = 0;
					}
					currBlockFlag = 0;
					j++;
					k++;
					currBlockLen++;
					if ( forReal ) copiedArcs++;
				}
			}
			else { // Second case: we are currently skipping entries from the reference list
				if ( blocksCompression == 2 && currList[j] == refList[k]+1 ) {
					/* If we found a match we flush the current block and start a new copying phase. */
					blocks.add( currBlockLen );
					blockFlags.add( currBlockFlag );
					copying = true;
					currBlockLen = 0;
					currBlockFlag = 2;
				}
				else if ( blocksCompression == 2 && currList[j] == refList[k]+2 ) {
					blocks.add( currBlockLen );
					blockFlags.add( currBlockFlag );
					copying = true;
					currBlockLen = 0;
					currBlockFlag = 3;
				}
				else if ( currList[ j ] < refList[ k ] ) {
					/* If we did not trespass the current element of the reference list, we just
					   add the current element to the extra list and move on. j gets increased. */
					extras.add( currList[ j++ ] );
				}
				else if ( currList[ j ] > refList[ k ] ) {
					/* If we trespassed the current element of the reference list, we
					   increase the block length. k gets increased. */
					k++;
					currBlockLen++;
				}
				else { // currList[ j ] == refList[ k ]
					/* If we found a match we flush the current block and start a new copying phase. */
					blocks.add( currBlockLen );
					blockFlags.add( currBlockFlag );
					copying = true;
					currBlockLen = 0;
					currBlockFlag = 0;
				}
			}
		}

		/* We do not record the last block. The only case when we have to enqueue the last block's length
		 * is when we were copying and we did not copy up to the end of the reference list.
		 */
		if ( copying &&
			(k < refLen || (blocksCompression == 2 && currBlockFlag != 1))
		) {
			blocks.add( currBlockLen );
			blockFlags.add( currBlockFlag );
		}

		// If there are still missing elements, we add them to the extra list.
		while( j < currLen ) extras.add( currList[ j++ ] );

		// We store locally the resulting arrays for faster access.
		final int block[] = blocks.elements(), blockCount = blocks.size(), extraCount = extras.size();
	
		// If we have a nontrivial reference window we write the reference to the reference list.
		if ( windowSize > 0 ) { 
			t = writeReference( obs, ref );
			if ( forReal ) bitsForReferences += t;
		}

		if ( STATS ) if ( forReal ) referenceStats.println( ref );

		// Then, if the reference is not void we write the length of the copy list.
		if ( ref != 0 ) {
			if ( STATS ) if ( forReal ) blockCountStats.println( blockCount );
			if ( blocksCompression == 1 ) {
				t = writeBlockCount( obs, blockCount );
				if ( forReal ) bitsForBlocks += t;
				

				// Then, we write the copy list; all lengths except the first one are decremented.
				if ( blockCount > 0 ) {
					t = writeBlock( obs, block[ 0 ] );
					if ( forReal ) bitsForBlocks += t;
					for( i = 1; i < blockCount; i++ ) { 
						t = writeBlock( obs, block[ i ] - 1 );
						if ( forReal ) bitsForBlocks += t;
					}
				
					if ( STATS ) if ( forReal ) {
						blockStats.println( block[ 0 ] ); 
						for( i = 1; i < blockCount; i++ ) blockStats.println( block[ i ] - 1 );
					}
				}
			}
			else if ( blocksCompression == 2 ) {
				// Copy flags compression writing
				int count = 0;
				if ( blockCount > 0 ) {
					for ( i = 0; i < blockCount; i++ ) {
						for ( int l = 0; l < block[ i ]; l++ ) {
							obs.writeInt(blockFlags.get(i), 2);
						}
						count += block[i];
					}
					if ( STATS ) if ( forReal ) {
						blockStats.println( block[ 0 ] ); 
						for( i = 1; i < blockCount; i++ ) blockStats.println( block[ i ] - 1 );
					}
				}
				for ( int l = 0; l < refLen - count; l++ ) {
					obs.writeInt(1, 2);
				}

				if ( forReal ) bitsForBlocks += currLen * 2;
			}
			else {
				// Copy list compression writing
				int count = 0;
				boolean isOne = true;
				if ( blockCount > 0 ) {
					for ( i = 0; i < blockCount; i++ ) {
						for ( int l = 0; l < block[ i ]; l++ ) {
							obs.writeBit(isOne);
						}
						isOne = !isOne;
						count += block[i];
					}
					if ( STATS ) if ( forReal ) {
						blockStats.println( block[ 0 ] ); 
						for( i = 1; i < blockCount; i++ ) blockStats.println( block[ i ] - 1 );
					}
				}
				for ( int l = 0; l < refLen - count; l++ ) {
					obs.writeBit(isOne);
				}

				if ( forReal ) bitsForBlocks += currLen;
			}
		}

		// Finally, we write the extra list.
		if ( extraCount > 0 ) {

			final int residual[], residualCount;

			if ( minIntervalLength != NO_INTERVALS ) {
				// If we are to produce intervals, we first compute them.
				final int intervalCount = intervalize( extras, minIntervalLength, left, len, residuals );
				
				// We write the number of intervals.
				t = obs.writeGamma( intervalCount );
				if ( forReal ) bitsForIntervals += t; 
				
				if ( STATS ) if ( forReal ) intervalCountStats.println( intervalCount );
				
				int currIntLen;
				
				// We write out the intervals.
				for( i = 0; i < intervalCount; i++ ) {
					if ( i == 0 ) t = obs.writeLongGamma( Fast.int2nat( (long)( prev = left.getInt( i ) ) - currNode ) );
					else t = obs.writeGamma( left.getInt( i ) - prev - 1 );
					if ( forReal ) bitsForIntervals += t;
					currIntLen = len.getInt( i );
					prev = left.getInt( i ) + currIntLen;
					if ( forReal ) intervalisedArcs += currIntLen;
					t = obs.writeGamma( currIntLen - minIntervalLength );
					if ( forReal ) bitsForIntervals += t;
				}

				if ( STATS ) if ( forReal ) for( i = 0; i < intervalCount; i++ ) {
					if ( i == 0 ) leftStats.println( Fast.int2nat( (long)( prev = left.getInt( i ) ) - currNode ) );
					else leftStats.println( left.getInt( i ) - prev - 1 );
					prev = left.getInt( i ) + len.getInt( i );
					lenStats.println( len.getInt( i ) - minIntervalLength );
				}


				residual = residuals.elements();
				residualCount = residuals.size();
			}
			else {
				residual = extras.elements();
				residualCount = extras.size();
			}
					 
			if ( STATS ) if ( forReal ) residualCountStats.println( residualCount );

			// Now we write out the residuals, if any
			if ( residualCount != 0 ) {
				if ( forReal ) {
					residualArcs += residualCount;
					updateBins( currNode, residual, residualCount, residualGapStats );
				}
				if ( residualCompression == 1 ) {
					t = writeResidual( obs, Fast.int2nat( (long)( prev = residual[ 0 ] ) - currNode ) );
				}
				else {
					t = writeResidual( obs, (long)( prev = residual[0] ) );
				}
				if ( forReal ) bitsForResiduals += t;
				for( i = 1; i < residualCount; i++ ) {
					if ( residual[ i ] == prev ) throw new IllegalArgumentException( "Repeated successor " + prev + " in successor list of node " + currNode );
					if ( residualCompression == 1 ) {
						t = writeResidual( obs, residual[ i ] - prev - 1 );
					}
					else {
						t = writeResidual( obs, residual[ i ] );
					}
					if ( forReal ) bitsForResiduals += t;
					prev = residual[ i ];
				}
			
				if ( STATS ) if ( forReal ) {
					residualStats.println( Fast.int2nat( (long)( prev = residual[ 0 ] ) - currNode ) );
					for( i = 1; i < residualCount; i++ ) {
						residualStats.println( residual[ i ] - prev - 1 );
						prev = residual[ i ];
					}
				}
			}
			
		}

		return (int)( obs.writtenBits() - writtenBitsAtStart );
	}

	/** Writes the given graph using a given base name.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @param windowSize the window size (-1 for the default value).
	 * @param maxRefCount the maximum reference count (-1 for the default value).
	 * @param minIntervalLength the minimum interval length (-1 for the default value, {@link #NO_INTERVALS} to disable).
	 * @param residualCompression the residual compression format (-1 for the default value).
	 * @param blocksCompression the reference copy list compression format (-1 for the default value).
	 * @param zetaK the parameter used for residual &zeta;-coding, if used (-1 for the default value).
	 * @param flags the flag mask.
	 * @param pl a progress logger to log the state of compression, or <code>null</code> if no logging is required.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	public static void store( ImmutableGraph graph, CharSequence basename, int windowSize, int maxRefCount, int minIntervalLength, int residualCompression, int blocksCompression, int zetaK, int flags, ProgressLogger pl ) throws IOException {
		BVGraph g = new BVGraph();
		if ( windowSize != -1 ) g.windowSize = windowSize;
		if ( maxRefCount != -1 ) g.maxRefCount = maxRefCount;
		if ( minIntervalLength != -1 ) g.minIntervalLength = minIntervalLength;
		if ( residualCompression != -1 ) g.residualCompression = residualCompression;
		if ( blocksCompression != -1 ) g.blocksCompression = blocksCompression;
		if ( zetaK != -1 ) g.zetaK = zetaK;
		g.setFlags( flags );
		g.storeInternal( graph, basename, pl );
	}
	
	/** Writes the given graph using a given base name.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @param windowSize the window size (-1 for the default value).
	 * @param maxRefCount the maximum reference count (-1 for the default value).
	 * @param minIntervalLength the minimum interval length (-1 for the default value, {@link #NO_INTERVALS} to disable).
	 * @param zetaK the parameter used for residual &zeta;-coding, if used (-1 for the default value).
	 * @param flags the flag mask.
	 * @param pl a progress logger to log the state of compression, or <code>null</code> if no logging is required.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	public static void store( ImmutableGraph graph, CharSequence basename, int windowSize, int maxRefCount, int minIntervalLength, 
		int zetaK, int flags, ProgressLogger pl ) throws IOException {
		BVGraph g = new BVGraph();
		if ( windowSize != -1 ) g.windowSize = windowSize;
		if ( maxRefCount != -1 ) g.maxRefCount = maxRefCount;
		if ( minIntervalLength != -1 ) g.minIntervalLength = minIntervalLength;
		if ( zetaK != -1 ) g.zetaK = zetaK;
		g.setFlags( flags );
		g.storeInternal( graph, basename, pl );
	}
	
	/** Writes the given graph using a given base name, without any progress logger.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @param windowSize the window size (-1 for the default value).
	 * @param maxRefCount the maximum reference count (-1 for the default value).
	 * @param minIntervalLength the minimum interval length (-1 for the default value, {@link #NO_INTERVALS} to disable).
	 * @param zetaK the parameter used for residual &zeta;-coding, if used (-1 for the default value).
	 * @param flags the flag mask.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	public static void store( ImmutableGraph graph, CharSequence basename, int windowSize, int maxRefCount, int minIntervalLength, 
		int zetaK, int flags ) throws IOException {
		BVGraph.store( graph, basename, windowSize, maxRefCount, minIntervalLength, -1, -1, zetaK, flags, (ProgressLogger)null );
	}

	/** Writes the given graph using a given base name, with all
	 * parameters set to their default values.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @param pl a progress logger to log the state of compression, or <code>null</code> if no logging is required.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	public static void store( ImmutableGraph graph, CharSequence basename, ProgressLogger pl ) throws IOException {
		BVGraph.store( graph, basename, -1, -1, -1, -1, -1, -1, 0, pl );
	}

	
	/** Writes the given graph using a given base name, without any progress logger and with all
	 * parameters set to their default values.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	public static void store( ImmutableGraph graph, CharSequence basename ) throws IOException {
		BVGraph.store( graph, basename, (ProgressLogger)null );
	}


	/** Updates a list of exponential bins using the gaps a given list of strinctly increasing integers. 
	 * @param currNode the current node.
	 * @param list a strictly increasing list of integers.
	 * @param length the number of valid elements in <code>list</code>. 
	 * @param bin the bins.
	 */
	private static void updateBins( final int currNode, final int[] list, final int length, final long[] bin ) {
		for( int i = length - 1; i-- != 0; ) bin[ Fast.mostSignificantBit( list[ i + 1 ] - list[ i ] ) ]++;
		final int msb = Fast.mostSignificantBit( Fast.int2nat( (long)list[ 0 ] - currNode ) );
		if ( msb >= 0 ) bin[ msb ]++;
	}

	/** Statistics for the gap width of successor lists (exponentially binned). */
	private long[] successorGapStats;
	/** Statistics for the gap width of residuals (exponentially binned). */
	private long[] residualGapStats;
	/** Bits used for outdegress. */
	private long bitsForOutdegrees;
	/** Bits used to write backward references. */
	private long bitsForReferences;
	/** Bits used to write inclusion-exclusion blocks. */
	private long bitsForBlocks;
	/** Bits used to write residuals. */
	private long bitsForResiduals;
	/** Bits used to write intervals. */
	private long bitsForIntervals;
	
	/** Writes the given graph <code>graph</code> using a given base name, and the compression parameters and flags
	 * of this graph object. Note that the latter is relevant only as far as parameters and flags are concerned; its
	 * content is really irrelevant.
	 *
	 * @param graph a graph to be compressed.
	 * @param basename a base name.
	 * @param pl a progress logger to measure the state of compression, or <code>null</code> if no logging is required.
	 * @throws IOException if some exception is raised while writing the graph.
	 */
	private void storeInternal( ImmutableGraph graph, CharSequence basename, ProgressLogger pl ) throws IOException {
		// Used for differential compression
		final OutputBitStream bitCount = new OutputBitStream( NullOutputStream.getInstance(), 0  );
		int outd, currNode = -1, currIndex, j, bestIndex, cand;
		long best, t, bitOffset = 0;
		copiedArcs = 0;
		intervalisedArcs = 0;
		residualArcs = 0;
		
		OutputBitStream graphObs = new OutputBitStream( new FileOutputStream( basename + GRAPH_EXTENSION ), STD_BUFFER_SIZE );
		OutputBitStream offsetObs = new OutputBitStream( new FileOutputStream( basename + OFFSETS_EXTENSION ), STD_BUFFER_SIZE );

		if ( STATS ) {
			offsetStats = new PrintWriter( new FileWriter( basename + ".offsetStats" ) );
			referenceStats = new PrintWriter( new FileWriter( basename + ".referenceStats" ) );
			outdegreeStats = new PrintWriter( new FileWriter( basename + ".outdegreeStats" ) );
			blockCountStats = new PrintWriter( new FileWriter( basename + ".blockCountStats" ) );
			blockStats = new PrintWriter( new FileWriter( basename + ".blockStats" ) );
			intervalCountStats = new PrintWriter( new FileWriter( basename + ".intervalCountStats" ) );
			leftStats = new PrintWriter( new FileWriter( basename + ".leftStats" ) );
			lenStats = new PrintWriter( new FileWriter( basename + ".lenStats" ) );
			residualCountStats = new PrintWriter( new FileWriter( basename + ".residualCountStats" ) );
			residualStats = new PrintWriter( new FileWriter( basename + ".residualStats" ) );
		}

		final int cyclicBufferSize = windowSize + 1;
		// Cyclic array of previous lists.
		int list[][] = new int[ cyclicBufferSize ][ INITIAL_SUCCESSOR_LIST_LENGTH ];
		// For each list, its length.
		int listLen[] = new int[ cyclicBufferSize ];
		// For each list, the depth of its references.
		int refCount[] = new int[ cyclicBufferSize ];
		successorGapStats = new long[ 32 ];
		residualGapStats = new long[ 32 ];
		
		long totRef = 0, totDist = 0, totLinks = 0;

		// Note that it is fundamental that the time required to set up the iterator is not measured by the progress logger.
		final NodeIterator nodeIterator = graph.nodeIterator();
		nodeIterator.hasNext(); // Forces offline graphs to fill buffers.
		
		if ( pl != null ) {
			pl.itemsName = "nodes";
			try {
				pl.expectedUpdates = graph.numNodes();
			}
			catch( UnsupportedOperationException ignore ) {}
			pl.start( "Storing..." );
		}
		
		// We iterate over the nodes of graph
		while( nodeIterator.hasNext() ) {
			// currNode is the currently examined node, of outdegree outd, with index currIndex (within the cyclic array)
			int u = nodeIterator.nextInt();
			if ( ++currNode != u ) throw new IllegalStateException( "Invalid node sequence: expected " + currNode + ", found " + u );
			outd = nodeIterator.outdegree();// get the number of successors of currNode
			currIndex = currNode % cyclicBufferSize;

			// We write the current offset to the offset stream
			writeOffset( offsetObs, graphObs.writtenBits() - bitOffset );

			if ( STATS ) offsetStats.println( graphObs.writtenBits() - bitOffset );
		
			bitOffset = graphObs.writtenBits();

			// We write the node outdegree
			bitsForOutdegrees += writeOutdegree( graphObs, outd );

			if ( STATS ) outdegreeStats.println( outd );

			if ( outd > list[ currIndex ].length ) list[ currIndex ] = IntArrays.ensureCapacity( list[ currIndex ], outd );

			// The successor list we are going to compress and write out
			System.arraycopy( nodeIterator.successorArray(), 0, list[ currIndex ], 0, outd );
			listLen[ currIndex ] = outd;
			
			if ( outd > 0 ) {
				updateBins( currNode, list[ currIndex ], outd, successorGapStats );
				try {
					// Now we check the best candidate for compression.
					best = Integer.MAX_VALUE;
					bestIndex = -1;
					
					refCount[ currIndex ] = -1;
					
					for( j = 0; j < cyclicBufferSize; j++ ) {
						cand = ( currNode - j + cyclicBufferSize ) % cyclicBufferSize;
						if ( refCount[ cand ] < maxRefCount && listLen[ cand ] != 0
								&& ( t = diffComp( bitCount, currNode, j, list[ cand ], listLen[ cand ], list[ currIndex ], listLen[ currIndex ], false ) ) < best ) {
							best = t;
							bestIndex = cand;
						}
					}
					
					if ( ASSERTS ) assert bestIndex >= 0;
					refCount[ currIndex ] = refCount[ bestIndex ] + 1;
					diffComp( graphObs, currNode, ( currNode - bestIndex + cyclicBufferSize ) % cyclicBufferSize, list[ bestIndex ], 
							listLen[ bestIndex ], list[ currIndex ], listLen[ currIndex], true );
					
					totLinks += outd;
					totRef += refCount[ currIndex ];
					totDist += ( currNode - bestIndex + cyclicBufferSize ) % cyclicBufferSize;
				} catch ( RuntimeException e ) {
					LOGGER.debug( "An exception occurred while storing node " + currNode + " with outlinks " + Arrays.toString( Arrays.copyOfRange( nodeIterator.successorArray(), 0, nodeIterator.outdegree() ) ) );
					throw e;
				}
			}

				
			if ( pl != null && ( ( currNode + 1 ) & ( ( 1 << 20 ) - 1 ) ) == 0 ) LOGGER.info( new Formatter( Locale.ROOT ).format(
					"bits/link: %.3f; bits/node: %.3f; avgref: %.3f; avgdist: %.3f.",
					Double.valueOf( (double)graphObs.writtenBits() / ( totLinks != 0 ? totLinks : 1 ) ),
					Double.valueOf( (double)graphObs.writtenBits() / currNode ),
					Double.valueOf( ( double )totRef / currNode ),
					Double.valueOf( ( double )totDist / currNode )
				).toString()
			);
			
			if ( pl != null ) pl.update();
		}
		
		if ( currNode + 1 != graph.numNodes() ) throw new IllegalStateException( "The graph claimed to have " + graph.numNodes() + " nodes, but the node iterator returned " + ( currNode + 1 ) );
		
		// We write the final offset to the offset stream.
		writeOffset( offsetObs, graphObs.writtenBits() - bitOffset );

		graphObs.close();
		offsetObs.close();

		if ( pl != null ) pl.done();
		
		final DecimalFormat format = ((DecimalFormat)NumberFormat.getInstance( Locale.US ));
		format.applyPattern( "0.###" );
		
		// Finally, we save all data related to this graph in a property file.
		final Properties properties = new Properties();
		final int n = graph.numNodes(); // At this point this *must* work (see ArcListASCIIGraph)
		properties.setProperty( "nodes", String.valueOf( n ) );
		properties.setProperty( "arcs", String.valueOf( totLinks ) );
		properties.setProperty( "windowsize", String.valueOf( windowSize ) );
		properties.setProperty( "maxrefcount", String.valueOf( maxRefCount ) );
		properties.setProperty( "minintervallength", String.valueOf( minIntervalLength ) );
		properties.setProperty( "residualcompression", String.valueOf( residualCompression ) );
		properties.setProperty( "blockscompression", String.valueOf( blocksCompression ) );
		if ( residualCoding == ZETA ) properties.setProperty( "zetak", String.valueOf( zetaK ) );
		properties.setProperty( "compressionflags", flags2String( flags ).toString() );
		properties.setProperty( "avgref", format.format( (double)totRef / n ) );
		properties.setProperty( "avgdist", format.format( (double) totDist / n ) );
		properties.setProperty( "copiedarcs", String.valueOf( copiedArcs ) );
		properties.setProperty( "intervalisedarcs", String.valueOf( intervalisedArcs ) );
		properties.setProperty( "residualarcs", String.valueOf( residualArcs ) );
		properties.setProperty( "bitsperlink", format.format( (double)graphObs.writtenBits() / totLinks ) );
		properties.setProperty( "compratio", format.format( graphObs.writtenBits() * Math.log( 2 ) / ( stirling( (double)n * n ) - stirling( totLinks ) - stirling( (double)n * n - totLinks ) ) ) );
		properties.setProperty( "bitspernode", format.format( (double)graphObs.writtenBits() / n ) );
		properties.setProperty( "avgbitsforoutdegrees", format.format( (double)bitsForOutdegrees / n ) );
		properties.setProperty( "avgbitsforreferences", format.format( (double)bitsForReferences / n ) );
		properties.setProperty( "avgbitsforblocks", format.format( (double)bitsForBlocks / n ) );
		properties.setProperty( "avgbitsforresiduals", format.format( (double)bitsForResiduals / n ) );
		properties.setProperty( "avgbitsforintervals", format.format( (double)bitsForIntervals / n ) );
		properties.setProperty( "bitsforoutdegrees", Long.toString( bitsForOutdegrees ) );
		properties.setProperty( "bitsforreferences", Long.toString( bitsForReferences ) );
		properties.setProperty( "bitsforblocks", Long.toString( bitsForBlocks ) );
		properties.setProperty( "bitsforresiduals", Long.toString( bitsForResiduals) );
		properties.setProperty( "bitsforintervals", Long.toString( bitsForIntervals ) );
		properties.setProperty( ImmutableGraph.GRAPHCLASS_PROPERTY_KEY, this.getClass().getName() );
		properties.setProperty( "version", String.valueOf( BVGRAPH_VERSION ) );
		final FileOutputStream propertyFile = new FileOutputStream( basename + PROPERTIES_EXTENSION );
		// Binned data
		int l;
		for( l = successorGapStats.length; l-- != 0; ) if ( successorGapStats[ l ] != 0 ) break;
		StringBuilder s = new StringBuilder();
		BigInteger totGap = BigInteger.ZERO;
		double totLogGap = 0;
		long numGaps = 0;
		
		long g = 1;
		for( int i = 0; i <= l; i++ ) {
			if ( i != 0 ) s.append( ',' );
			s.append( successorGapStats[ i ] );
			numGaps += successorGapStats[ i ];
			totGap = totGap.add( BigInteger.valueOf( g * 2 + g - 1 ).multiply( BigInteger.valueOf( successorGapStats[ i ] ) ) );
			totLogGap += ( Fast.log2( g * 2 + g + 1 ) - 1 ) * successorGapStats[ i ];
			g *= 2;
		}

		properties.setProperty( "successorexpstats", s.toString() );
		properties.setProperty( "successoravggap", numGaps == 0 ? "0" : new BigDecimal( totGap ).divide( BigDecimal.valueOf( numGaps * 2 ), 3, RoundingMode.HALF_EVEN ).toString() );
		properties.setProperty( "successoravgloggap", numGaps == 0 ? "0" : Double.toString( totLogGap / numGaps ) );

		s.setLength( 0 );
		
		for( l = residualGapStats.length; l-- != 0; ) if ( residualGapStats[ l ] != 0 ) break;
		g = 1;
		numGaps = 0;
		totLogGap = 0;
		totGap = BigInteger.ZERO;
		for( int i = 0; i <= l; i++ ) {
			if ( i != 0 ) s.append( ',' );
			s.append( residualGapStats[ i ] );
			totGap = totGap.add( BigInteger.valueOf( g * 2 + g - 1 ).multiply( BigInteger.valueOf( residualGapStats[ i ] ) ) );
			totLogGap += ( Fast.log2( g * 2 + g + 1 ) - 1 ) * residualGapStats[ i ];
			numGaps += residualGapStats[ i ];
			g *= 2;
		}
		
		properties.setProperty( "residualexpstats", s.toString() );
		properties.setProperty( "residualavggap", numGaps == 0 ? "0" : new BigDecimal( totGap ).divide( BigDecimal.valueOf( numGaps * 2 ), 3, RoundingMode.HALF_EVEN ).toString() );
		properties.setProperty( "residualavgloggap", numGaps == 0 ? "0" : Double.toString( totLogGap / numGaps ) );
		
		properties.store( propertyFile, "BVGraph properties" );

		propertyFile.close();

		if ( STATS ) {
			offsetStats.close();
			referenceStats.close();
			outdegreeStats.close();
			blockCountStats.close();
			blockStats.close();
			intervalCountStats.close();
			leftStats.close();
			lenStats.close();
			residualCountStats.close();
			residualStats.close();
		}
	}

	private static double stirling( double n ) {
		return n * Math.log( n ) - n + (1./2) * Math.log( 2 * Math.PI * n ) ;
	}
	
	/** Write the offset file to a given bit stream.
	 * @param obs the output bit stream to which offsets will be written.
	 * @param pl a progress logger, or <code>null</code>.  
	 */
	public void writeOffsets( final OutputBitStream obs, final ProgressLogger pl ) throws IOException {
		final BVGraphNodeIterator nodeIterator = (BVGraphNodeIterator) nodeIterator( 0 );
		int n = numNodes();
		long lastOffset = 0;
		while( n-- != 0 ) {
				// We fetch the current position of the underlying input bit stream, which is at the start of the next node.
				writeOffset( obs, nodeIterator.ibs.readBits() - lastOffset );
				lastOffset = nodeIterator.ibs.readBits();
				nodeIterator.nextInt();
				nodeIterator.outdegree();
				nodeIterator.successorArray();
				if ( pl != null ) pl.update();
		}
		writeOffset( obs, nodeIterator.ibs.readBits() - lastOffset );
	}
		

	/** Reads an immutable graph and stores it as a {@link BVGraph}. */
	public static void main( String args[] ) throws SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, JSAPException, ClassNotFoundException, InstantiationException {
		String source, dest;
		Class<?> graphClass;
		int flags = 0;

		SimpleJSAP jsap = new SimpleJSAP( BVGraph.class.getName(), "Compresses differentially a graph. Source and destination are basenames from which suitable filenames will be stemmed; alternatively, if the suitable option was specified, source is a spec (see below). For more information about the compression techniques, see the Javadoc documentation.",
				new Parameter[] {
						new FlaggedOption( "comp", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'c', "comp", "A compression flag (may be specified several times)." ).setAllowMultipleDeclarations( true ),
						new FlaggedOption( "windowSize", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_WINDOW_SIZE ), JSAP.NOT_REQUIRED, 'w', "window-size", "Reference window size (0 to disable)." ),
						new FlaggedOption( "maxRefCount", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_MAX_REF_COUNT ), JSAP.NOT_REQUIRED, 'm', "max-ref-count", "Maximum number of backward references (-1 for ∞)." ),
						new FlaggedOption( "minIntervalLength", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_MIN_INTERVAL_LENGTH ), JSAP.NOT_REQUIRED, 'i', "min-interval-length", "Minimum length of an interval (0 to disable)." ),
						new FlaggedOption( "residualCompression", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_RESIDUAL_COMPRESSION ), JSAP.NOT_REQUIRED, 'r', "residual-compression", "Residual nodes compression format (0 for none)." ),
						new FlaggedOption( "blocksCompression", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_BLOCKS_COMPRESSION ), JSAP.NOT_REQUIRED, 'b', "blocks-compression", "Copy blocks compression format (0 for copy list)." ),
						new FlaggedOption( "zetaK", JSAP.INTEGER_PARSER, String.valueOf( DEFAULT_ZETA_K ), JSAP.NOT_REQUIRED, 'k', "zeta-k", "The k parameter for zeta-k codes." ),
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph." ),
						new Switch( "spec", 's', "spec", "The source is not a basename but rather a specification of the form <ImmutableGraphImplementation>(arg,arg,...)." ),
						new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ),
						new Switch( "offline", 'o', "offline", "Use the offline load method to reduce memory consumption." ),
						new Switch( "once", '1', "once", "Use the read-once load method to read a graph from standard input." ),
						new Switch( "offsets", 'O', "offsets", "Generates offsets for the source graph." ),
						new Switch( "list", 'L', "list", "Precomputes an Elias-Fano list of offsets for the source graph." ),
						new Switch( "degrees", 'd', "degrees", "Stores the outdegrees of all nodes using &gamma; coding." ),
						new UnflaggedOption( "sourceBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the source graph, or a source spec if --spec was given; it is immaterial when --once is specified." ),
						new UnflaggedOption( "destBasename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED, JSAP.NOT_GREEDY, "The basename of the destination graph; if omitted, no recompression is performed. This is useful in conjunction with --offsets and --list." ),
					}		
				);
		
		JSAPResult jsapResult = jsap.parse( args );
		if ( jsap.messagePrinted() ) System.exit( 1 );

		for( String compressionFlag: jsapResult.getStringArray( "comp" ) ) {
			try {
				flags |= BVGraph.class.getField( compressionFlag ).getInt( BVGraph.class );
			}
			catch ( Exception notFound ) {
				throw new JSAPException( "Compression method " + compressionFlag + " unknown." );
			}
		}
		
		final int windowSize = jsapResult.getInt( "windowSize" );
		final int zetaK = jsapResult.getInt( "zetaK" );
		int maxRefCount = jsapResult.getInt( "maxRefCount" );
		if ( maxRefCount == -1 ) maxRefCount = Integer.MAX_VALUE;
		final int minIntervalLength = jsapResult.getInt( "minIntervalLength" );
		final int residualCompression = jsapResult.getInt( "residualCompression" );
		final int blocksCompression = jsapResult.getInt( "blocksCompression" );
		final boolean offline = jsapResult.getBoolean( "offline" );
		final boolean once = jsapResult.getBoolean( "once" );
		final boolean spec = jsapResult.getBoolean( "spec" );
		final boolean writeOffsets = jsapResult.getBoolean( "offsets" );
		final boolean list = jsapResult.getBoolean( "list" );
		final boolean degrees = jsapResult.getBoolean( "degrees" );
		graphClass = jsapResult.getClass( "graphClass" );
		source = jsapResult.getString( "sourceBasename" );
		dest = jsapResult.getString( "destBasename" ); 

		final ImmutableGraph graph;
		final ProgressLogger pl = new ProgressLogger( LOGGER, jsapResult.getLong( "logInterval" ), TimeUnit.MILLISECONDS );

		if ( graphClass != null ) {
			if ( spec ) {
				System.err.println( "Options --graph-class and --spec are incompatible" );
				System.exit( 1 );
			}
			if ( once ) graph = (ImmutableGraph)graphClass.getMethod( LoadMethod.ONCE.toMethod(), InputStream.class ).invoke( null, System.in );
			else if ( list || degrees || offline ) graph = (ImmutableGraph)graphClass.getMethod( LoadMethod.OFFLINE.toMethod(), CharSequence.class ).invoke( null, source );
			else graph = (ImmutableGraph)graphClass.getMethod( LoadMethod.SEQUENTIAL.toMethod(), CharSequence.class, ProgressLogger.class ).invoke( null, source, pl );
		}
		else {
			if ( !spec ) graph = once ? ImmutableGraph.loadOnce( System.in ) : offline ? ImmutableGraph.loadOffline( source, pl ) : ImmutableGraph.loadSequential( source, pl );
			else graph = ObjectParser.fromSpec( source, ImmutableGraph.class, GraphClassParser.PACKAGE );
		}

		if ( dest != null )	{
			if ( writeOffsets || list || degrees ) throw new IllegalArgumentException( "You cannot specify a destination graph with these options" );
			BVGraph.store( graph, dest, windowSize, maxRefCount, minIntervalLength, residualCompression, blocksCompression, zetaK, flags, pl );
		}
		else {
			if ( ! ( graph instanceof BVGraph ) ) throw new IllegalArgumentException( "The source graph is not a BVGraph" );
			final BVGraph bvGraph = (BVGraph)graph;
			if ( writeOffsets ) {
					final OutputBitStream offsets = new OutputBitStream( graph.basename() + OFFSETS_EXTENSION, 64 * 1024 );
					pl.expectedUpdates = graph.numNodes();
					pl.start( "Writing offsets..." );
					((BVGraph)graph).writeOffsets( offsets, pl );
					offsets.close();
					pl.count = graph.numNodes();
					pl.done();
			}
			if ( list ) {
				final InputBitStream offsets = new InputBitStream( graph.basename() + OFFSETS_EXTENSION );
				BinIO.storeObject( new EliasFanoMonotoneLongBigList( graph.numNodes() + 1, new File( graph.basename() + GRAPH_EXTENSION ).length() * Byte.SIZE + 1, new OffsetsLongIterator( bvGraph, offsets ) ), graph.basename() + OFFSETS_BIG_LIST_EXTENSION );
				offsets.close();
			}
			if ( degrees ) {
				final OutputBitStream outdegrees = new OutputBitStream( graph.basename() + OUTDEGREES_EXTENSION, 64 * 1024 );
				NodeIterator nodeIterator = graph.nodeIterator();
				for( int i = graph.numNodes(); i-- != 0; ) {
					nodeIterator.nextInt();
					outdegrees.writeGamma( nodeIterator.outdegree() );
				}
				
				outdegrees.close();
			}
		}
	}
}
