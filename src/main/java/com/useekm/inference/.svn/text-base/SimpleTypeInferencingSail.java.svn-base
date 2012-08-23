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
package com.useekm.inference;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.FilterIteration;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.sail.helpers.SailWrapper;

import com.useekm.reposail.RepositorySail;

/**
 * Sail that inferences rdfs:subClassOf and rdf:type triples.
 * 
 * <p>
 * The subClassOf relations are inferred (forward-chaining) so that:
 * 
 * <pre>
 * ?x rdf:type ?y -> ?y rdfs:subClassOf ?y
 * ?x rdfs:subClassOf ?y  ->  ?x rdfs:subClassOf ?x AND ?y subClassOf ?y
 * ?x rdfs:subClassOf ?y AND ?y rdfs:subClassOf ?z  ->  ?x rdfs:subClassOf ?z
 * </pre>
 * 
 * <p>
 * For rdf:type relations backward chaining is used (i.e. queries and calls to {@link SailConnection#getStatements(Resource, URI, Value, boolean, Resource...)} are rewritten) as
 * follows:
 * 
 * <pre>
 * ?x rdf:type ?y  ->  ?x rdf:type ?type . ?type rdfs:subClassOf ?y
 * </pre>
 * 
 * If you don't want to include inferred subclasses, query for <code>?x &lt;os:prop/type&gt; ?y</code>, which will be rewritten to <code>?x rdf:type ?y</code>. All other
 * queries/patterns are left untouched. Hence the pattern <code>?s ?p ?o</code> where variable <code>p</code> is not bound, will <strong>NOT</strong> include inferred
 * <code>rdf:type</code> relations
 * 
 * <p>
 * Additionally, &lt;os:prop/type&gt; and &lt;os:prop/subClassOf&gt; relations will be inferred as follows:
 * <ul>
 * <li>&lt;os:prop/subClassOf&gt; will exist for rdfs:subClassOf relations that are direct, so could not have been inferred by any of the above rules (inferenced by
 * forward-chaining).</li>
 * <li>&lt;os:prop/type&gt; will exist for rdf:type relations that are direct, so could not have been inferred through a combination of rdf:type and rdfs:subClassOf relations
 * (inferenced by backward-chaining).
 * </ul>`
 * 
 * @todo: Improve how this implementation works when contexts are used (to which contexts do inferenced statements belong, how are queries handled that use context limiters?).
 * @todo: Some forms of multiple inheritance are not currently supported (will result in undefined behavior!). E.g. when ?x is inferred to be rdfs:subClassOf ?y via different
 *        paths,
 *        some removes may not work correctly. Don't use multiple inheritance with the current implementation.
 */
public class SimpleTypeInferencingSail extends SailWrapper {
    public static final String NS_PROPERTY = "os:prop/";
    public static final URI DIRECT_TYPE = new URIImpl(NS_PROPERTY + "type");
    public static final URI DIRECT_SUBCLASSOF = new URIImpl(NS_PROPERTY + "subClassOf");

    public SimpleTypeInferencingSail(Sail baseSail) {
        super(baseSail);
    }

    @Override public SailConnection getConnection() throws SailException {
        return new SimpleTypeInferencingSailConnection(super.getConnection(), getValueFactory());
    }

    public static class SimpleTypeInferencingSailConnection extends SailConnectionWrapper {
        private static final String CONTEXT = "g";
        private static final String OBJECT = "o";
        private static final String PREDICATE = "p";
        private static final String SUBJECT = "s";
        private static final String PATTERN = "?" + SUBJECT + " ?" + PREDICATE + " ?" + OBJECT;

        private final ValueFactory vf;

        public SimpleTypeInferencingSailConnection(SailConnection wrappedCon, ValueFactory vf) {
            super(wrappedCon);
            this.vf = vf;
        }

        @Override public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
            throws SailException {
            if (RDF.TYPE.equals(pred))
                return getTypeInferredStatements(subj, pred, obj, includeInferred, contexts);
            else if (DIRECT_TYPE.equals(pred))
                return super.getStatements(subj, RDF.TYPE, obj, includeInferred, contexts);
            return super.getStatements(subj, pred, obj, includeInferred, contexts);
        }

