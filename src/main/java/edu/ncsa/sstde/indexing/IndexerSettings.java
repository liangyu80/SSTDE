/*
 * Copyright 2010 by TalkingTrends (Amsterdam, The Netherlands)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensahara.com/licenses/apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.ncsa.sstde.indexing;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.useekm.indexing.exception.IndexException;
import com.useekm.indexing.internal.Indexer;

/**
 * Specifies the type and configuration of {@link Indexer}s for an
 * {@link IndexingSail}/{@link IndexingSailConnection}.
 */
public interface IndexerSettings {
	/**
	 * Returns an {@link IndexingSailConnection} specific {@link Indexer}. Note
	 * that an {@link Indexer} is not thread-safe, hence every
	 * {@link IndexingSailConnection} should have its own {@link Indexer} (which
	 * is ensured by {@link IndexingSail}, so unless you subclass
	 * {@link IndexingSail} or {@link IndexerSettings} there is no need to worry
	 * about this).
	 */
	Indexer createIndexer();

	IndexGraph getIndexGraph();

	/**
	 * @return If the {@link Indexer} can compute the results of this
	 *         functionCall (including bindings for all statement-patterns that
	 *         use this result) the method returns the variable that will be
	 *         bound as a result of this filter function. Returns null if the
	 *         call is not supported.
	 */
	Var getResultVarFromFunctionCall(URI function, List<ValueExpr> args)
			throws IndexException;

	/**
	 * TODO: <strong>note this assumes all statements with that predicate being
	 * indexed, which is currently not enforced, but things will break if that
	 * is not the behavior!</strong>
	 * 
	 * @param predicate
	 *            The predicate to check for being indexed.
	 * @return true if statements with a predicate equal to the argument are
	 *         being indexed, false if not.
	 */
	boolean isPredicateIndexed(URI predicate);

	/**
	 * Initializes the Indexing Sail functionality.
	 * 
	 * @throws IndexException
	 *             If initialization fails.
	 */
	void initialize();

	void initProperties(Properties properties);

	/**
	 * @return all the statement patterns defined in its "IndexGraph"
	 */
	public Collection<StatementPattern> getMatchSatatments();

	/**
	 * @return all the variables defined in the statement patterns
	 */
	Collection<Var> getIndexedVars();
}
