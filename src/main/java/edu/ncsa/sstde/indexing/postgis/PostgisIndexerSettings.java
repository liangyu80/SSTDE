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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.Validate;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;

import com.useekm.geosparql.Contains;
import com.useekm.geosparql.ContainsProperly;
import com.useekm.geosparql.CoveredBy;
import com.useekm.geosparql.Covers;
import com.useekm.geosparql.Crosses;
import com.useekm.geosparql.Disjoint;
import com.useekm.geosparql.Equals;
import com.useekm.geosparql.Intersects;
import com.useekm.geosparql.Overlaps;
import com.useekm.geosparql.Relate;
import com.useekm.geosparql.Touches;
import com.useekm.geosparql.Within;
import com.useekm.indexing.exception.IndexException;
import com.useekm.indexing.internal.Indexer;
import com.useekm.indexing.postgis.IndexedStatement;
import com.useekm.indexing.postgis.PartitionDef;
import com.useekm.indexing.postgis.PostgisIndexMatcher;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.ncsa.sstde.indexing.IndexGraph;
import edu.ncsa.sstde.indexing.IndexerSettings;
import edu.ncsa.sstde.indexing.IndexingSail;
import edu.ncsa.sstde.indexing.IndexingSailConnection;
import edu.ncsa.sstde.indexing.LiteralDef;
import edu.ncsa.sstde.util.DataTypeURI;

/**
 * Settings for {@link PostgisIndexer}.
 */
public class PostgisIndexerSettings implements IndexerSettings {
//	private static final String SQL_BASE_TABLE = "/com/useekm/indexing/postgis/basetable.sql";
//	private static final String SQL_CHILD_TABLE = "/com/useekm/indexing/postgis/childtable.sql";
	private static final String DEFAULT_LOOKUP = "$";
//	private static final String DEFAULT_BASE_TABLE = "idxst";
	static final String DEFAULT_PARTITION = "0"; // because it start with a 0,
													// it will never overlap
													// with other partitions
	public static final String EXTENSION_NS = "http://rdf.opensahara.com/search#";
	public static final URI TEXT = new URIImpl(EXTENSION_NS + "text");
	public static final URI WITHIN = new URIImpl(EXTENSION_NS + Within.NAME);
	public static final URI INTERSECTS = new URIImpl(EXTENSION_NS
			+ Intersects.NAME);
	public static final URI OVERLAPS = new URIImpl(EXTENSION_NS + Overlaps.NAME);
	public static final URI CROSSES = new URIImpl(EXTENSION_NS + Crosses.NAME);
	public static final URI COVERED_BY = new URIImpl(EXTENSION_NS
			+ CoveredBy.NAME);
	public static final URI COVERS = new URIImpl(EXTENSION_NS + Covers.NAME);
	public static final URI CONTAINS = new URIImpl(EXTENSION_NS + Contains.NAME);
	public static final URI CONTAINS_PROPERLY = new URIImpl(EXTENSION_NS
			+ ContainsProperly.NAME);
	public static final URI EQUALS = new URIImpl(EXTENSION_NS + Equals.NAME);
	public static final URI DISJOINT = new URIImpl(EXTENSION_NS + Disjoint.NAME);
	public static final URI TOUCHES = new URIImpl(EXTENSION_NS + Touches.NAME);
	public static final URI RELATE = new URIImpl(EXTENSION_NS + Relate.NAME);

	public static final String OPERATOR_NS = "http://www.ncsa.uiuc.edu/math/operators#";
	public static final String OID = "_oid";
	// public static final URI LESSTHAN = new
	// URIImpl("http://www.ncsa.uiuc.edu/math/operators/lessthan");
	// public static final URI EQUALS2 = new
	// URIImpl("http://www.ncsa.uiuc.edu/math/operators/equals");

