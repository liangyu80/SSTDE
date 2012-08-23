package edu.ncsa.uiuc.rdfrepo.testing;

import info.aduna.iteration.CloseableIteration;

import java.io.File;

import org.openrdf.model.Statement;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

public class Count {
	public static void main(String[] args) throws SailException {
		String dir = "E:\\SSN\\repo2";
		Sail sail = new NativeStore(new File(dir));
		sail.initialize();
		SailConnection connection = sail.getConnection();
		
		CloseableIteration<? extends Statement, SailException> statements = connection.getStatements(null, null, null, false);
		long i = 0;
		for (;statements.hasNext();) {
			statements.next();
			System.out.println(++i);
		}
		
		connection.close();
		sail.shutDown();
	}
}
