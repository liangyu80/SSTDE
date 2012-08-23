/*
 * Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
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
package com.useekm.reposail;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryLockedException;
import org.openrdf.repository.RepositoryReadOnlyException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailLockedException;
import org.openrdf.sail.SailReadOnlyException;

/**
 * A RepositorySail can be used to expose a Sail layer on top of a Semantic Database that does not implement a {@link Sail} layer itself, but does implement {@link Repository}
 * layer. There are a bunch of Semantci Databases (e.g. Virtusoso and 4Store) that have a Repository interface for access, but do not have the lower level Sail interface, required
 * for many of Open Sahara's Sesame Sail wrappers. This class is an easy way to expose the {@link Repository} as a {@link Sail}.
 * <p>
 * The {@link RepositorySail} will only work for queries that use query algebra that is compatible with SPARQL 1.0. That means that many operators that are specific for SerQL are
 * not supported. If you use SerQL as query language, some query operators (such as MINUS) are not supported.
 */
public class RepositorySail implements Sail {
    private Repository repository;

    public RepositorySail(Repository repository) {
        this.repository = repository;
    }

    @Override public void setDataDir(File dataDir) {
        repository.setDataDir(dataDir);
    }

    @Override public File getDataDir() {
        return repository.getDataDir();
    }

    @Override public void initialize() throws SailException {
        try {
            repository.initialize();
        } catch (RepositoryException e) {
            throw convert(e);
        }
    }

    @Override public void shutDown() throws SailException {
        try {
            repository.shutDown();
        } catch (RepositoryException e) {
            throw convert(e);
        }
    }

    @Override public boolean isWritable() throws SailException {
        try {
            return repository.isWritable();
        } catch (RepositoryException e) {
            throw convert(e);
        }
    }

    @Override public RepositorySailConnection getConnection() throws SailException {
        try {
            return new RepositorySailConnection(repository.getConnection());
        } catch (RepositoryException e) {
            throw convert(e);
        }
    }

    @Override public ValueFactory getValueFactory() {
        return repository.getValueFactory();
    }

    public static SailException convert(RepositoryException e) {
        if (e instanceof RepositoryLockedException) {
            SailLockedException result = new SailLockedException(((RepositoryLockedException)e).getLockedBy(), ((RepositoryLockedException)e).getRequestedBy());
            result.initCause(e);
            return result;
        } else if (e instanceof RepositoryReadOnlyException) {
            SailReadOnlyException result = new SailReadOnlyException(e.getMessage());
            result.initCause(e);
            return result;
        }
        return new SailException(e);
    }

    public static SailException convert(MalformedQueryException e) {
        return new SailException(e);
    }

    public static SailException convert(QueryEvaluationException e) {
        return new SailException(e);
    }
}
