/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package edu.ncsa.sstde.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStatistics;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

import com.useekm.indexing.algebra.indexer.AbstractIdxQuery;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * 
 * To optimize the sequence of a TupleQuery. The Sesame Native Store does the
 * optimization before any query, but for other repositories, it is necessary to
 * use this optimizer before executing the query
 * 
 * @author liangyu
 * 
 */
public class IndexedJoinOptimizer implements QueryOptimizer {

	protected final EvaluationStatistics statistics;

	public IndexedJoinOptimizer() {
		this(new EvaluationStatistics());
	}

	public IndexedJoinOptimizer(EvaluationStatistics statistics) {
		this.statistics = statistics;
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset,
			BindingSet bindings) {
		tupleExpr.visit(new JoinVisitor());
	}

	protected static class StatementPatternCollector2 extends
			StatementPatternCollector {
		public static List<StatementPattern> process(QueryModelNode node) {
			if (node instanceof IndexerExpr) {
				return null;
			} else {
				return StatementPatternCollector.process(node);
			}
		}
	}

	protected class JoinVisitor extends QueryModelVisitorBase<RuntimeException> {

		Set<String> boundVars = new HashSet<String>();

		@Override
		public void meet(LeftJoin leftJoin) {
			leftJoin.getLeftArg().visit(this);

			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);
				boundVars.addAll(leftJoin.getLeftArg().getBindingNames());

