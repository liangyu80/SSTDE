package edu.ncsa.sstde.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import com.useekm.indexing.internal.Indexer;

import edu.ncsa.sstde.util.DataTypeURI;
import edu.ncsa.sstde.util.GeoSPARQLVoc;


/**
 * This class is to help to find out the best correspondence between a
 * IndexGraph object and a set of statements, which could be extracted from a
 * SPARQL query
 * 
 * @author liangyu
 * 
 */
public class GraphAnalyzer {
	private IndexGraph indexingGraph = null;

	/**
	 * A {@link IndexGraph} is stored and later on used to compare against the
	 * input patterns
	 * 
	 * @param indexingGraph
	 * 
	 */
	public GraphAnalyzer(IndexGraph indexingGraph) {
		this.indexingGraph = indexingGraph;
	}

	/**
	 * 
	 * A matched indexed graph is used to search the patterns from a set of
	 * other candidate patterns. During the selection, the are usually some
	 * mapping between the predefined pattern and the input. It also stores the
	 * {@link FunctionCall}, {@link Compare}, etc. elements that are related to
	 * the variables in the selected statement patterns. *
	 * 
	 * @author liangyu
	 * 
	 */
	public class MatchedIndexedGraph {

		/**
		 * @param remainedStatements
		 *            The candidate patterns for selection
		 * @param selectedStatments
		 *            The patterns that have already been selected before.
		 * @param nameMappings
		 *            The nameMapping between the predefined patterns and the
		 *            target patterns
		 * @param varFilters
		 */
		private MatchedIndexedGraph(
				Collection<StatementPattern> remainedStatements,
				Collection<StatementPattern> selectedStatments,
				Map<String, String> nameMappings,
				Collection<VarFilter> varFilters) {
			this.remainedStatements = remainedStatements;
			this.selectedStatements = selectedStatments;
			this.nameMappings = nameMappings;
			this.varFilters = varFilters;
		}

		private Collection<StatementPattern> remainedStatements = null;
		private Collection<StatementPattern> selectedStatements = null;
		private Map<String, String> nameMappings = null;
		private Collection<VarFilter> varFilters = null;
		private Collection<FunctionCall> functionCalls = null;
		private Collection<Compare> compares = null;
		private Collection<Regex> regexs = null;
		private Collection<OrderElem> orders = null;
		private Collection<String> usedVarNames = null;
		private Indexer indexer = null;
		private long limit = -1;

		public long getLimit() {
			return limit;
		}

		public void setLimit(long limit) {
			this.limit = limit;
		}

		public Collection<String> getUsedVarNames() {
			return usedVarNames;
		}

		public void setUsedVarNames(Collection<String> usedVarNames) {
			this.usedVarNames = usedVarNames;
		}

		/**
		 * @return all the patterns that have been selected so far
		 */
		public Collection<StatementPattern> getSelectedStatements() {
			if (this.selectedStatements == null) {
				this.selectedStatements = new ArrayList<StatementPattern>();
			}
			return selectedStatements;
		}

		/**
		 * @return return all the "order" elements
		 */
		public Collection<OrderElem> getOrders() {
			if (this.orders == null) {
				this.orders = new ArrayList<OrderElem>();
			}
			return orders;
		}

		/**
		 * @param to
		 *            attach an indexer to this object, so that later on it can
		 *            used to send the actual query to
		 */
		public void setIndexer(Indexer indexer) {
			this.indexer = indexer;
		}

		/**
		 * @return get the attached indexer
		 */
		public Indexer getIndexer() {
			return indexer;
		}

		/**
		 * @return to get all the remained candidate statement patterns
		 */
		public Collection<StatementPattern> getRemainedStatements() {
			return remainedStatements;
		}

		/**
		 * @return to get the name mappings that have been collected so far
		 */
		public Map<String, String> getNameMappings() {
			return nameMappings;
		}

		/**
		 * @return the verse name mapping. The original mapping is from the
		 *         predefined patterns to the candidate patterns, while this is
		 *         in the inverse direction.
		 */
		public Map<String, String> getVerseNameMappings() {
			Map<String, String> result = new HashMap<String, String>();
			for (String key : this.nameMappings.keySet()) {
				result.put(this.nameMappings.get(key), key);
			}
			return result;
		}

		/**
		 * @return all the VarFilter objects that have been matched so far
		 * @see VarFilter
		 */
		public Collection<VarFilter> getVarFilters() {
			return varFilters;
		}

		/**
		 * @return alll the function calls that are related to variables in the
		 *         selected patterns
		 * @see org.openrdf.query.algebra.FunctionCall
		 */
		public Collection<FunctionCall> getFunctionCalls() {
			if (functionCalls == null) {
				functionCalls = new ArrayList<FunctionCall>();
			}
			return functionCalls;
		}

