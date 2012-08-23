package edu.ncsa.uiuc.rdfrepo.testing;

import info.aduna.iteration.CloseableIteration;

import java.io.File;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

public class BugTest {
	public static void main(String[] args) throws RepositoryException, SailException, MalformedQueryException, QueryEvaluationException {
		String dir = "repo2";
		Sail sail = new NativeStore(new File(dir));
		sail.initialize();
		SailRepository repository = new SailRepository(sail);
		ValueFactory valueFactory = repository.getValueFactory();
//		RepositoryResult<Statement> result = repository
//				.getConnection()
//				.getStatements(
//						valueFactory
//								.createURI("http://sensorweb.ncsa.uiuc.edu/data/sensordata/sites/CUAHSI/NWIS/05539660"),
//						null, null, false);
//		for(;result.hasNext();){
//			System.out.println(result.next());
//		}
		
		String sparql = "select * where {?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. ?subject <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?geo. ?geo <http://www.opengis.net/rdf#hasWKT> ?coord.}";
		ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(
				QueryLanguage.SPARQL, sparql, null);
		
//		TupleQuery query = repository.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparql);
//		TupleQueryResult queryResult = query.evaluate();
//		int count = 0;
//		for(;queryResult.hasNext();){
//			BindingSet bindingSet = queryResult.next();
//			if ("http://sensorweb.ncsa.uiuc.edu/data/sensordata/sites/CUAHSI/NWIS/05539660".equals(bindingSet.getValue("subject").toString())) {
//				System.out.println(bindingSet);
//			}
//			
////			System.out.println(queryResult.next());
//		}
		
		
		CloseableIteration<? extends BindingSet, QueryEvaluationException> iterator = sail.getConnection()
				.evaluate(parsedQuery.getTupleExpr(),
						null, new EmptyBindingSet(), false);
		
		for(;iterator.hasNext();){
			BindingSet bindingSet = iterator.next();
			if ("http://sensorweb.ncsa.uiuc.edu/data/sensordata/sites/CUAHSI/NWIS/05539660".equals(bindingSet.getValue("subject").toString())) {
				System.out.println(bindingSet);
			}
			
		}
		
	}
}
