package edu.ncsa.uiuc.rdfrepo.testing;

import info.aduna.iteration.CloseableIteration;

import java.io.File;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;


import edu.ncsa.sstde.indexing.IndexManager;
import edu.ncsa.sstde.indexing.IndexingSail;


public class WritingTest {
	public static void main(String[] args) throws SailException {
//		simpleTest();
		long t1 = System.currentTimeMillis();
		
//		copyTest();
		deleteTest();
		long t2 = System.currentTimeMillis();
		System.out.println( "it takes " + (t2-t1));
//		commitTest();
	}

	private static void simpleTest() throws SailException {
		String dir = "repo2";
		Sail sail = new NativeStore(new File(dir));
		IndexingSail indexingSail = new IndexingSail(sail,
				IndexManager.getInstance());
		indexingSail.initialize();
		ValueFactory factory = indexingSail.getValueFactory();

		indexingSail.getConnection().addStatement(
				factory.createURI("http://www.ncsa.uiuc/subject"),
				factory.createURI("http://www.ncsa.uiuc/predicate"), factory.createURI("http://www.ncsa.uiuc/object"));
	}
	
	private static void copyTest() throws SailException{
		String dir1 = "repo2";
		Sail sail = new NativeStore(new File(dir1));

		String dir2 = "repo-temp";
		Sail sail2 = new NativeStore(new File(dir2));
		sail2 = new IndexingSail(sail2, IndexManager.getInstance());
		
		sail.initialize();
		sail2.initialize();
		
		ValueFactory factory = sail.getValueFactory();
		CloseableIteration<? extends Statement, SailException> statements = sail
				.getConnection().getStatements(null, null, null, false);

		SailConnection connection = sail2.getConnection();

		int cachesize = 1000;
		int cached = 0;
		long count = 0;
		for (; statements.hasNext();) {
			cached++;
			count++;
			Statement statement = statements.next();
			// sail.getConnection().executeUpdate(arg0, arg1, arg2, arg3)
//			System.out.println(statement);
			connection.addStatement(statement.getSubject(),
					statement.getPredicate(), statement.getObject());
			
			if (cached > cachesize) {
				System.out.println(count);
				connection.commit();
				cached = 0;
			}
		}
		
		connection.commit();
	}
	
	private static void deleteTest() throws SailException{
//		String dir1 = "repo2";
//		Sail sail = new NativeStore(new File(dir1));

		String dir2 = "repo-temp";
		Sail sail2 = new NativeStore(new File(dir2));
		sail2 = new IndexingSail(sail2, IndexManager.getInstance());
		
//		sail.initialize();
		sail2.initialize();
		
//		ValueFactory factory = sail2.getValueFactory();
//		CloseableIteration<? extends Statement, SailException> statements = sail2
//				.getConnection().getStatements(null, null, null, false);

		SailConnection connection = sail2.getConnection();

		int cachesize = 1000;
		int cached = 0;
		long count = 0;
		connection.removeStatements(null, null, null, null);
//		connection.commit();
	}

	
	private static void commitTest() throws SailException{
		String dir2 = "repo-temp";
		Sail sail = new NativeStore(new File(dir2));
		sail.initialize();
		ValueFactory factory = sail.getValueFactory();
		SailConnection connection = sail.getConnection();
//		connection.clear();
//		for (int i = 0; i < 10; i++) {
//			connection.addStatement(
//					factory.createURI("http://www.ncsa.uiuc/subject_" + i),
//					factory.createURI("http://www.ncsa.uiuc/predicate"), factory.createURI("http://www.ncsa.uiuc/object"));
//		}
//		factory.createURI("http://www.opengis.net/rdf#hasWKT")
		CloseableIteration<? extends Statement, SailException> statements = connection.getStatements(null, null, null, false);
		
		for (;statements.hasNext();) {
			System.out.println(statements.next());
		}
	}
	
}