        private CloseableIteration<? extends Statement, SailException> getTypeInferredStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
            throws SailException {
            QueryBindingSet bs = createBindingSet(subj, pred, obj);
            ParsedTupleQuery ptq = createInferredStatementsQuery(contexts);
            CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = evaluate(ptq.getTupleExpr(), ptq.getDataset(), bs, includeInferred);
            bindingsIter = new FilterIteration<BindingSet, QueryEvaluationException>(bindingsIter) {
                @Override protected boolean accept(BindingSet bindingSet) {
                    Value context = bindingSet.getValue(CONTEXT);
                    return bindingSet.getValue(SUBJECT) instanceof Resource
                            && bindingSet.getValue(PREDICATE) instanceof URI
                            && bindingSet.getValue(OBJECT) instanceof Value
                            && (context == null || context instanceof Resource);
                }
            };
            //Convert bindings to statements:
            CloseableIteration<Statement, QueryEvaluationException> statsIter = new ConvertingIteration<BindingSet, Statement, QueryEvaluationException>(bindingsIter) {
                @Override protected Statement convert(BindingSet bindingSet) {
                    Resource subject = (Resource)bindingSet.getValue(SUBJECT);
                    URI predicate = (URI)bindingSet.getValue(PREDICATE);
                    Value object = bindingSet.getValue(OBJECT);
                    Resource context = (Resource)bindingSet.getValue(CONTEXT);
                    if (context == null)
                        return vf.createStatement(subject, predicate, object);
                    else
                        return vf.createStatement(subject, predicate, object, context);
                }
            };
            return new ExcConvertingIteration<Statement>(statsIter);
        }

        private QueryBindingSet createBindingSet(Resource subj, URI pred, Value obj) {
            QueryBindingSet bs = new QueryBindingSet(3);
            if (subj != null)
                bs.addBinding(SUBJECT, subj);
            if (pred != null)
                bs.addBinding(PREDICATE, pred);
            if (obj != null)
                bs.addBinding(OBJECT, obj);
            return bs;
        }

        private ParsedTupleQuery createInferredStatementsQuery(Resource... contexts) throws SailException {
            //?s ?p ?o .
            //OPTIONAL {
            //  GRAPH ?g { ?s ?p ?o }
            //}
            //FILTER( !bound(?g) && ... )
            boolean withContexts = contexts.length > 0;
            StringBuffer query = new StringBuffer("select distinct ").append(PATTERN);
            if (withContexts)
                query.append(" ?").append(CONTEXT);
            query.append(" where{").append(PATTERN).append('.');
            if (withContexts) {
                query.append("optional{graph ?").append(CONTEXT).append("{").append(PATTERN).append("}}");
                query.append("FILTER(");
                boolean first = true;
                for (Resource context: contexts) {
                    if (!first)
                        query.append("||");
                    if (context != null)
                        query.append('?').append(CONTEXT).append("=<").append(context.stringValue()).append('>');
                    else
                        query.append("!bound(?").append(CONTEXT).append(')');
                    first = false;
                }
                query.append(")");
            }
            query.append("}");
            try {
                return QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query.toString(), null);
            } catch (MalformedQueryException e) {
                throw new SailException("Internal error parsing query: " + query, e);
            }
        }

