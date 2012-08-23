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

import org.apache.commons.lang.Validate;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

import com.useekm.indexing.internal.Indexer;
import com.useekm.indexing.internal.QueryEvaluator;
import com.useekm.indexing.internal.QueryEvaluatorUtil;


/**
 * An IndexingSail adds support for external index and search facilities to a wrapped {@link Sail}.
 * <p>
 * IndexingSail is tested with Sesame Native, Sesame Memory, and BigData, but should work with any {@link Sail} based Triple Store (including AllegroGraph, Mulgara, Sesame RDBMS,
 * and others).
 * 
 * @see Indexer
 * @see IndexingSailConnection
 * @see IndexerSettings
 */
/**
 * @author liangyu
 * 
 */
public class IndexingSail extends SailWrapper {
	/**
	 * A
	 * {@link SailConnection#addStatement(org.openrdf.model.Resource, URI, org.openrdf.model.Value, org.openrdf.model.Resource...)}
	 * with a predicate that starts with this prefix is interpreted as an
	 * {@link IndexingSail} specific comment. Such statements will not be added,
	 * but will result in special processing as indicated by the rest of the
	 * statement.
	 * 
	 * @see #CMD_REINDEX
	 */
	public static final String MAGIC_PREDICATE_PREFIX = "cmd:##";
	public static final String MAGIC_OPTION_PREFIX = "opt:##";
	/**
	 * Adding a statement with predicate <code>&lt;cmd:##reindex&gt;</code> will
	 * trigger a {@link IndexingSailConnection#reindex()} .
	 */
	public static final URI CMD_REINDEX = new URIImpl(MAGIC_PREDICATE_PREFIX
			+ "reindex");
	/**
	 * Using this as an argument to a function indicates that the filtering
	 * should be done by the {@link Indexer}.
	 */
	public static final URI ARG_BY_INDEX = new URIImpl(MAGIC_OPTION_PREFIX
			+ "byindex");
	/**
	 * Using this as an argument to a function indicates that the filter should
	 * not be done by the indexer, but by executing the filter function during
	 * evaluation of the SPARQL query.
	 */
	public static final URI ARG_BY_RDF = new URIImpl(MAGIC_OPTION_PREFIX
			+ "byrdf");

	// private final IndexerSettings settings;
	private final QueryEvaluator queryEvaluator;
	/**
	 * The indexmanager for the indexingsail
	 */
	private IndexManager manager = null;

	/**
	 * @param sail
	 *            The underlying RDF repository sail
	 * @param manager
	 *            The IndexManager for IndexingSail. Originally it is
	 *            indexingSetting, now it is replaced by the IndexManager
	 */
	public IndexingSail(Sail sail, IndexManager manager) {
		super(sail);
		this.manager = manager;
		this.queryEvaluator = QueryEvaluatorUtil.getEvaluator(sail);
	}

	@Override
	public IndexingSailConnection getConnection() throws SailException {
		Validate.notNull(this.manager);
		return new IndexingSailConnection(super.getConnection(), this.manager,
				getValueFactory(), queryEvaluator);
	}

	@Override
	public void initialize() throws SailException {
		super.initialize();
		manager.initialize();
	}

	/**
	 * @see Indexer#reindex()
	 * 
	 * @throws SailException
	 *             if the
	 */

}