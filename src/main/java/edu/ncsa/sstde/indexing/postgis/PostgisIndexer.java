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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.Validate;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.postgis.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.useekm.indexing.algebra.ConstraintOptimizer;
import com.useekm.indexing.algebra.OrderByOptimizer;
import com.useekm.indexing.algebra.indexer.AbstractIdxQuery;

import com.useekm.indexing.algebra.indexer.IdxJoin;
import com.useekm.indexing.algebra.indexer.IdxOrder;
import com.useekm.indexing.algebra.indexer.IdxOrder.OrderInfo;
import com.useekm.indexing.algebra.indexer.IdxQuery;
import com.useekm.indexing.exception.IndexException;
import com.useekm.indexing.internal.AbstractIndexer;
import com.useekm.indexing.internal.Indexer;
import com.useekm.indexing.postgis.IndexedStatement;


import edu.ncsa.sstde.indexing.LiteralDef;
import edu.ncsa.sstde.indexing.GraphAnalyzer.MatchedIndexedGraph;
import edu.ncsa.sstde.indexing.GraphAnalyzer.VarFilter;
import edu.ncsa.sstde.indexing.algebra.IndexerExpr;
import edu.ncsa.sstde.util.DataTypeURI;
import edu.ncsa.sstde.util.DateFormatter;

/**
 * {@link Indexer} for a Postgres/Postgis backed index.
 * <p>
 * This indexer has support for geospatial and free text indexing and searches.
 * It can also (optionally) use any other statement for moving limiting joins on
 * index searches from the SparQL evaluator to the Indexe evaluator. There is
 * <strong>no</strong> support for indexing statements that have a {@link BNode}
 * at the subject position.
 * 
 * @see Indexer
 * @see PostgisIndexerSettings
 */
public class PostgisIndexer extends AbstractIndexer {
	// Note that some SQL variables are always inlined in the query, instead of
	// being passed as an out-of-line parameter.
	// The reason for this is the way that Potgres optimizes (creates a query
	// plan) for queries. A full explanation can be found
	// here:
	// http://postgresql.1045698.n5.nabble.com/JDBC-prepared-queries-and-partitioning-tt2173740.html#a2173741
	// Summary:
	// Since we don't reuse the PreparedStatement, we allways get an unnamed
	// statement from the JDBC driver.
	// 'Unnamed' indicates to the server that it should use the first set of
	// parameters it gets to construct the best
	// plan for those parameters. However, since the server doesn't know that we
	// won't reuse the parameters, it will
	// create a query plan that is valid for all parameter values. This means
	// some optimizations can not be performed,
	// such as constraint exclusion. Since we want constraint exclusion to work
	// for partitions, and we partition based
	// on predicate, literal type and literal language, we will always send
	// those values inline when they are known.
	// See also #40.
	private static final Logger LOG = LoggerFactory
			.getLogger(PostgisIndexer.class);
	private static final char EQ = '=';
	private static final String SELECT = "SELECT";
	private static final String AND = " AND ";
	private static final String WHERE = " WHERE ";
	private static final String ORDER_BY = " ORDER BY ";
	private static final String DESC = " DESC ";
	private static final String ASC = " ASC ";
	private static final String FROM = " FROM ";
	private static final String ST_PREFIX = "ST_";

	private MessageDigest DIGEST;
	private PreparedStatement insertStatement;
	private PreparedStatement removeStatement;
	private String[] varNames = null;

	private final PostgisIndexerSettings settings;
	private Connection connection;
	private Map<String, List<IndexedStatement>> addedStatements;
	private String name = null;
//	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	// private HashSet<String> batchToAdd = new HashSet<String>();
	// private HashSet<String> batchToRemove = new HashSet<String>();