	public static final int DEFAULT_FETCH_SIZE = 200;
	public static final int DEFAULT_BATCH_SIZE = 200;
	public static final int DEFAULT_REINDEX_COMMIT_SIZE = 1000;
	public static final int DEFAULT_SRID = 4326;
	public static final int DEFAULT_DIMENSION = 2;
	public static final GeometryFactory DEFAULT_GEOM_FACTORY = new GeometryFactory(
			new PrecisionModel(), DEFAULT_SRID);
	private static final BinaryGeometry BINARY_GEOMETRY = new BinaryGeometry();
	private static final RelateGeometry RELATE_GEOMETRY = new RelateGeometry();
	private static final TextSearch TEXT_SEARCH = new TextSearch();
	private static final Map<URI, AbstractResolveSearchArg> SUPPORTED_FUNCTIONS = new HashMap<URI, AbstractResolveSearchArg>();
	
	static {
		SUPPORTED_FUNCTIONS.put(TEXT, TEXT_SEARCH);
		SUPPORTED_FUNCTIONS.put(WITHIN, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(INTERSECTS, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(OVERLAPS, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(CROSSES, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(COVERED_BY, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(COVERS, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(CONTAINS, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(CONTAINS_PROPERLY, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(EQUALS, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(DISJOINT, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(TOUCHES, BINARY_GEOMETRY);
		SUPPORTED_FUNCTIONS.put(RELATE, RELATE_GEOMETRY);
	}

	private List<PostgisIndexMatcher> matchers = new ArrayList<PostgisIndexMatcher>();
	private List<PartitionDef> partitions = new ArrayList<PartitionDef>();
	private Map<String, Map<String, String>> partitionNames;
	private DataSource dataSource;
	private String defaultSearchConfig = "simple";
	private int fetchSize = DEFAULT_FETCH_SIZE;
	private int batchSize = DEFAULT_BATCH_SIZE;
	private int reindexBatchSize = DEFAULT_REINDEX_COMMIT_SIZE;
//	private String baseTable = DEFAULT_BASE_TABLE;
	private Collection<StatementPattern> matchSatatments = null;
//	private Collection<Var> indexedVars = null;
	private IndexGraph indexingGraph = null;
	private String tableName = null;

	@Override
	public Collection<StatementPattern> getMatchSatatments() {
		return matchSatatments;
	}

	public void setMatchSatatments(Collection<StatementPattern> matchSatatments) {
		this.matchSatatments = matchSatatments;
	}

	public String getDefaultSearchConfig() {
		return defaultSearchConfig;
	}

	public void setDefaultSearchConfig(String defaultSearchConfig) {
		Validate.notEmpty(defaultSearchConfig);
		this.defaultSearchConfig = defaultSearchConfig;
	}

	public String getBaseTable() {
		// return baseTable;
		return "idxst2";
	}

	public String getDefaultTable() {
		return getTableName(DEFAULT_PARTITION);
	}

	public List<PostgisIndexMatcher> getMatchers() {
		return matchers;
	}

	public void setMatchers(List<PostgisIndexMatcher> matchers) {
		this.matchers = matchers;
	}

	public List<PartitionDef> getPartitions() {
		return partitions;
	}

	public void setPartitions(List<PartitionDef> partitions) {
		this.partitions = partitions;
	}

	public int getReindexBatchSize() {
		return reindexBatchSize;
	}

	public void setReindexBatchSize(int reindexBatchSize) {
		this.reindexBatchSize = reindexBatchSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String getTableName(String predicate, String lang) {
		if (partitionNames == null)
			validateAndInitPartitions();
		Map<String, String> langParts = partitionNames.get(predicate);
		if (langParts == null)
			langParts = partitionNames.get(DEFAULT_LOOKUP);
		String result = langParts.get(lang.toLowerCase(Locale.ROOT));
		return result == null ? langParts.get(DEFAULT_LOOKUP) : result;
	}

	void validateMatchers() {
		for (int i = 1; i < matchers.size(); ++i) {
			for (int k = 0; k != i; ++k)
				if (matchers.get(k).includes(matchers.get(i)))
					throw new IllegalStateException("Matcher "
							+ matchers.get(k)
							+ " will allways match before matcher "
							+ matchers.get(i));
		}
	}

	void validateAndInitPartitions() {
		partitionNames = new HashMap<String, Map<String, String>>();
		partitionNames.put(DEFAULT_LOOKUP, createNamesMap(DEFAULT_PARTITION));
		Set<String> predicates = new HashSet<String>();
		for (PartitionDef pd : partitions) {
			if (pd.getPredicates() == null || pd.getPredicates().length == 0)
				throw new IllegalStateException(
						"Invalid partition: no predicates defined");
			validateAndInitPartition(pd, predicates);
		}
	}

	private void validateAndInitPartition(PartitionDef pd,
			Set<String> predicates) {
		for (String predicate : pd.getPredicates())
			if (predicate.isEmpty())
				throw new IllegalStateException(
						"Invalid partition: empty predicate");
			else if (!predicates.add(predicate))
				throw new IllegalStateException(
						"Overlapping partition for predicate: " + predicate);
			else {
				new URIImpl(predicate); // will throw if not a valid uri
				partitionNames.put(predicate, createNamesMap(pd.getName()));
			}
		validateAndInitLanguagesForPartition(pd);
	}

	private void validateAndInitLanguagesForPartition(PartitionDef pd) {
		Set<String> languages = new HashSet<String>();
		if (pd.getLanguagePartitions() != null)
			for (String l : pd.getLanguagePartitions()) {
				String lang = l.toLowerCase(Locale.ROOT);
				if (lang.isEmpty())
					throw new IllegalStateException(
							"Invalid partition: empty language");
				else if (!languages.add(lang))
					throw new IllegalStateException(
							"Overlapping partition for predicate: " + lang);
				for (String predicate : pd.getPredicates())
					partitionNames.get(predicate).put(lang,
							getTableName(getPartitionName(pd, lang)));
			}
	}

	Map<String, String> getPartitionCheckConstraints() {
		Map<String, String> result = new HashMap<String, String>();
		StringBuffer defaultCheck = new StringBuffer(IndexedStatement.PREDICATE)
				.append(" NOT IN(");
		boolean first = true;
		for (PartitionDef partition : partitions) {
			StringBuffer predicateChecks = new StringBuffer();
			predicateChecks
					.append(IndexedStatement.PREDICATE)
					.append(" IN(")
					.append(IndexedStatement.escapedString(partition
							.getPredicates()[0]));
			if (!first)
				defaultCheck.append(',');
			first = false;
			defaultCheck.append(IndexedStatement.escapedString(partition
					.getPredicates()[0]));
			for (int i = 1; i < partition.getPredicates().length; ++i) {
				predicateChecks.append(',').append(
						IndexedStatement.escapedString(partition
								.getPredicates()[i]));
				defaultCheck.append(',').append(
						IndexedStatement.escapedString(partition
								.getPredicates()[i]));
			}
			predicateChecks.append(')');
			getPartitionByLanguageConstraints(result, partition,
					predicateChecks.toString());
		}
		defaultCheck.append(')');
		if (!result.isEmpty())
			result.put(DEFAULT_PARTITION, defaultCheck.toString());
		return result;
	}

	private void getPartitionByLanguageConstraints(Map<String, String> result,
			PartitionDef partition, String predicateChecks) {
		if (partition.getLanguagePartitions() != null
				&& partition.getLanguagePartitions().length > 0) {
			result.put(getPartitionName(partition, 0),
					getPartitionCheck(predicateChecks, partition, 0));
			StringBuffer languageDefault = new StringBuffer(predicateChecks)
					.append(" AND ").append(IndexedStatement.OBJECT_LANGUAGE)
					.append(" NOT IN('")
					.append(partition.getLanguagePartitions()[0].toLowerCase())
					.append('\'');
			for (int i = 1; i < partition.getLanguagePartitions().length; ++i) {
				result.put(getPartitionName(partition, i),
						getPartitionCheck(predicateChecks, partition, i));
				languageDefault
						.append(",\'")
						.append(partition.getLanguagePartitions()[i]
								.toLowerCase()).append('\'');
			}
			languageDefault.append(')');
			result.put(partition.getName(), languageDefault.toString());
		} else
			result.put(partition.getName(), predicateChecks);
	}

	private Map<String, String> createNamesMap(String defaultPartition) {
		Map<String, String> result = new HashMap<String, String>();
		result.put(DEFAULT_LOOKUP, getTableName(defaultPartition));
		return result;
	}

	private String getTableName(String partitionName) {
		return getBaseTable() + '_' + partitionName;
	}

	private String getPartitionName(PartitionDef partition, int langIdx) {
		return partition.getName() + '_'
				+ partition.getLanguagePartitions()[langIdx].toLowerCase();
	}

	public String getPartitionName(PartitionDef partition, String lang) {
		return partition.getName() + '_' + lang;
	}

	private String getPartitionCheck(String predicateChecks,
			PartitionDef partition, int langIdx) {
		return predicateChecks + " AND " + IndexedStatement.OBJECT_LANGUAGE
				+ "='"
				+ partition.getLanguagePartitions()[langIdx].toLowerCase()
				+ '\'';
	}

	/**
	 * @return The first {@link PostgisIndexMatcher} that matches the triple
	 *         specified in the parameters. Null if no match is found.
	 */
	public PostgisIndexMatcher findIndexMatcherForAdd(Resource subj, URI pred,
			Value obj, Resource... ctx) {
		for (PostgisIndexMatcher matcher : matchers) {
			if (matcher.matchesAdd(subj, pred, obj, ctx))
				return matcher;
		}
		return null;
	}

	/**
	 * @return False if it is certain that a delete with this pattern will not
	 *         affect the index. Otherwise true. An implementation may always
	 *         return true (false positive), this method is only used for
	 *         optimizations.
	 */
	public boolean deleteAffectsIndex(Resource subj, URI pred, Value obj,
			Resource... ctx) {
		for (PostgisIndexMatcher matcher : matchers) {
			if (matcher.matchesDelete(subj, pred, obj, ctx))
				return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPredicateIndexed(URI pred) {
		for (PostgisIndexMatcher matcher : matchers) {
			if (matcher.matchesDelete(null, pred, null))
				return true;
		}
		return false;
	}

	/**
	 * Creates a {@link PostgisIndexer}. Note that an {@link Indexer} is not
	 * thread-safe, hence every {@link IndexingSailConnection} should have its
	 * own {@link Indexer}.
	 */
	@Override
	public PostgisIndexer createIndexer() {
		return new PostgisIndexer(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Var getResultVarFromFunctionCall(URI function, List<ValueExpr> args) {
		AbstractResolveSearchArg resolver = SUPPORTED_FUNCTIONS.get(function);
		if (resolver == null)
			return null;
		return resolver.getResultVarFromFunctionCall(function, args);
	}

	@Override
	public void initialize() {
		// validateMatchers();
		// validateAndInitPartitions();
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			if (!isInitialized(conn))
				createTables(conn);
			conn.close();
			conn = null;
		} catch (SQLException e) {
			throw new IndexException(e);
		} catch (IOException e) {
			throw new IndexException(e);
		} finally {
			IndexedStatement.closeQuietly(conn);
		}
	}

	private boolean isInitialized(Connection conn) throws SQLException {
		ResultSet tables = conn.getMetaData().getTables(null, null, null,
				new String[] { "TABLE" });

		try {
			while (tables.next()) {
				String table = tables.getString(3);
				if (getTableName().equalsIgnoreCase(table)) {
					return true;
				}
			}
			return false;
		} finally {
			tables.close();
		}
	}

	private void createTables(Connection conn) throws IOException, SQLException {

		Collection<String> varNames = this.getIndexGraph().getVarNames();

		String createClause = "create table " + getTableName() + "(";
		StringBuffer indexClause = new StringBuffer();
//		StringBuffer uniqueClause = new StringBuffer("ALTER TABLE "
//				+ getTableName()
//				+ " ADD CONSTRAINT unique_index_constraint_" + getTableName() + " UNIQUE(");
		
		//add the first column as the primary, which is a 32 characters MD5 code to indicate the unique graph
		createClause += OID + " character(32) NOT NULL,CONSTRAINT " + getTableName() + "_pk PRIMARY KEY (" + OID + "),";
		for (String varname : varNames) {
			String sqlType = getVarType(varname);
			createClause += varname + " " + sqlType + " NOT NULL,";
			String indexName = "index_" + getTableName() + "_" + varname;
			String indexType = sqlType.equals("geometry") ? "gist" : "btree";

			indexClause.append("CREATE INDEX " + indexName + " ON "
					+ getTableName() + " USING " + indexType + " (" + varname
					+ ");");

//			uniqueClause.append(varname).append(',');
		}

		createClause = createClause.substring(0, createClause.length() - 1)
				+ ") WITH (OIDS=FALSE);";
//		uniqueClause.deleteCharAt(uniqueClause.length()-1).append(")");
		Statement stat = conn.createStatement();
		try {
			stat.execute(createClause + indexClause.toString());
			// for (Entry<String, String> childTable:
			// getPartitionCheckConstraints().entrySet()) {
			// String tableName = getTableName(childTable.getKey());
			// stat.execute(replaceVars(sqlChild, getBaseTable(), tableName,
			// childTable.getValue()));
			// }
		} finally {
			stat.close();
		}
	}

	private String getVarType(String varname) {
		LiteralDef literalDef = this.getIndexGraph().getLiteralType(varname);
		if (literalDef == null) {
			return "text";
		} else {
			if (literalDef.getType().equals(DataTypeURI.DATETIME)) {
				return "timestamp without time zone";
			} else if (literalDef.getType().equals(DataTypeURI.INTEGER)) {
				return "integer";
			} else if (literalDef.getType().equals(DataTypeURI.FLOAT)) {
				return "double precision";
			} else if (literalDef.getType().equals(DataTypeURI.DOUBLE)) {
				return "double precision";
			} else if (literalDef.getType().equals(DataTypeURI.STRING)) {
				return "character varying";
			} else if (literalDef.getType().equals(DataTypeURI.TIME)) {
				return "time without time zone";
			} else if (DataTypeURI.isGeometry(literalDef.getType())) {
				return "geometry";
			}
		}
		return null;
	}

	public String replaceVars(String sql, String baseName, String tableName,
			String check) {
		if (baseName != null)
			sql = sql.replace("${BASE_NAME}", baseName);
		sql = sql.replace("${TABLE_NAME}", tableName);
		sql = sql.replace("${DIMENSION}", String.valueOf(DEFAULT_DIMENSION));
		sql = sql.replace("${SRID}", String.valueOf(DEFAULT_SRID));
		if (check != null)
			sql = sql.replace("${CHECK}", check);
		return sql;
	}

	private static abstract class AbstractResolveSearchArg {
		abstract public Var getResultVarFromFunctionCall(URI function,
				List<ValueExpr> args);

		protected boolean isConstant(ValueExpr expr) {
			return expr instanceof ValueConstant
					|| (expr instanceof Var && ((Var) expr).hasValue());
		}

		protected boolean isUnboundVariable(ValueExpr expr) {
			return expr instanceof Var && !((Var) expr).hasValue();
		}

		protected Literal firstLiteral(List<ValueExpr> args, int startIndex) {
			Literal result = null;
			for (int i = startIndex; i < args.size() && result == null; ++i) {
				ValueExpr expr = args.get(i);
				if ((expr instanceof Var && ((Var) expr).getValue() instanceof Literal))
					result = (Literal) ((Var) expr).getValue();
				else if (expr instanceof ValueConstant
						&& ((ValueConstant) expr).getValue() instanceof Literal)
					result = (Literal) ((ValueConstant) expr).getValue();
			}
			return result;
		}

		protected URI indexCallOption(List<ValueExpr> args, int startIndex) {
			for (int i = startIndex; i < args.size(); ++i) {
				ValueExpr expr = args.get(i);
				URI uri = null;
				if ((expr instanceof Var && ((Var) expr).getValue() instanceof URI))
					uri = (URI) ((Var) expr).getValue();
				else if (expr instanceof ValueConstant
						&& ((ValueConstant) expr).getValue() instanceof URI)
					uri = (URI) ((ValueConstant) expr).getValue();
				if (IndexingSail.ARG_BY_RDF.equals(uri)
						|| IndexingSail.ARG_BY_INDEX.equals(uri))
					return uri;
			}
			return null;
		}
	}

	private static class BinaryGeometry extends AbstractResolveSearchArg {
		@Override
		public Var getResultVarFromFunctionCall(URI function,
				List<ValueExpr> args) {
			URI indexCallOption = indexCallOption(args, 2);
			if (args.size() >= 2
					&& !IndexingSail.ARG_BY_RDF.equals(indexCallOption)) {
				ValueExpr arg1 = args.get(0);
				ValueExpr arg2 = args.get(1);
				if (isUnboundVariable(arg1) && isConstant(arg2))
					return (Var) arg1;
				else if (isUnboundVariable(arg2) && isConstant(arg1))
					return (Var) arg2;
			}
			if (IndexingSail.ARG_BY_INDEX.equals(indexCallOption))
				throw new IndexException("Can not be executed on index: "
						+ function.stringValue() + ": " + args);
			return null;
		}
	}

	private static class RelateGeometry extends AbstractResolveSearchArg {
		@Override
		public Var getResultVarFromFunctionCall(URI function,
				List<ValueExpr> args) {
			URI indexCallOption = indexCallOption(args, 3);
			Var result = null;
			if (args.size() >= 3
					&& !IndexingSail.ARG_BY_RDF.equals(indexCallOption))
				result = findResultVar(args);
			if (result == null
					&& IndexingSail.ARG_BY_INDEX.equals(indexCallOption))
				throw new IndexException("Can not be executed on index: "
						+ function.stringValue() + ": " + args);
			return result;
		}

		protected Var findResultVar(List<ValueExpr> args) {
			Literal relatePattern = firstLiteral(args, 2);
			if (relatePattern != null) {
				ValueExpr arg1 = args.get(0);
				ValueExpr arg2 = args.get(1);
				if (isUnboundVariable(arg1) && isConstant(arg2))
					return (Var) arg1;
				else if (isUnboundVariable(arg2) && isConstant(arg1))
					return (Var) arg2;
			}
			return null;
		}
	}

	private static class TextSearch extends AbstractResolveSearchArg {
		@Override
		public Var getResultVarFromFunctionCall(URI function,
				List<ValueExpr> args) {
			if (args.size() >= 2
					&& !IndexingSail.ARG_BY_RDF
							.equals(indexCallOption(args, 2))) {
				ValueExpr arg1 = args.get(0);
				ValueExpr arg2 = args.get(1);
				if (isUnboundVariable(arg1) && isConstant(arg2))
					return (Var) arg1;
			}
			throw new IndexException("Invalid call: " + function + args); // Unlike
																			// with
																			// the
																			// geometry
																			// functions,
																			// there
																			// is
																			// no
																			// fall-back
																			// evaluation
																			// possible
		}
	}

	@Override
	public IndexGraph getIndexGraph() {
		return indexingGraph;
	}

	public void setIndexGraph(IndexGraph indexingGraph) {
		this.indexingGraph = indexingGraph;
	}

	@Override
	public void initProperties(Properties properties) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(properties.getProperty("provider",
				"org.postgresql.Driver"));
		dataSource.setUrl(properties.getProperty("url"));
		dataSource.setUsername(properties.getProperty("username"));
		dataSource.setPassword(properties.getProperty("password"));
		this.setDataSource(dataSource);
		this.setIndexGraph((IndexGraph) properties.get("index-graph"));
		this.tableName = properties.getProperty("index-table");
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public Collection<Var> getIndexedVars() {
		return null;
	}

}