				leftJoin.getRightArg().visit(this);
			} finally {
				boundVars = origBoundVars;
			}
		}

		@Override
		public void meet(Join node) {
			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);

				// Recursively get the join arguments
				List<TupleExpr> joinArgs = getJoinArgs(node,
						new ArrayList<TupleExpr>());

				// Build maps of cardinalities and vars per tuple expression
				Map<TupleExpr, Double> cardinalityMap = new HashMap<TupleExpr, Double>();
				Map<TupleExpr, List<Var>> varsMap = new HashMap<TupleExpr, List<Var>>();

				for (TupleExpr tupleExpr : joinArgs) {
					cardinalityMap.put(tupleExpr,
							statistics.getCardinality(tupleExpr));
					varsMap.put(tupleExpr, getStatementPatternVars(tupleExpr));
				}

				// Build map of var frequences
				Map<Var, Integer> varFreqMap = new HashMap<Var, Integer>();
				for (List<Var> varList : varsMap.values()) {
					getVarFreqMap(varList, varFreqMap);
				}

				// Reorder the (recursive) join arguments to a more optimal
				// sequence
				List<TupleExpr> orderedJoinArgs = new ArrayList<TupleExpr>(
						joinArgs.size());
				while (!joinArgs.isEmpty()) {
					TupleExpr tupleExpr = selectNextTupleExpr(joinArgs,
							cardinalityMap, varsMap, varFreqMap, boundVars);

					joinArgs.remove(tupleExpr);
					orderedJoinArgs.add(tupleExpr);

					// Recursively optimize join arguments
					tupleExpr.visit(this);

					boundVars.addAll(tupleExpr.getBindingNames());
				}

				// Build new join hierarchy
				// Note: generated hierarchy is right-recursive to help the
				// IterativeEvaluationOptimizer to factor out the left-most join
				// argument
				int i = orderedJoinArgs.size() - 1;
				TupleExpr replacement = orderedJoinArgs.get(i);
				for (i--; i >= 0; i--) {
					replacement = new Join(orderedJoinArgs.get(i), replacement);
				}

				// Replace old join hierarchy
				node.replaceWith(replacement);
			} finally {
				boundVars = origBoundVars;
			}
		}

		protected <L extends List<TupleExpr>> L getJoinArgs(
				TupleExpr tupleExpr, L joinArgs) {
			if (tupleExpr instanceof Join) {
				Join join = (Join) tupleExpr;
				getJoinArgs(join.getLeftArg(), joinArgs);
				getJoinArgs(join.getRightArg(), joinArgs);
			} else {
				joinArgs.add(tupleExpr);
			}

			return joinArgs;
		}

		protected List<Var> getStatementPatternVars(TupleExpr tupleExpr) {
			List<Var> varList = null;
			if (tupleExpr instanceof IndexerExpr) {
				IndexerExpr indexerExpr = (IndexerExpr) tupleExpr;
				varList = new ArrayList<Var>();
				if (indexerExpr.getQuery() instanceof AbstractIdxQuery) {
					AbstractIdxQuery abstractIdxQuery = (AbstractIdxQuery) indexerExpr
							.getQuery();
					// varList.add(abstractIdxQuery.getSubjectVar());
					// varList.add(abstractIdxQuery.getPredicateVar());
					// varList.add(abstractIdxQuery.getObjectVar());
					varList.addAll(abstractIdxQuery.getVars());
				}
				// varList = new ArrayList<Var>();
				// varList.add(indexerExpr.getqu)
			} else {
				List<StatementPattern> stPatterns = StatementPatternCollector
						.process(tupleExpr);
				varList = new ArrayList<Var>(stPatterns.size() * 4);
				for (StatementPattern sp : stPatterns) {
					sp.getVars(varList);
				}
			}
			return varList;
		}

		protected <M extends Map<Var, Integer>> M getVarFreqMap(
				List<Var> varList, M varFreqMap) {
			for (Var var : varList) {
				Integer freq = varFreqMap.get(var);
				freq = (freq == null) ? 1 : freq + 1;
				varFreqMap.put(var, freq);
			}
			return varFreqMap;
		}

		/**
		 * Selects from a list of tuple expressions the next tuple expression
		 * that should be evaluated. This method selects the tuple expression
		 * with highest number of bound variables, preferring variables that
		 * have been bound in other tuple expressions over variables with a
		 * fixed value.
		 */
		protected TupleExpr selectNextTupleExpr(List<TupleExpr> expressions,
				Map<TupleExpr, Double> cardinalityMap,
				Map<TupleExpr, List<Var>> varsMap,
				Map<Var, Integer> varFreqMap, Set<String> boundVars) {
			double lowestCardinality = Double.MAX_VALUE;
			TupleExpr result = null;

			for (TupleExpr tupleExpr : expressions) {
				// Calculate a score for this tuple expression
				double cardinality = getTupleExprCardinality(tupleExpr,
						cardinalityMap, varsMap, varFreqMap, boundVars);

				if (cardinality < lowestCardinality) {
					// More specific path expression found
					lowestCardinality = cardinality;
					result = tupleExpr;
				}
			}

			return result;
		}

		protected boolean containVarNames(List<Var> unboundVars,
				Set<String> boundVars) {

			for (Var var : unboundVars) {
				if (boundVars.contains(var.getName())) {
					return true;
				}
			}

			return false;
		}

		protected double getTupleExprCardinality(TupleExpr tupleExpr,
				Map<TupleExpr, Double> cardinalityMap,
				Map<TupleExpr, List<Var>> varsMap,
				Map<Var, Integer> varFreqMap, Set<String> boundVars) {
			double cardinality = cardinalityMap.get(tupleExpr);

			List<Var> vars = varsMap.get(tupleExpr);

			List<Var> variables = getVariable(vars);
			if (variables != null && boundVars.size() > 0
					&& !containVarNames(variables, boundVars)) {
				return Double.MAX_VALUE;
			}

			// Compensate for variables that are bound earlier in the evaluation
			List<Var> unboundVars = getUnboundVars(vars);
			List<Var> constantVars = getConstantVars(vars);
			int nonConstantVarCount = vars.size() - constantVars.size();
			if (nonConstantVarCount > 0) {
				double exp = (double) unboundVars.size() / nonConstantVarCount;
				cardinality = Math.pow(cardinality, exp);
			}

			if (unboundVars.isEmpty()) {
				// Prefer patterns with more bound vars
				if (nonConstantVarCount > 0) {
					cardinality /= nonConstantVarCount;
				}
			} else {
				// Prefer patterns that bind variables from other tuple
				// expressions
				int foreignVarFreq = getForeignVarFreq(unboundVars, varFreqMap);
				if (foreignVarFreq > 0) {
					cardinality /= foreignVarFreq;
				}
			}

			// Prefer patterns that bind more variables
			// List<Var> distinctUnboundVars = getUnboundVars(new
			// HashSet<Var>(vars));
			// if (distinctUnboundVars.size() >= 2) {
			// cardinality /= distinctUnboundVars.size();
			// }

			return cardinality;
		}

		protected List<Var> getConstantVars(Iterable<Var> vars) {
			List<Var> constantVars = new ArrayList<Var>();

			for (Var var : vars) {
				if (var.hasValue()) {
					constantVars.add(var);
				}
			}

			return constantVars;
		}

		protected List<Var> getUnboundVars(Iterable<Var> vars) {
			List<Var> unboundVars = new ArrayList<Var>();

			for (Var var : vars) {
				if (!var.hasValue() && !this.boundVars.contains(var.getName())) {
					unboundVars.add(var);
				}
			}

			return unboundVars;
		}

		protected List<Var> getVariable(Iterable<Var> vars) {
			List<Var> unboundVars = new ArrayList<Var>();

			for (Var var : vars) {
				if (!var.hasValue()) {
					unboundVars.add(var);
				}
			}

			return unboundVars;
		}

		protected int getForeignVarFreq(List<Var> ownUnboundVars,
				Map<Var, Integer> varFreqMap) {
			int result = 0;

			Map<Var, Integer> ownFreqMap = getVarFreqMap(ownUnboundVars,
					new HashMap<Var, Integer>());

			for (Map.Entry<Var, Integer> entry : ownFreqMap.entrySet()) {
				Var var = entry.getKey();
				int ownFreq = entry.getValue();
				result += varFreqMap.get(var) - ownFreq;
			}

			return result;
		}
	}
}
