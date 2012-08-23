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

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.SingletonIteration;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * A {@link SailConnection} created by {@link RepositorySail}.
 * 
 * @see RepositorySail
 */
public class RepositorySailConnection implements SailConnection {
    private static final EmptyBindingSet EMPTY_BINDINGS = new EmptyBindingSet();
    private final RepositoryConnection connection;

    public RepositorySailConnection(RepositoryConnection connection) throws RepositoryException {
        this.connection = connection;
        connection.setAutoCommit(false);
    }

    RepositoryConnection getRepositoryConnection() {
        return connection;
    }

    @Override public boolean isOpen() throws SailException {
        try {
            return connection.isOpen();
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void close() throws SailException {
        try {
            connection.close();
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
        throws SailException {
        try {
            Query query = connection.prepareQuery(QueryLanguage.SPARQL, new TupleExprToSparqlConverter().translate(tupleExpr));
            for (String bind: bindings.getBindingNames())
                query.setBinding(bind, bindings.getBinding(bind).getValue());
            if (query instanceof TupleQuery)
                return ((TupleQuery)query).evaluate();
            else if (query instanceof BooleanQuery)
                return ((BooleanQuery)query).evaluate() ? new SingletonIteration<BindingSet, QueryEvaluationException>(EMPTY_BINDINGS)
                    : new EmptyIteration<BindingSet, QueryEvaluationException>();
            else {
                Validate.isTrue(query instanceof GraphQuery);
                // How to handle: Multi-Projection? SourceName/TargetName of ProjectionElem's? Context/graph binding results?
                throw new QueryEvaluationException("CONSTRUCT queries not yet supported");
            }
        } catch (MalformedQueryException e) {
            throw RepositorySail.convert(e);
        } catch (QueryEvaluationException e) {
            throw RepositorySail.convert(e);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void executeUpdate(UpdateExpr updateExpr, Dataset dataset, BindingSet bindings, boolean includeInferred) throws SailException {
        throw new UnsupportedOperationException();
    }

    @Override public CloseableIteration<? extends Resource, SailException> getContextIDs() throws SailException {
        try {
            return new ConvertingIteration<Resource>(connection.getContextIDs());
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
        throws SailException {
        try {
            return new ConvertingIteration<Statement>(connection.getStatements(subj, pred, obj, includeInferred, contexts));
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public long size(Resource... contexts) throws SailException {
        try {
            return connection.size(contexts);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void commit() throws SailException {
        try {
            connection.commit();
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void rollback() throws SailException {
        try {
            connection.rollback();
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        try {
            connection.add(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
        try {
            connection.remove(subj, pred, obj, contexts);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void clear(Resource... contexts) throws SailException {
        try {
            connection.clear(contexts);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public CloseableIteration<? extends Namespace, SailException> getNamespaces() throws SailException {
        try {
            return new ConvertingIteration<Namespace>(connection.getNamespaces());
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }
    
    private static final class ConvertingIteration<T> extends ExceptionConvertingIteration<T, SailException> {
        private ConvertingIteration(CloseableIterationBase<T, RepositoryException> source) {
            super(source);
        }

        @Override protected SailException convert(Exception e) {
            if (e instanceof RepositoryException) {
                return RepositorySail.convert((RepositoryException)e);
            } else {
                Validate.isTrue(e instanceof RuntimeException);
                throw (RuntimeException)e;
            }
        }
    };

    @Override public String getNamespace(String prefix) throws SailException {
        try {
            return connection.getNamespace(prefix);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void setNamespace(String prefix, String name) throws SailException {
        try {
            connection.setNamespace(prefix, name);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void removeNamespace(String prefix) throws SailException {
        try {
            connection.removeNamespace(prefix);
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }

    @Override public void clearNamespaces() throws SailException {
        try {
            connection.clearNamespaces();
        } catch (RepositoryException e) {
            throw RepositorySail.convert(e);
        }
    }
}