		/**
		 * @return all the compare filters that are related to the variables in
		 *         the selected patterns
		 * @see org.openrdf.query.algebra.Compare
		 */
		public Collection<Compare> getCompares() {
			if (compares == null) {
				compares = new ArrayList<Compare>();
			}
			return compares;
		}

		/**
		 * @return all the regex expressions that are defined in the selected
		 *         patterns
		 * @see org.openrdf.query.algebra.Regex
		 */
		public Collection<Regex> getRegexs() {
			if (regexs == null) {
				regexs = new ArrayList<Regex>();
			}
			return regexs;
		}

		/**
		 * To select patterns from the candidate patterns. Each selected pattern
		 * will form a new graph including it and all the previous patterns.
		 * They are like possible "branches" from the previous path
		 * 
		 * @param pattern
		 *            an input pattern that is used to compare against the
		 *            candidate patterns
		 * @return a collection of new matched graphs, each of which represents
		 *         a new possible path extending the previous path a step
		 *         further
		 */
		public Collection<MatchedIndexedGraph> select(StatementPattern pattern) {
			Collection<MatchedIndexedGraph> graphs = null;
			for (StatementPattern pattern2 : this.remainedStatements) {

				Map<String, String> nameMappingsCopy = null;
				Collection<VarFilter> varFiltersCopy = null;

				if (this.nameMappings == null) {
					nameMappingsCopy = new HashMap<String, String>();
				} else {
					nameMappingsCopy = new HashMap<String, String>(
							this.nameMappings);
				}

				if (this.varFilters == null) {
					varFiltersCopy = new ArrayList<GraphAnalyzer.VarFilter>();
				} else {
					varFiltersCopy = new ArrayList<GraphAnalyzer.VarFilter>(
							varFilters);
				}

				if (subsume(pattern, pattern2, nameMappingsCopy, varFiltersCopy)) {
					Collection<StatementPattern> remainPs = new ArrayList<StatementPattern>(
							this.remainedStatements);
					Collection<StatementPattern> selectedPatternCopy = null;
					if (this.selectedStatements == null) {
						selectedPatternCopy = new ArrayList<StatementPattern>();
					} else {
						selectedPatternCopy = new ArrayList<StatementPattern>(
								this.selectedStatements);
					}
					remainPs.remove(pattern2);
					selectedPatternCopy.add(pattern2);

					MatchedIndexedGraph graph = new MatchedIndexedGraph(
							remainPs, selectedPatternCopy, nameMappingsCopy,
							varFiltersCopy);
					if (graphs == null) {
						graphs = new ArrayList<GraphAnalyzer.MatchedIndexedGraph>();
					}
					graphs.add(graph);
				}
			}
			return graphs;
		}

		/**
		 * To decide if the statement pattern semantically subsume another
		 * statement. For example, {?s ?p ?o} subsume any statements, which {?s
		 * ssn:observedBy ?o} subsumes {ncsa:observation ssn:observedBy ?o}
		 * 
		 * @param pattern
		 *            the pattern expected to subsume another
		 * @param pattern2
		 *            the pattern expected to be subsumed by another
		 * @param nameMappings
		 *            if matched, then put the name mapping in this map. For
		 *            example, {?s ssn:observedBy ?o} subsumes {ncsa:observation
		 *            ssn:observedBy ?sensor}, the variable "o" in the first
		 *            pattern should be mapped to "sensor" in the second pattern
		 * @param varFilters
		 *            if matched, there are possible bindings between a variable
		 *            and a constant value, then put it in the collection.For
		 *            example, {?s ssn:observedBy ?o} subsumes {ncsa:observation
		 *            ssn:observedBy ?sensor}, the variable "s" in the first
		 *            pattern should be bond to "ncsa:observation" in the second
		 *            statement, which is a constant uri.
		 * @return
		 */
		private boolean subsume(StatementPattern pattern,
				StatementPattern pattern2, Map<String, String> nameMappings,
				Collection<VarFilter> varFilters) {
			return subsume(pattern.getSubjectVar(), pattern2.getSubjectVar(),
					nameMappings, varFilters)
					&& subsume(pattern.getPredicateVar(),
							pattern2.getPredicateVar(), nameMappings,
							varFilters)
					&& subsume(pattern.getObjectVar(), pattern2.getObjectVar(),
							nameMappings, varFilters);
		}

