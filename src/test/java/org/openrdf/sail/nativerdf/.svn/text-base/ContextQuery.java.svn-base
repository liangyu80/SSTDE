package org.openrdf.sail.nativerdf;

import info.aduna.iteration.CloseableIteration;

import java.io.File;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.nativerdf.NativeTripleSource;

public class ContextQuery {
	public static void main(String[] args) throws SailException,
			MalformedQueryException, RepositoryException,
			QueryEvaluationException {
		// testIteration();
		testQuery4();
	}

	private static void testQuery() throws SailException,
			MalformedQueryException, RepositoryException,
			QueryEvaluationException {
		String sparql = "select * from <http://www.ncsa.uiuc.edu/Temp1> where {?s ?p ?o}";
		String dir2 = "repo-context";
		Sail sail2 = new NativeStore(new File(dir2));

		sail2.initialize();

		SailRepository repository = new SailRepository(sail2);
		TupleQuery prepareTupleQuery = repository.getConnection()
				.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
		TupleQueryResult evaluate = prepareTupleQuery.evaluate();

		for (; evaluate.hasNext();) {
			System.out.println(evaluate.next());
		}
	}

	private static void testQuery2() throws SailException,
			MalformedQueryException, RepositoryException,
			QueryEvaluationException {
		String sparql = "select *  where { graph <http://www.ncsa.uiuc.edu/Temp> {?s ?p ?o}}";
		String dir2 = "repo-context";
		Sail sail2 = new NativeStore(new File(dir2));
		sail2.initialize();
		ParsedTupleQuery query = QueryParserUtil.parseTupleQuery(
				QueryLanguage.SPARQL, sparql, null);
		QueryBindingSet bindingSet = new QueryBindingSet();
		bindingSet
				.addBinding(
						"s",
						sail2.getValueFactory()
								.createURI(
										"http://www.ncsa.uiuc.edu/linked_data/blank_node/node16f4isikhx4698"));
		CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate = sail2
				.getConnection().evaluate(query.getTupleExpr(), null,
						bindingSet, false);

		for (; evaluate.hasNext();) {
			System.out.println(evaluate.next());
		}

	}

	private static void testQuery3() throws SailException,
			MalformedQueryException, RepositoryException,
			QueryEvaluationException {
		String sparql = "select *  where { graph <http://www.ncsa.uiuc.edu/Temp> {?s ?p ?o}}";
		sparql = "select * where {?observation <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.oclc.org/NET/ssnx/ssn#Observation>}";
		sparql = "select * where {?observation <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> ?property}";
		String dir2 = "repo-context";
		Sail sail2 = new NativeStore(new File(dir2));
		sail2.initialize();
		ParsedTupleQuery query = QueryParserUtil.parseTupleQuery(
				QueryLanguage.SPARQL, sparql, null);
		QueryBindingSet bindingSet = new QueryBindingSet();
		ValueFactory factory = sail2.getValueFactory();
		// bindingSet
		// .addBinding(
		// "sensor",
		// factory.createURI("http://sensorweb.ncsa.uiuc.edu/data/sensordata/sites/CUAHSI/NWIS/03337100"));
		// bindingSet.addBinding("coord", factory.createLiteral("fdsfdsfsd",
		// factory.createURI("http://rdf.opensahara.com/type/geo/wkt")));
		// bindingSet.addBinding("timeinstance",
		// factory.createURI("http://www.ncsa.uiuc.edu/linked_data/blank_node/node16f4isikhx28759"));
		// bindingSet.addBinding("observation",
		// factory.createURI("http://sensorweb.ncsa.uiuc.edu/data/observation/USGS/NWIS/03337100/00065/2011-05-15T23:30:00.000-06:00"));
		// bindingSet.addBinding("timeinstancevalue",
		// factory.createLiteral("2011-05-15 23:30:00-05",
		// factory.createURI("http://www.w3.org/2001/xmlschema#datetime")));
		// bindingSet.addBinding("loc",
		// factory.createURI("http://www.ncsa.uiuc.edu/linked_data/blank_node/node16f4isikhx1447417"));
		bindingSet
				.addBinding(
						"observation",
						factory.createURI("http://sensorweb.ncsa.uiuc.edu/data/observation/USGS/NWIS/03337100/00065/2011-05-15T23:30:00.000-06:00"));

		CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate = sail2
				.getConnection().evaluate(query.getTupleExpr(), null,
						bindingSet, false);

		for (; evaluate.hasNext();) {
			System.out.println(evaluate.next());
		}

	}

