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
package edu.ncsa.sstde.indexing.postgis;

import info.aduna.iteration.CloseableIteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

import com.useekm.indexing.postgis.IndexedStatement;

import edu.ncsa.sstde.indexing.LiteralDef;
import edu.ncsa.sstde.util.DateFormatter;

public class PostgisIteration implements
		CloseableIteration<BindingSet, QueryEvaluationException> {
	private PreparedStatement ps;
	private ResultSet results;
	private boolean hasNext;
	private boolean closed;
	private int toFetchsize = -1;
	private int limitIndex;
//	private int fetchTimes = 0;
	private int fetchedSize = 0;
	private ValueFactory valueFactory;
	// List<? extends ResultBinding> bindings;
//	private Map<String, String> nameMapping;
	private Map<String, LiteralDef> literalMaps;
	private ValueCreator[] creators;


	public PostgisIteration(ValueFactory valueFactory, PreparedStatement ps,
			Map<String, String> nameMapping, PostgisIndexerSettings settings, int fetchsize, int limitiIndex )
			throws SQLException {
		this.toFetchsize = fetchsize + 1;
		this.limitIndex = limitiIndex;
		ps.setInt(this.limitIndex, this.toFetchsize);
		init(valueFactory, ps, nameMapping, settings);
	}

	public PostgisIteration(ValueFactory valueFactory, PreparedStatement ps,
			Map<String, String> nameMapping, PostgisIndexerSettings settings)
			throws SQLException {

		// ------------------------------------------------------
		init(valueFactory, ps, nameMapping, settings);

		// this.bindings = bindings;
	}
	
	private void init(ValueFactory valueFactory, PreparedStatement ps,
			Map<String, String> nameMapping, PostgisIndexerSettings settings)
			throws SQLException {
		// long t1 = System.currentTimeMillis();
		this.results = ps.executeQuery();
		// int count =0;
		// for (;results.next();count++) {
		// String a = results.getString("observation");
		// }
		// long t2 = System.currentTimeMillis();
		// System.out.println("==============================" + count + ", " +
		// (t2-t1));

		// ------------------------------------------------------

//		this.nameMapping = nameMapping;
		this.literalMaps = settings.getIndexGraph().getLiteralDefMap();

		try {
			// this.results.setFetchSize(settings.getFetchSize());
			// System.out.println(ps.toString());
			this.hasNext = results.next();
		} catch (SQLException e) {
			internalQuietClose();
			throw e;
		}
		this.ps = ps; // after try-block, because caller is responsible for
						// closing ps if an exception is thrown
		this.valueFactory = valueFactory;

		ResultSetMetaData metaData = results.getMetaData();
		int columns = metaData.getColumnCount();
		this.creators = new ValueCreator[columns];

		for (int i = 0; i < columns; i++) {
			String columnName = metaData.getColumnName(i + 1);
			LiteralDef literalDef = literalMaps.get(columnName);
			if (literalDef == null) {
				this.creators[i] = new URICreator(nameMapping.get(columnName));
			} else {
				this.creators[i] = new LiteralCreator(literalDef.getType(),
						nameMapping.get(columnName));
			}
		}
	}

	@Override
	public void close() throws QueryEvaluationException {
		try {
			internalClose();
		} catch (SQLException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		boolean result = !closed && hasNext;
		if (!result && !closed)
			close();
		return result;
	}

	private interface ValueCreator {
		public Value createValue(ResultSet resultSet, int i)
				throws SQLException;

		public String getName();
	}

	private class URICreator implements ValueCreator {
		private String varName = null;

		public URICreator(String string) {
			this.varName = string;
		}

		@Override
		public String getName() {
			return varName;
		}

		@Override
		public Value createValue(ResultSet resultSet, int i)
				throws SQLException {
			return valueFactory.createURI(resultSet.getString(i));
		}
	}

	private class LiteralCreator implements ValueCreator {

		private String typeURI;
		private String varName;

		public LiteralCreator(String typeURI, String varName) {
			this.typeURI = typeURI;
			this.varName = varName;
		}

		@Override
		public String getName() {
			return varName;
		}

		@Override
		public Value createValue(ResultSet resultSet, int i)
				throws SQLException {
			Object object = resultSet.getObject(i);
			if (object instanceof Timestamp) {
				return valueFactory.createLiteral(DateFormatter.getInstance().format(object),
						typeURI);
			}
			return valueFactory.createLiteral(object.toString(), typeURI);
		}

	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		if (!hasNext || closed)
			throw new NoSuchElementException();
		try {
			QueryBindingSet result = new QueryBindingSet(this.creators.length);
			for (int i = 0; i < this.creators.length; i++) {
				ValueCreator creator = creators[i];

				result.addBinding(creator.getName(),
						creator.createValue(results, i + 1));
			}
			fetchedSize ++;
			hasNext = results.next();
			if (!hasNext && toFetchsize > 0) {
				fetchNext();
			}
			if (!hasNext)
				internalClose();
			return result;

		} catch (SQLException e) {
			internalQuietClose(); // Make sure the iteration is closed even if
									// using code does not close the iteration
									// properly
			throw new QueryEvaluationException(e);
		}
	}

	private void fetchNext() throws SQLException {
		
//		ps.setInt(this.limitIndex, ((int) (Math.pow(2, ++fetchTimes)) * toFetchsize));
		if (fetchedSize == this.toFetchsize) {
			ps.setInt(limitIndex, Integer.MAX_VALUE);
			results.close();
			results = ps.executeQuery();	
			hasNext = results.absolute(fetchedSize + 1);
			
		}
		
//		if (fetchedSize == ((int) (Math.pow(2, fetchTimes)) * toFetchsize )) {
//			ps.setInt(this.limitIndex, ((int) (Math.pow(2, ++fetchTimes)) * toFetchsize));
//			results.close();
//			results = ps.executeQuery();	
//			hasNext = results.absolute(fetchedSize + 1);
//		}
	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new UnsupportedOperationException();

	}

	private void internalQuietClose() {
		try {
			if (!closed) {
				IndexedStatement.closeQuietly(results);
				IndexedStatement.closeQuietly(ps);
			}
		} finally {
			closed = true;
		}
	}

	void internalClose() throws SQLException {
		try {
			if (!closed) {
				try {
					results.close();
				} finally {
					ps.close();
				}
			}
		} finally {
			closed = true;
		}
	}

}