		/**
		 * The comparison between two statement patterns can be boiled down to
		 * comparison between their subjects, predicates and objects
		 * respectively.
		 * 
		 * @param var1
		 *            the Var object expected to subsume another
		 * @param var2
		 *            the Var object expected to be subsumed by another
		 * @param mappings
		 *            the collection of name mappings.
		 *            {@link #subsume(StatementPattern, StatementPattern, Map, Collection)}
		 * @param varFilters
		 *            the collection of all the variable-constant value
		 *            bindings.
		 *            {@link #subsume(StatementPattern, StatementPattern, Map, Collection)}
		 * @return
		 */
		private boolean subsume(Var var1, Var var2,
				Map<String, String> mappings, Collection<VarFilter> varFilters) {
			if (var1.hasValue()) {
				return var1.getValue().equals(var2.getValue());
			} else {
				if (var2.hasValue()) {
					varFilters.add(new VarFilter(var1.getName(), var2
							.getValue().stringValue()));
				} else {
					String existedValue = mappings.get(var1.getName());
					if (existedValue != null
							&& !existedValue.equals(var2.getName())) {
						return false;
					}
					mappings.put(var1.getName(), var2.getName());
				}
				return true;
			}
		}

		/**
		 * @return the score of itself. It will be used to decide which matched
		 *         pattern is the best one. In the implementation, we value the
		 *         matched {@link FunctionCall} the most, then {@link Compare}
		 *         and {@link Regex} filters, the {@link VarFilter} is valued
		 *         least.
		 */
		public int getScore() {
			return (this.varFilters == null ? 0 : this.varFilters.size())
					+ (this.functionCalls == null ? 0 : this.functionCalls
							.size()) * 1000
					+ (this.compares == null ? 0 : this.compares.size()) * 10
					+ (this.regexs == null ? 0 : this.regexs.size()) * 10;
		}
	}

	/**
	 * @author liangyu
	 * 
	 *         A VarFilter store the correspondence between a variable and a
	 *         constant value during the matching process, and later on is used
	 *         to add filters in a SQL query. For example, if the predefined
	 *         graph patter is {?o ssn:observedBy ?s}, and the query pattern is
	 *         {?o ssn:observedBy <http://.....#sensor1>}, then the varFilter is
	 *         [?s=<http://.....#sensor1>], later on it will be translated to a
	 *         SQL clause
	 */
	public class VarFilter {

		private String varName = null;
		private String value = null;

		public String getVarName() {
			return varName;
		}

		public String getValue() {
			return value;
		}

		public VarFilter(String varName, String value) {
			this.varName = varName;
			this.value = value;
		}
	}

	/**
	 * Extract the variable name from a {@link Compare}, a {@link FunctionCall},
	 * or an {@link OrderElem}
	 * 
	 * @param base
	 *            a query node that contains a variable
	 * @return the name of the variable
	 */
	private static String getVarName(QueryModelNodeBase base) {
		if (base instanceof BinaryValueOperator) {
			Var var = null;
			var = getVarFromCompare((BinaryValueOperator) base);
			if (var != null) {
				return var.getName();
			}
		} else if (base instanceof FunctionCall) {
			for (ValueExpr valueExp : ((FunctionCall) base).getArgs()) {
				if (valueExp instanceof Var && !((Var) valueExp).hasValue()) {
					return ((Var) valueExp).getName();
				}
			}
		} else if (base instanceof OrderElem) {
			// for(OrderElem elem: ((Order) base).getElements()){
			// Var var = (Var) elem.getExpr();
			return ((Var) ((OrderElem) base).getExpr()).getName();

		}
		return null;
	}

	/**
	 * A method dedicated to extract the variable name from a
	 * {@link BinaryValueOperator}.
	 * 
	 * @param base
	 * @return the variable name.
	 */
	private static Var getVarFromCompare(BinaryValueOperator base) {
		if (base.getLeftArg() instanceof Var) {
			return (Var) base.getLeftArg();
		} else if (base.getRightArg() instanceof Var) {
			return (Var) base.getRightArg();
		}
		return null;
	}

	/**
	 * collect all the {@link Compare}, {@link Regex} and {@link FunctionCall},
	 * and organize them into a map indexed by the variable names in them
	 * 
	 * @param tupleExpr
	 * @return A map of those query nodes indexed by the names of the variables
	 *         in them.
	 */
	public static Map<String, Collection<QueryModelNodeBase>> getFilters(
			TupleExpr tupleExpr) {
		final Map<String, Collection<QueryModelNodeBase>> result = new HashMap<String, Collection<QueryModelNodeBase>>();
		tupleExpr.visit(new QueryModelVisitorBase<RuntimeException>() {

			@Override
			public void meet(Compare node) throws RuntimeException {
				getVarFromNode(node);
			}

			@Override
			public void meet(FunctionCall node) throws RuntimeException {
				getVarFromNode(node);
			}

			@Override
			public void meet(OrderElem node) throws RuntimeException {
				getVarFromNode(node);
			}

			@Override
			public void meet(Regex node) throws RuntimeException {
				getVarFromNode(node);
			}

			private void getVarFromNode(QueryModelNodeBase node) {
				String varName = getVarName(node);
				if (varName != null) {
					if (result.get(varName) == null) {
						Collection<QueryModelNodeBase> constraints = new ArrayList<QueryModelNodeBase>();
						constraints.add(node);
						result.put(varName, constraints);
					} else {
						result.get(varName).add(node);
					}
				}
			}
		});

		return result;
	}

