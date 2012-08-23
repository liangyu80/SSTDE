package edu.ncsa.sstde.sail;


import org.openrdf.repository.sail.config.SailRepositoryConfig;

public class IndexingSailRepoConfig extends SailRepositoryConfig{

	public IndexingSailRepoConfig() {
		super();
		this.setType(IndexingSailRepoFactory.REPOSITORY_TYPE);
	}

}
