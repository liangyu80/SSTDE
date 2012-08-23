package edu.ncsa.sstde.sail;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.repository.sail.config.SailRepositoryFactory;
import org.openrdf.sail.Sail;


import edu.ncsa.sstde.indexing.IndexManager;
import edu.ncsa.sstde.indexing.IndexingSail;

public class IndexingSailRepoFactory extends SailRepositoryFactory {

	public static final String REPOSITORY_TYPE = "useekm2:IndexingSailRepository";
	
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	
	@Override
	public RepositoryImplConfig getConfig() {
		return new IndexingSailRepoConfig();
	}


	public Repository getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		if (config instanceof IndexingSailRepoConfig) {
			IndexingSailRepoConfig indexingSailRepoConfig = (IndexingSailRepoConfig) config;
			SailRepositoryConfig sailRepositoryConfig = indexingSailRepoConfig;
			SailRepository repository = (SailRepository) (new SailRepositoryFactory()).getRepository(sailRepositoryConfig);
			Sail sail = repository.getSail();
			
			IndexingSail indexingSail = new IndexingSail(sail, IndexManager.getInstance());
			return new SailRepository(indexingSail);
		}
		
		throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
	}

}