	/**
	 * To select the best matched graph from a {@link TupleExpr}, which
	 * indicating a SPARQL query.
	 * 
	 * @param tupleExpr
	 *            the input query expression.
	 * @return the best matched graph.
	 */
	public MatchedIndexedGraph selectBestMatchedGraph(TupleExpr tupleExpr) {
		final Collection<StatementPattern> patterns = new ArrayList<StatementPattern>();
		tupleExpr.visit(new QueryModelVisitorBase<RuntimeException>() {
			@Override
			public void meet(StatementPattern node) throws RuntimeException {
				patterns.add(node);
			}

		});
		return analyzeQuery(patterns, getFilters(tupleExpr));
	}

	/**
	 * To select the best matched graph from a set of candidate statement
	 * patterns, and the constraints imposed on the variables in those patterns.
	 * 
	 * @param patterns
	 * @param constraints
	 * @return the best matched pattern.
	 */
	private MatchedIndexedGraph analyzeQuery(
			Collection<StatementPattern> patterns,
			Map<String, Collection<QueryModelNodeBase>> constraints) {
		// Map<String, LiteralDef> literalMap = this.indexingGraph
		// .getLiteralDefMap();
		Collection<MatchedIndexedGraph> graphs = filterGraph(patterns);

		int maxScore = 0;
		MatchedIndexedGraph bestGraph = null;
		for (MatchedIndexedGraph graph : graphs) {
			for (String key : graph.nameMappings.keySet()) {
				String var = graph.nameMappings.get(key);
				Collection<QueryModelNodeBase> cons = constraints.get(var);
				if (cons != null) {
					for (QueryModelNodeBase queryModelNodeBase : cons) {
						if (queryModelNodeBase instanceof FunctionCall) {
							FunctionCall call = (FunctionCall) queryModelNodeBase;
							if (matchFunctionType(call, key)) {
								graph.getFunctionCalls().add(call);
							}
						} else if (queryModelNodeBase instanceof Compare) {
							if (DataTypeURI.isNumeric(this.indexingGraph
									.getLiteralDefMap().get(key).getType())) {
								graph.getCompares().add(
										(Compare) queryModelNodeBase);
							}
						} else if (queryModelNodeBase instanceof Regex) {
							if (DataTypeURI.isText(this.indexingGraph
									.getLiteralDefMap().get(key).getType())) {
								graph.getRegexs().add(
										(Regex) queryModelNodeBase);
							}
						} else if (queryModelNodeBase instanceof OrderElem) {
							graph.getOrders().add(
									(OrderElem) queryModelNodeBase);
						}
					}
				}
			}

			int score = graph.getScore();
			if (score > maxScore) {
				maxScore = score;
				bestGraph = graph;
			}
		}
		return bestGraph;

	}

	/**
	 * To decide if a {@link FunctionCall} uses the correct data type that
	 * defined in the {@link LiteralDef}, which are read from the configuration
	 * XML files.
	 * 
	 * @param call
	 *            the input {@link FunctionCall}
	 * @param var
	 *            the variable name.
	 * @return if the {@link FunctionCall} uses the correct data type.
	 */
	private boolean matchFunctionType(FunctionCall call, String var) {
		
		return DataTypeURI.isGeometry(this.indexingGraph.getLiteralDefMap()
				.get(var).getType())
				&& GeoSPARQLVoc.isGeoSPARQLPred(call.getURI());
	}

	/**
	 * To filter out all the possible matched graphs from the candidate
	 * statement patterns.
	 * 
	 * @param targets
	 *            the candidate statement patterns.
	 * @return the collection of all possibly matched patterns.
	 */
	private Collection<MatchedIndexedGraph> filterGraph(
			Collection<StatementPattern> targets) {

		MatchedIndexedGraph startGraph = new MatchedIndexedGraph(targets, null,
				null, null);
		Collection<MatchedIndexedGraph> graphs = new ArrayList<GraphAnalyzer.MatchedIndexedGraph>();
		graphs.add(startGraph);
		for (StatementPattern pattern : this.indexingGraph.getPatterns()) {
			Collection<MatchedIndexedGraph> newgraphs = new ArrayList<GraphAnalyzer.MatchedIndexedGraph>();
			for (MatchedIndexedGraph graph : graphs) {
				Collection<MatchedIndexedGraph> selectedGraphs = graph
						.select(pattern);
				if (selectedGraphs != null) {
//					newgraphs.addAll(graph.select(pattern));
					newgraphs.addAll(selectedGraphs);
				}
			}
			graphs = newgraphs;
		}
		return graphs;
	}

}