	protected PostgisIndexer(PostgisIndexerSettings settings) {
		this.settings = settings;
		try {
			DIGEST = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostgisIndexerSettings getSettings() {
		return settings;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The optional ctx parameter is currently ignored.
	 */

	private String[] getVarNames() {
		if (this.varNames == null) {
			Object[] vars = this.getSettings().getIndexGraph().getVarNames()
					.toArray();
			this.varNames = new String[vars.length];
			for (int i = 0; i < vars.length; i++) {
				this.varNames[i] = vars[i].toString();
			}

		}

		return this.varNames;

	}

	private int[] SQLTypes = null;

	private int[] getSQLTypes() {
		if (this.SQLTypes == null) {
			this.SQLTypes = getSQLTypes(getVarNames());
		}
		return SQLTypes;
	}

	//
	// private boolean addIndex(SailConnection connection, Resource subj, URI
	// pred,
	// Value obj, Resource... ctx) {
	//
	// Map<String, Collection<StatementPattern>> patternMap = this
	// .getSettings().getIndexGraph().getStatementPatternMap();
	//
	// Collection<StatementPattern> patterns = new HashSet<StatementPattern>();
	// Collection<StatementPattern> p1 = patternMap.get(subj.stringValue());
	// if (p1 != null) {
	// patterns.addAll(p1);
	// }
	// Collection<StatementPattern> p2 = patternMap.get(pred.stringValue());
	// if (p2 != null) {
	// patterns.addAll(p2);
	// }
	// Collection<StatementPattern> p3 = patternMap.get(obj.stringValue());
	// if (p3 != null) {
	// patterns.addAll(p3);
	// }
	//
	// if (patterns.size() > 0) {
	// try {
	// PreparedStatement statement = getInsertStatment();
	// boolean oldAutoCommit = statement.getConnection()
	// .getAutoCommit();
	// statement.getConnection().setAutoCommit(true);
	// // HashSet<String> newGraphIDs = new HashSet<String>();
	// for (StatementPattern pattern : patterns) {
	//
	// QueryBindingSet bindingSet = new QueryBindingSet(2);
	// if (!pattern.getSubjectVar().hasValue()) {
	// bindingSet.addBinding(
	// pattern.getSubjectVar().getName(), subj);
	// }
	// if (!pattern.getPredicateVar().hasValue()) {
	// bindingSet.addBinding(pattern.getPredicateVar()
	// .getName(), pred);
	// }
	// if (!pattern.getObjectVar().hasValue()) {
	// bindingSet.addBinding(pattern.getObjectVar().getName(),
	// obj);
	// }
	// DIGEST.reset();
	//
	// if (this.getSettings().getIndexGraph().getPatterns().size() == 1) {
	// setInsertValue(this.getVarNames(),
	// this.getSQLTypes(), statement, bindingSet);
	// try {
	// statement.executeUpdate();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// } else {
	// CloseableIteration<? extends BindingSet, QueryEvaluationException>
	// iterator = connection
	// .evaluate(this.getSettings().getIndexGraph()
	// .getTupleQuery(), null, bindingSet,
	// false);
	//
	//
	// // int batchsize = this.getSettings().getBatchSize();
	// // long count = 0;
	//
	// for (; iterator.hasNext(); ) {
	// BindingSet resultBinding = iterator.next();
	// String md5 = setInsertValue(this.getVarNames(),
	// this.getSQLTypes(), statement,
	// resultBinding);
	// if (!batchToAdd.contains(md5)) {
	// batchToAdd.add(md5);
	// try {
	// statement.executeUpdate();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	//
	// }
	// statement.getConnection().setAutoCommit(oldAutoCommit);
	// } catch (SQLException e1) {
	// e1.printStackTrace();
	// } catch (SailException e) {
	// e.printStackTrace();
	// } catch (QueryEvaluationException e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// return false;
	// }

	// private boolean addIndex(Value subj, Value pred, Value obj, pattern){
	//
	// }



	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleExpr optimize(TupleExpr tupleExpr, Dataset dataset,
			BindingSet bindings) {
		new ConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
		super.optimize(tupleExpr, dataset, bindings);
		new OrderByOptimizer().optimize(tupleExpr, dataset, bindings);
		return tupleExpr;
	}

	// private void addIndexes(IndexingSailConnection sailConn)
	// throws SailException, SQLException {
	// int reindexBatchSize = settings.getReindexBatchSize();
	// CloseableIteration<? extends Statement, SailException> sts = sailConn
	// .getStatements((Resource) null, null, null, false);
	// try {
	// addIndexes(sts, reindexBatchSize);
	// sts.close();
	// sts = null;
	// } finally {
	// SesameUtil.closeQuietly(sts);
	// }
	// }

	// private void addIndexes(
	// CloseableIteration<? extends Statement, SailException> sts,
	// int reindexBatchSize) throws SQLException, SailException {
	// int count = 0;
	// while (sts.hasNext()) {
	// Statement st = sts.next();
	// if (addIndex(st.getSubject(), st.getPredicate(), st.getObject(),
	// st.getContext())) {
	// ++count;
	// if (count % reindexBatchSize == 0) {
	// LOG.info("Added {} statements to index", count);
	// commit();
	// }
	// }
	// }
	// LOG.info("Added {} statements to index", count);
	// commit();
	// }

	/**
	 * @return A hibernate session object for storing an
	 *         {@link IndexedStatement}. Creates a new session ands starts a
	 *         transaction on that session if no session was started yet.
	 */
	protected Connection getConnection() {
		if (connection == null) {
			// System.out.println("conenction initialized ");
			Connection newConnection = null;
			try {
				newConnection = settings.getDataSource().getConnection();
				newConnection.setAutoCommit(false);
			} catch (SQLException e) {
				IndexedStatement.closeQuietly(newConnection);
				throw new IndexException("Could not get a database connection",
						e);
			}
			connection = newConnection;
		}
		return connection;
	}

	Connection getConnectionInternal() {
		return connection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() {
		try {
			flushInternal();
		} catch (SQLException e) {
			throw new IndexException("Flush error", e);
		}
	}

	void flushInternal() throws SQLException {
		if (addedStatements == null)
			return;
		try {
			for (Map.Entry<String, List<IndexedStatement>> perTable : addedStatements
					.entrySet()) {
				PreparedStatement ps = IndexedStatement.addPrepare(
						getConnection(), perTable.getKey());
				try {
					for (IndexedStatement stat : perTable.getValue())
						IndexedStatement.addNewBatch(ps, stat);
					ps.executeBatch();
					ps.close();
					ps = null;
				} finally {
					IndexedStatement.closeQuietly(ps);
				}
			}
		} finally {
			addedStatements = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() {
		try {
			flush();
			Connection conn = getConnectionInternal();
			if (conn != null)
				conn.commit();
		} catch (SQLException e) {
			throw new IndexException("Commit error", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() {
		try {
			addedStatements = null;
			Connection conn = getConnectionInternal();
			if (conn != null)
				conn.rollback();
		} catch (SQLException e) {
			throw new IndexException("Rollback error", e);
		}
	}

	@Override
	public void close() {
		try {
			rollback();
			if (connection != null)
				connection.close();
			connection = null;
		} catch (SQLException e) {
			throw new IndexException(e);
		} finally {
			IndexedStatement.closeQuietly(connection);
			connection = null;
		}
	}

	/**
	 * @return An iterator over the result bindings of the provided expression.
	 */
	@Override
	public PostgisIteration iterator(ValueFactory valueFactory,
			IndexerExpr expr, BindingSet sparqlBindings)
			throws QueryEvaluationException {

		// System.out.println("till postgisindexer, time = " +
		// (System.currentTimeMillis() - Timer.time));
		// IdxQuery idxQuery = expr.getQuery();
		SqlQueryBuilder builder = new SqlQueryBuilder();
		PostgisIteration result = null;
		PreparedStatement ps = null;
		try {
			// flushInternal(); // make sure the database is synchronized before
			// we
			// start a query
			asSql(expr.getGraph(), builder, sparqlBindings);
			// List<ResultBinding> resultBindings = new
			// ArrayList<ResultBinding>(
			// expr.getBindingNames().size());
			ps = createSqlQuery(builder);

			if (builder.limit > 0) {
				result = new PostgisIteration(valueFactory, ps, expr.getGraph()
						.getNameMappings(), settings, (int) builder.limit,
						builder.inputBindings.size() + 1);
			} else {
				result = new PostgisIteration(valueFactory, ps, expr.getGraph()
						.getNameMappings(), settings);

			}
			// result = new PostgisIteration(valueFactory, ps, resultBindings,
			// settings);

			return result;
		} catch (SQLException e) {
			throw new QueryEvaluationException(e);
		} finally {
			if (result == null)
				IndexedStatement.closeQuietly(ps);
		}
	}

	private void asSql(MatchedIndexedGraph graph, SqlQueryBuilder builder,
			BindingSet sparqlBindings) {
		StringBuffer from = new StringBuffer(SELECT);

		Map<String, String> verseNameMappings = graph.getVerseNameMappings();
		for (String varName : graph.getUsedVarNames()) {
			String name = verseNameMappings.get(varName);
			if (name != null) {
				from.append(' ').append(name).append(',');
			}

		}
		if (from.length() == SELECT.length()) {
			for (String varName : verseNameMappings.values()) {
				from.append(' ').append(varName).append(',');
			}
		}
		// for (String column : graph.getNameMappings().keySet()) {
		// from.append(' ').append(column).append(',');
		// }

		Map<String, String> verseMapping = verseNameMappings;
		from.deleteCharAt(from.length() - 1);

		from.append(FROM);
		from.append(this.getName());

		// compose the where clause
		StringBuffer where = new StringBuffer();

		for (FunctionCall call : graph.getFunctionCalls()) {
			URIImpl url = new URIImpl(call.getURI());
			where.append(ST_PREFIX).append(url.getLocalName()).append('(');

			for (int i = 0; i < call.getArgs().size(); i++) {
				ValueExpr param = call.getArgs().get(i);
				if (param instanceof Var) {
					where.append(verseMapping.get(((Var) param).getName()))
							.append(',');
				} else if (param instanceof ValueConstant) {
					where.append('?').append(',');
					builder.inputBindings.add(new Binding(Types.OTHER,
							parseLiteral((Literal) ((ValueConstant) param)
									.getValue())));
				}
			}
			where.deleteCharAt(where.length() - 1).append(")=true").append(AND);
		}

		for (Compare compare : graph.getCompares()) {
			addCompareWhere(where, compare, builder, verseMapping);
			where.append(AND);
		}

		for (VarFilter filter : graph.getVarFilters()) {
			where.append(filter.getVarName()).append("=?").append(AND);
			builder.inputBindings.add(new Binding(Types.VARCHAR, filter
					.getValue()));
			// System.out.println(filter);
		}

		for (@SuppressWarnings("unused") Regex regex : graph.getRegexs()) {

		}

		if (where.length() > 0) {
			where.delete(where.length() - 5, where.length() - 1);
		}

		// compose the order by clause
		StringBuffer orderby = new StringBuffer();
		if (graph.getOrders() != null && graph.getOrders() instanceof List) {
			List<OrderElem> orders = (List<OrderElem>) graph.getOrders();
			Map<String, LiteralDef> literalDefMap = this.getSettings().getIndexGraph().getLiteralDefMap();
			for (int i = graph.getOrders().size() - 1; i > -1; i--) {
				OrderElem order = orders.get(i);
				if (order.getExpr() instanceof Var) {
					String colName = verseMapping.get(((Var) order.getExpr())
							.getName());
					if (literalDefMap.get(colName) != null) {
						orderby.append(colName);
						orderby.append(order.isAscending() ? ASC : DESC);
						orderby.append(',');

					}
				}
			}
		}

//		for (OrderElem order : graph.getOrders()) {
//			if (order.getExpr() instanceof Var) {
//				orderby.append(verseMapping.get(((Var) order.getExpr())
//						.getName()));
//				orderby.append(order.isAscending() ? ASC : DESC);
//				orderby.append(',');
//			}
//
//		}

		if (orderby.length() > 0) {
			orderby.deleteCharAt(orderby.length() - 1);
		}

		// combine all the query segments
		if (where.length() > 0) {
			from.append(WHERE).append(where);
		}
		if (orderby.length() > 0) {
			from.append(ORDER_BY).append(orderby);
		}

		builder.setSQL(from.toString());
		builder.setLimit(graph.getLimit());
	}

	private void addCompareWhere(StringBuffer where, Compare compare,
			SqlQueryBuilder queryBuilder, Map<String, String> verseMapping) {

		addCompareArg(where, compare.getLeftArg(), queryBuilder, verseMapping);
		where.append(compare.getOperator().getSymbol());
		addCompareArg(where, compare.getRightArg(), queryBuilder, verseMapping);
	}

	private void addCompareArg(StringBuffer where, ValueExpr valueExpr,
			SqlQueryBuilder queryBuilder, Map<String, String> verseMapping) {
		if (valueExpr instanceof Var) {
			String columnname = verseMapping.get(((Var) valueExpr).getName());
			where.append(columnname);
		} else {
			where.append("?");
			ValueConstant constant = (ValueConstant) valueExpr;
			Literal literal = (Literal) constant.getValue();
			Object data = parseLiteral(literal);
			queryBuilder.inputBindings.add(new Binding(getSQLType(data), data));
		}
	}

	private int getSQLType(Object object) {
		if (object instanceof PGgeometry) {
			return Types.OTHER;
		} else if (object instanceof Timestamp) {
			return Types.TIMESTAMP;
		} else if (object instanceof String) {
			return Types.VARCHAR;
		} else if (object instanceof Double) {
			return Types.DOUBLE;
		} else if (object instanceof Float) {
			return Types.FLOAT;
		} else if (object instanceof Integer) {
			return Types.INTEGER;
		} else if (object instanceof Time) {
			return Types.TIME;
		}

		return Types.OTHER;
	}

	private Object parseLiteral(Literal literal) {
		String uri = literal.getDatatype().stringValue();
		if (DataTypeURI.isGeometry(uri)) {
			return IndexedStatement.asGeometry(literal, true);
		} else if (DataTypeURI.DATETIME.equals(uri)) {
			try {
				return new Timestamp(DateFormatter.getInstance().parse(literal.getLabel())
						.getTime());
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		} else if (DataTypeURI.isText(uri)) {
			return literal.getLabel();
		} else if (DataTypeURI.DOUBLE.equals(uri)) {
			return Double.valueOf(literal.getLabel());
		} else if (DataTypeURI.FLOAT.equals(uri)) {
			return Float.valueOf(literal.getLabel());
		} else if (DataTypeURI.INTEGER.equals(uri)) {
			return Integer.valueOf(literal.getLabel());
		} else if (DataTypeURI.TIME.equals(uri)) {
			return Time.valueOf(literal.getLabel());
		}

		return null;
	}

	private PreparedStatement createSqlQuery(SqlQueryBuilder builder)
			throws SQLException {
		String sql = builder.getSQL();
		boolean success = false;
		if (builder.limit > 0) {
			sql += " limit ?";
		}
		PreparedStatement ps = getConnection().prepareStatement(sql,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		try {

			int bindingIdx = 1;

			for (Binding binding : builder.inputBindings)
				ps.setObject(bindingIdx++, binding.value, binding.type);
			success = true;

			// if (builder.limit > 0) {

			// ps.setFetchSize((int)builder.limit + 1);
			// }
			return ps;
		} finally {
			if (!success)
				IndexedStatement.closeQuietly(ps);
		}
	}

	private void asSql(IdxQuery idxQuery, SqlQueryBuilder builder,
			BindingSet sparqlBindings) throws QueryEvaluationException,
			SQLException {
		if (idxQuery instanceof IdxJoin)
			asSql((IdxJoin) idxQuery, builder, sparqlBindings);
		else if (idxQuery instanceof IdxOrder)
			asSql((IdxOrder) idxQuery, builder, sparqlBindings);
		else {
			Validate.isTrue(idxQuery instanceof AbstractIdxQuery);
			asSql((AbstractIdxQuery) idxQuery, builder, sparqlBindings);
		}
	}

	private void asSql(IdxJoin idxJoin, SqlQueryBuilder builder,
			BindingSet sparqlBindings) throws QueryEvaluationException,
			SQLException {
		for (IdxQuery q : idxJoin.getQueries())
			asSql(q, builder, sparqlBindings);

		for (String resultColumn : idxJoin.getAllResultBindings()) {
			String firstResultColumn = null;
			boolean firstResultColumnObject = false;
			for (Map.Entry<AbstractIdxQuery, String> idxQueryEntry : builder.queryPartToAlias
					.entrySet()) {
				AbstractIdxQuery idxQuery = idxQueryEntry.getKey();
				if (idxQuery.getAllResultBindings().contains(resultColumn)) {
					if (firstResultColumn == null) {
						firstResultColumn = idxQueryEntry.getValue() + "."
								+ resultColumn;
						firstResultColumnObject = resultColumn.equals(idxQuery
								.getObjectVar().getName());
					} else {
						if (firstResultColumnObject
								|| resultColumn.equals(idxQuery.getObjectVar()
										.getName()))
							// See #78:
							throw new IndexException(
									"PostgisIndexer join on an object-side of a triple not [yet] supported: "
											+ resultColumn);
						builder.addJoin(firstResultColumn,
								idxQueryEntry.getValue() + "." + resultColumn);
					}
				}
			}
		}
	}

	private void asSql(IdxOrder idxOrder, SqlQueryBuilder builder,
			BindingSet sparqlBindings) throws QueryEvaluationException,
			SQLException {
		asSql(idxOrder.getQuery(), builder, sparqlBindings);
		builder.orderBy.addAll(idxOrder.getOrderElems());
	}

	public static final class Binding {
		private final int type;
		private final Object value;

		public Binding(int type, Object object) {
			this.type = type;
			this.value = object;
		}

		public int getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}
	}

	private static final class SqlQueryBuilder {
		private final Map<AbstractIdxQuery, String> queryPartToAlias = new HashMap<AbstractIdxQuery, String>();
		private final List<Binding> inputBindings = new ArrayList<Binding>();
		private final List<OrderInfo> orderBy = new ArrayList<OrderInfo>();
		private final List<String> joins = new ArrayList<String>();
		private String sql = null;
		private long limit = -1;

		public void setLimit(long limit) {
			this.limit = limit;
		}

		public String getSQL() {
			return this.sql;
		}

		public void setSQL(String sql) {
			this.sql = sql;
		}

		public void addJoin(String column1, String column2) {
			joins.add(column1 + EQ + column2);
		}

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void initialize() {
		this.getSettings().initialize();
	}

	private void clearTable() throws SQLException {
		Connection connection = getConnection();
		java.sql.Statement statement = connection.createStatement();
		statement.execute("delete from " + this.getSettings().getTableName());
		connection.commit();
		statement.close();
	}

	@Override
	public void reindex(SailConnection connection) throws SailException,
			IndexException {

		this.getSettings().getIndexedVars();
		CloseableIteration<? extends BindingSet, QueryEvaluationException> iterator = connection
				.evaluate(this.getSettings().getIndexGraph().getTupleQuery(),
						null, new EmptyBindingSet(), false);

		try {
			clearTable();
			writeIndex(iterator, true);
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			LOG.info(this.getSettings().getTableName());
			e.printStackTrace();
		}
	}

	// private void writeIndex(
	// CloseableIteration<? extends BindingSet, QueryEvaluationException>
	// iterator)
	// throws SQLException, QueryEvaluationException {
	// writeIndex(iterator, false);
	// }

	private void writeIndex(
			CloseableIteration<? extends BindingSet, QueryEvaluationException> iterator,
			boolean autoCommit) throws SQLException, QueryEvaluationException {
		PreparedStatement statement = getInsertStatment();
		boolean oldAutoCommit = statement.getConnection().getAutoCommit();
		statement.getConnection().setAutoCommit(autoCommit);
		// Object[] varNames = this.getSettings().getIndexGraph().getVarNames()
		// .toArray();
		// int[] types = getSQLTypes(varNames);

		int cache = 0;
		int batchsize = this.getSettings().getBatchSize();
		long count = 0;
		DIGEST.reset();
		for (; iterator.hasNext(); cache++) {

			BindingSet bindingSet = iterator.next();
			setInsertValue(this.getVarNames(), this.getSQLTypes(), statement,
					bindingSet);

			try {
				if (autoCommit) {
					statement.executeUpdate();
				} else {
					statement.addBatch();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (cache > batchsize) {
				if (!autoCommit) {
					statement.executeBatch();
					statement.getConnection().commit();
				}

				LOG.info(this.name + " " + (count += cache));
				cache = 0;
			}
		}

		if (!autoCommit) {
			statement.getConnection().commit();
		}
		statement.getConnection().setAutoCommit(oldAutoCommit);
	}

	private String setInsertValue(Object[] varNames, int[] types,
			PreparedStatement statement, BindingSet bindingSet)
			throws SQLException {

		for (int i = 0; i < varNames.length; i++) {
			Value value = bindingSet.getValue(varNames[i].toString());
			statement.setObject(
					i + 2,
					getSQLValue(bindingSet.getValue(varNames[i].toString()),
							types[i]));

			DIGEST.update(value.toString().getBytes());

		}
		String result = new String(Hex.encodeHex(DIGEST.digest()));
		statement.setString(1, result);
		return result;
	}

	private PreparedStatement getInsertStatment() throws SQLException {
		if (insertStatement == null) {

			String presql = createInsertSQL(this.getSettings().getTableName(),
					this.getVarNames());
			insertStatement = getConnection().prepareStatement(presql);
		}
		return insertStatement;
	}

	private int[] getSQLTypes(Object[] varNames) {
		int[] result = new int[varNames.length];
		Map<String, LiteralDef> map = this.getSettings().getIndexGraph()
				.getLiteralDefMap();
		for (int i = 0; i < varNames.length; i++) {
			LiteralDef literalDef = map.get(varNames[i]);
			if (literalDef == null) {
				result[i] = Types.VARCHAR;
			} else if (DataTypeURI.isGeometry(literalDef.getType())) {
				result[i] = Types.OTHER;
			} else if (DataTypeURI.DATETIME.equals(literalDef.getType())) {
				result[i] = Types.TIMESTAMP;
			}
		}
		return result;
	}

	private Object getSQLValue(Value value, int type) {

		if (type == Types.OTHER) {
			return IndexedStatement.asGeometry((Literal) value, true);
		} else if (type == Types.TIMESTAMP) {
			try {

				return new java.sql.Timestamp(DateFormatter.getInstance().parse(value.stringValue())
						.getTime());
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		} else {
			return value.stringValue();
		}
		return null;
	}

	private String createInsertSQL(String tableName, Object[] varNames) {
		StringBuffer leftHalf = new StringBuffer("insert into " + tableName
				+ " (" + PostgisIndexerSettings.OID + ",");
		StringBuffer rightHalf = new StringBuffer("values (?,");

		for (Object var : varNames) {
			leftHalf.append(var).append(',');
			rightHalf.append('?').append(',');
		}
		return leftHalf
				.deleteCharAt(leftHalf.length() - 1)
				.append(") ")
				.append(rightHalf.deleteCharAt(rightHalf.length() - 1).append(
						")")).toString();
	}

	@Override
	public boolean executeBatchAdd(SailConnection connection,
			Collection<Statement> operations) {

		// for (StatementOperation operation: operations) {
		// Statement statement = operation.getStatement();
		// if (operation.getOperation() == StatementOperation.OPERATION_ADD) {
		// addIndex(connection, statement.getSubject(),
		// statement.getPredicate(), statement.getObject());
		// }else if(operation.getOperation() ==
		// StatementOperation.OPERATION_REMOVE){
		// removeIndex(connection, statement.getSubject(),
		// statement.getPredicate(), statement.getObject());
		// }
		// }
		// batchToAdd.clear();
		// batchToRemove.clear();

		return true;
	}

	@Override
	public boolean executeBatchRemove(SailConnection connection,
			Collection<Statement> statements) {
		// TODO Auto-generated method stub
		return false;
	}

	private final int OPERATION_ADD = 1;
	private final int OPERATION_REMOVE = 2;

	@Override
	public void addBatch(SailConnection connection, Collection<Statement> toAdd) {
		executeBatch(connection, toAdd, OPERATION_ADD);
	}

	private void executeBatch(SailConnection connection,
			Collection<Statement> candidateStats, int operation) {

		HashSet<String> addedIDs = new HashSet<String>();
		for (Statement statement : candidateStats) {
			Map<String, Collection<StatementPattern>> patternMap = this
					.getSettings().getIndexGraph().getStatementPatternMap();

			Collection<StatementPattern> patterns = new HashSet<StatementPattern>();
			
			//check if the triple is indexed by the triple patterns 
			Collection<StatementPattern> p1 = patternMap.get(statement
					.getSubject().stringValue());
			if (p1 != null) {
				patterns.addAll(p1);
			}
			Collection<StatementPattern> p2 = patternMap.get(statement
					.getPredicate().stringValue());
			if (p2 != null) {
				patterns.addAll(p2);
			}
			Collection<StatementPattern> p3 = patternMap.get(statement
					.getObject().stringValue());
			if (p3 != null) {
				patterns.addAll(p3);
			}

			if (patterns.size() > 0) {
				try {
					PreparedStatement sqlStatement = null;
					if (operation == OPERATION_ADD) {
						sqlStatement = getInsertStatment();
					} else if (operation == OPERATION_REMOVE) {
						sqlStatement = getRemoveStatment();
					}
					boolean oldAutoCommit = sqlStatement.getConnection()
							.getAutoCommit();
					sqlStatement.getConnection().setAutoCommit(true);
					for (StatementPattern pattern : patterns) {
						QueryBindingSet bindingSet = new QueryBindingSet(2);
						if (!pattern.getSubjectVar().hasValue()) {
							bindingSet.addBinding(pattern.getSubjectVar()
									.getName(), statement.getSubject());
						}
						if (!pattern.getPredicateVar().hasValue()) {
							bindingSet.addBinding(pattern.getPredicateVar()
									.getName(), statement.getPredicate());
						}
						if (!pattern.getObjectVar().hasValue()) {
							bindingSet.addBinding(pattern.getObjectVar()
									.getName(), statement.getObject());
						}
						DIGEST.reset();

						if (this.getSettings().getIndexGraph().getPatterns()
								.size() == 1) {
							if (operation == OPERATION_ADD) {
								setInsertValue(this.getVarNames(),
										this.getSQLTypes(), sqlStatement,
										bindingSet);
							} else if (operation == OPERATION_REMOVE) {
								setRemoveValue(this.getVarNames(),
										sqlStatement, bindingSet);
							}

							try {
								sqlStatement.executeUpdate();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else {
							CloseableIteration<? extends BindingSet, QueryEvaluationException> iterator = connection
									.evaluate(this.getSettings()
											.getIndexGraph().getTupleQuery(),
											null, bindingSet, false);
							for (; iterator.hasNext();) {
								BindingSet resultBinding = iterator.next();
								if (operation == OPERATION_ADD) {
									String md5 = setInsertValue(
											this.getVarNames(),
											this.getSQLTypes(), sqlStatement,
											resultBinding);
									if (!addedIDs.contains(md5)) {
										addedIDs.add(md5);
										try {
											sqlStatement.executeUpdate();
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
								} else if (operation == OPERATION_REMOVE) {
									setRemoveValue(this.getVarNames(),
											sqlStatement, resultBinding);
									try {
										sqlStatement.executeUpdate();
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}

							}
						}

					}
					sqlStatement.getConnection().setAutoCommit(oldAutoCommit);
				} catch (SQLException e1) {
					e1.printStackTrace();
				} catch (SailException e) {
					e.printStackTrace();
				} catch (QueryEvaluationException e) {
					e.printStackTrace();
				}

			}

		}
	}

	private String setRemoveValue(String[] varNames,
			PreparedStatement statement, BindingSet resultBinding)
			throws SQLException {
		for (int i = 0; i < varNames.length; i++) {
			Value value = resultBinding.getValue(varNames[i].toString());
			DIGEST.update(value.toString().getBytes());
		}
		String result = new String(Hex.encodeHex(DIGEST.digest()));
		statement.setString(1, result);
		return result;

	}

	private PreparedStatement getRemoveStatment() throws SQLException {
		if (removeStatement == null) {
			String presql = createRemoveSQL(this.getSettings().getTableName());
			removeStatement = getConnection().prepareStatement(presql);
		}
		return removeStatement;
	}

	private String createRemoveSQL(String tableName) {
		return "delete from " + tableName + " where "
				+ PostgisIndexerSettings.OID + " = ?";
	}

	@Override
	public void removeBatch(SailConnection connection,
			Collection<Statement> toRemove) {
		executeBatch(connection, toRemove, OPERATION_REMOVE);
	}

	@Override
	public void clear() {
		try {
			this.clearTable();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
