package edu.ncsa.uiuc.rdfrepo.testing;

import java.io.File;

import org.junit.Test;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;


import edu.ncsa.sstde.indexing.IndexManager;
import edu.ncsa.sstde.indexing.IndexingSail;

public class IndexingSailTest {

	@Test
	public void create() throws SailException {
		String dir = "repo2";
        Sail sail = new NativeStore(new File(dir));
		IndexingSail indexingSail = new IndexingSail(sail, IndexManager.getInstance());
		indexingSail.initialize();
		
		
		
		long t1 = System.currentTimeMillis();
		indexingSail.getConnection().reindex();
		long t2 = System.currentTimeMillis();
		System.out.println("reindexing all the indexes takes " + (t2 - t1));
	}

}