	private static void testQuery4() throws SailException,
			MalformedQueryException, RepositoryException,
			QueryEvaluationException {
		String dir2 = "repo-context";
		NativeStore nativeStore = new NativeStore(new File(dir2));
//		NativeStoreConnection connection = nativeStore.getConnection();
		
		NativeTripleSource tripleSource = new NativeTripleSource(nativeStore,
				true, false);
		EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(
				tripleSource, null);

		
		Sail sail2 = nativeStore;
		sail2.initialize();
		QueryBindingSet bindingSet = new QueryBindingSet();
//		ValueFactory factory = sail2.getValueFactory();
		bindingSet
				.addBinding(
						"sensor",
						new URIImpl("http://sensorweb.ncsa.uiuc.edu/data/sensordata/sites/CUAHSI/NWIS/03337100"));
		bindingSet.addBinding("coord", new LiteralImpl(
				"0101000000000000600B0E56C000000000420E4440",
				new URIImpl("http://rdf.opensahara.com/type/geo/wkt")));
		bindingSet.addBinding("timeinstance", new URIImpl("http://www.ncsa.uiuc.edu/linked_data/blank_node/node16f4isikhx28759"));
		bindingSet.addBinding("observation", new URIImpl("http://sensorweb.ncsa.uiuc.edu/data/observation/USGS/NWIS/03337100/00065/2011-05-15T23:30:00.000-06:00"));
		bindingSet.addBinding("timeinstancevalue", new LiteralImpl("2011-05-15 23:30:00-05", new URIImpl("http://www.w3.org/2001/xmlschema#datetime")));
		bindingSet.addBinding("loc", new URIImpl("http://www.ncsa.uiuc.edu/linked_data/blank_node/node16f4isikhx1447417"));
		
//				.addBinding(
//						"observation",
//						factory.createURI("http://sensorweb.ncsa.uiuc.edu/data/observation/USGS/NWIS/03337100/00065/2011-05-15T23:30:00.000-06:00"));

		// CloseableIteration<? extends BindingSet, QueryEvaluationException>
		// evaluate = sail2
		// .getConnection().evaluate(query.getTupleExpr(), null,
		// bindingSet, false);
		StatementPattern pattern = new StatementPattern();
		pattern.setSubjectVar(new Var("observation"));
		pattern.setObjectVar(new Var("property"));
		pattern.setPredicateVar(new Var(null, new URIImpl(
				"http://purl.oclc.org/NET/ssnx/ssn#observedProperty")));

		CloseableIteration<BindingSet, QueryEvaluationException> evaluate = strategy
				.evaluate(pattern, bindingSet);

		for (; evaluate.hasNext();) {
			System.out.println(evaluate.next());
		}

	}

	private static void testIteration() throws SailException {
		String dir2 = "repo-context";
		Sail sail2 = new NativeStore(new File(dir2));

		sail2.initialize();
		URI uri = sail2.getValueFactory().createURI(
				"http://www.ncsa.uiuc.edu/Temp1");
		CloseableIteration<? extends Statement, SailException> statements = sail2
				.getConnection().getStatements(null, null, null, false, uri);

		long count = 0;
		for (; statements.hasNext();) {
			statements.next();
			// count++;
			System.out.println(count++);
		}
	}
}