        @Override public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            if (RDF.TYPE.equals(pred) && obj instanceof Resource) {
                addDirectType(subj, (Resource)obj, contexts);
            } else if (RDFS.SUBCLASSOF.equals(pred) && obj instanceof Resource)
                addSubclassInference(subj, (Resource)obj, contexts);
            else if (!DIRECT_SUBCLASSOF.equals(pred) && !DIRECT_TYPE.equals(pred)) //skip, those will be inferred
                super.addStatement(subj, pred, obj, contexts);
        }

        @Override public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            if (pred == null)
                patternRemove(subj, pred, obj, contexts);
            else if (RDFS.SUBCLASSOF.equals(pred))
                removeSubclassStatements(subj, obj, contexts);
            else if (RDF.TYPE.equals(pred)) {
                super.removeStatements(subj, pred, obj, contexts);
                if (obj instanceof Resource)
                    subClassSelfReferenceRemove((Resource)obj, contexts);

            } else if (!DIRECT_SUBCLASSOF.equals(pred) && !DIRECT_TYPE.equals(pred)) //skip, those will be inferred
                super.removeStatements(subj, pred, obj, contexts);
        }

        boolean hasStatement(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            CloseableIteration<? extends Statement, SailException> stats = super.getStatements(subj, pred, obj, false, contexts);
            try {
                return stats.hasNext();
            } finally {
                stats.close();
            }
        }

        private void removeSubclassStatements(Resource subj, Value obj, Resource... contexts) throws SailException {
            if (subj == null || obj == null)
                patternRemove(subj, RDFS.SUBCLASSOF, obj, contexts);
            else if (obj instanceof Resource)
                subClassRemove(subj, (Resource)obj, contexts);
            else
                super.removeStatements(subj, RDFS.SUBCLASSOF, obj, contexts);
        }

        private void addDirectType(Resource inst, Resource type, Resource... contexts) throws SailException {
            if (hasStatement(inst, RDF.TYPE, type, contexts))
                return;

            //If already instanceof a subtype we are ready:
            CloseableIteration<? extends Statement, SailException> stats = super.getStatements(null, RDFS.SUBCLASSOF, type, false, contexts);
            try {
                while (stats.hasNext())
                    if (hasStatement(inst, RDF.TYPE, stats.next().getSubject(), contexts))
                        return; // inst already instance of a subclass, therefore already inferred to be of this class
            } finally {
                stats.close();
            }

            //Remove all (direct-)type relations for superclasses:
            stats = super.getStatements(type, RDFS.SUBCLASSOF, null, false, contexts);
            try {
                while (stats.hasNext()) {
                    Value object = stats.next().getObject();
                    if (object instanceof Resource && !object.equals(type))
                        super.removeStatements(inst, RDF.TYPE, object, contexts);
                }
            } finally {
                stats.close();
            }
            //Add:
            super.addStatement(inst, RDF.TYPE, type, contexts);
            super.addStatement(type, RDFS.SUBCLASSOF, type, contexts);
        }

        private void addSubclassInference(Resource subClass, Resource superClass, Resource... contexts) throws SailException {
            if (hasStatement(subClass, RDFS.SUBCLASSOF, superClass, contexts))
                return;
            super.addStatement(subClass, RDFS.SUBCLASSOF, superClass, contexts);
            if (subClass.equals(superClass))
                return;
            super.addStatement(superClass, RDFS.SUBCLASSOF, superClass, contexts);
            super.addStatement(subClass, RDFS.SUBCLASSOF, subClass, contexts);
            computeInferredSubClassOf(subClass, superClass, contexts);
            computeDirectSubClassOf(subClass, superClass, contexts);
            removeInferredType(subClass, superClass, contexts);
        }

        private void computeInferredSubClassOf(Resource subClass, Resource superClass, Resource... contexts) throws SailException {
            Collection<Resource> subs = collectSubclasses(subClass, contexts);
            Collection<Resource> suprs = collectSuperClasses(superClass, contexts);
            for (Resource sub: subs)
                for (Resource spr: suprs)
                    super.addStatement(sub, RDFS.SUBCLASSOF, spr, contexts);
        }

        private void computeDirectSubClassOf(Resource subClass, Resource superClass, Resource... contexts) throws SailException {
            super.removeStatements(null, DIRECT_SUBCLASSOF, superClass, contexts);
            super.removeStatements(subClass, DIRECT_SUBCLASSOF, null, contexts);
            Collection<Resource> subClasses = collectSubclasses(superClass, contexts);
            subClasses.remove(superClass);
            for (Resource direct: subClasses)
                if (!isSubClassOf(direct, subClasses))
                    super.addStatement(direct, DIRECT_SUBCLASSOF, superClass, contexts);

            Collection<Resource> superClasses = collectSuperClasses(subClass, contexts);
            superClasses.remove(subClass);
            for (Resource direct: superClasses)
                if (!isSuperClassOf(direct, superClasses))
                    super.addStatement(subClass, DIRECT_SUBCLASSOF, direct, contexts);
        }

        private void removeInferredType(Resource subClass, Resource superClass, Resource... contexts) throws SailException {
            Collection<Resource> suprs = collectSuperClasses(superClass, contexts);
            //Find all instances of subClass and subClasses of subClass (hence DON't do this with a super. call):
            CloseableIteration<? extends Statement, SailException> stats = getStatements(null, RDF.TYPE, subClass, false, contexts);
            try {
                while (stats.hasNext()) {
                    Resource subject = stats.next().getSubject();
                    for (Resource supr: suprs)
                        super.removeStatements(subject, RDF.TYPE, supr, contexts);
                }
            } finally {
                stats.close();
            }
        }

        private boolean isSubClassOf(Resource clazz, Collection<Resource> clazzes, Resource... contexts) throws SailException {
            boolean result = false;
            for (Resource supr: clazzes) {
                if (!supr.equals(clazz))
                    result = hasStatement(clazz, RDFS.SUBCLASSOF, supr, contexts);
                if (result)
                    return true;
            }
            return false;
        }

        private boolean isSuperClassOf(Resource clazz, Collection<Resource> clazzes, Resource... contexts) throws SailException {
            boolean result = false;
            for (Resource sub: clazzes) {
                if (!sub.equals(clazz))
                    result = hasStatement(sub, RDFS.SUBCLASSOF, clazz, contexts);
                if (result)
                    return true;
            }
            return false;
        }

        private void subClassRemove(Resource subClass, Resource superClass, Resource[] contexts) throws SailException {
            if (hasStatement(subClass, DIRECT_SUBCLASSOF, superClass, contexts)) { //else the statement is implicitely available via inference
                super.removeStatements(subClass, DIRECT_SUBCLASSOF, superClass, contexts);
                CloseableIteration<? extends Statement, SailException> stats = super.getStatements(null, RDFS.SUBCLASSOF, subClass, false, contexts);
                try {
                    while (stats.hasNext()) {
                        Resource subj = stats.next().getSubject();
                        CloseableIteration<? extends Statement, SailException> suprs = super.getStatements(superClass, RDFS.SUBCLASSOF, null, false, contexts);
                        try {
                            while (suprs.hasNext()) {
                                Value obj = suprs.next().getObject();
                                if (obj instanceof Resource)
                                    super.removeStatements(subj, RDFS.SUBCLASSOF, obj, contexts);
                            }
                        } finally {
                            suprs.close();
                        }
                    }
                    subClassSelfReferenceRemove(subClass, contexts);
                    subClassSelfReferenceRemove(superClass, contexts);
                } finally {
                    stats.close();
                }
            }
        }

        protected void subClassSelfReferenceRemove(Resource clazz, Resource[] contexts) throws SailException {
            if (!hasStatement(clazz, DIRECT_SUBCLASSOF, null, contexts) && !hasStatement(null, DIRECT_SUBCLASSOF, clazz, contexts)
                && !hasStatement(null, RDF.TYPE, clazz, contexts))
                super.removeStatements(clazz, RDFS.SUBCLASSOF, clazz, contexts);
        }

        private void patternRemove(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {
            CloseableIteration<? extends Statement, SailException> stats = super.getStatements(subj, pred, obj, false, contexts);
            try {
                while (stats.hasNext()) {
                    Statement stat = stats.next();
                    removeStatements(stat.getSubject(), stat.getPredicate(), stat.getObject(), contexts);
                }
            } finally {
                stats.close();
            }
        }

        private Collection<Resource> collectSuperClasses(Resource child, Resource... contexts) throws SailException {
            Collection<Resource> suprs = new HashSet<Resource>();
            CloseableIteration<? extends Statement, SailException> stats = super.getStatements(child, RDFS.SUBCLASSOF, null, false, contexts);
            try {
                while (stats.hasNext()) {
                    Value spr = stats.next().getObject();
                    if (spr instanceof Resource)
                        suprs.add((Resource)spr);
                }
            } finally {
                stats.close();
            }
            return suprs;
        }

        private Collection<Resource> collectSubclasses(Resource parent, Resource... contexts) throws SailException {
            Collection<Resource> subs = new HashSet<Resource>();
            CloseableIteration<? extends Statement, SailException> stats = super.getStatements(null, RDFS.SUBCLASSOF, parent, false, contexts);
            try {
                while (stats.hasNext())
                    subs.add(stats.next().getSubject());
            } finally {
                stats.close();
            }
            return subs;
        }

        @Override public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
            boolean includeInferred) throws SailException {
            tupleExpr = tupleExpr.clone();
            new TypePatternInferencer().optimize(tupleExpr, dataset, bindings);
            return super.evaluate(tupleExpr, dataset, bindings, includeInferred);
        }
    }

    public static final class ExcConvertingIteration<T> extends ExceptionConvertingIteration<T, SailException> {
        private ExcConvertingIteration(CloseableIteration<T, QueryEvaluationException> source) {
            super(source);
        }

        @Override protected SailException convert(Exception e) {
            if (e instanceof QueryEvaluationException) {
                return RepositorySail.convert((QueryEvaluationException)e);
            } else {
                Validate.isTrue(e instanceof RuntimeException);
                throw (RuntimeException)e;
            }
        }
    };
}
