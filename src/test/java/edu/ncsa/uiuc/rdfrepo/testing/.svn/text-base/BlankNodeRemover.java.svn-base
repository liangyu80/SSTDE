/*
 * Copyright 2012 by TalkingTrends (Amsterdam, The Netherlands)
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
package edu.ncsa.uiuc.rdfrepo.testing;

import java.io.File;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

public class BlankNodeRemover {
    public static void main(String[] args) throws SailException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        String sparql = "select * where {?s ?p ?o}";
        String dir = "C:\\work\\programming\\RDFRepTest2\\useekm\\useekm-core\\repo2";
        Sail sail = new NativeStore(new File(dir));
        sail.initialize();
        SailRepositoryConnection connection = (new SailRepository(sail)).getConnection();

        RepositoryResult<Statement> result = connection.getStatements(null, null, null, false);

        //        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        //        TupleQueryResult queryResult = query.evaluate();
        String prefix = "http://www.ncsa.uiuc.edu/linked_data/blank_node/";
        for (long i = 0; result.hasNext(); i++) {
            Statement statement = result.next();
            //            System.out.println(statement);
            if (statement.getSubject() instanceof BNode || statement.getObject() instanceof BNode) {
                System.out.println(statement);
                Resource subject = statement.getSubject();
                Value object = statement.getObject();
                if (subject instanceof BNode) {
                    subject = sail.getValueFactory().createURI(prefix + subject.stringValue());
                }
                if (object instanceof BNode) {
                    object = sail.getValueFactory().createURI(prefix + object.stringValue());
                }
                Statement newStatement = sail.getValueFactory().createStatement(subject, statement.getPredicate(), object);
                connection.remove(statement);
                connection.add(newStatement);
                System.out.println(i);
                //                connection.remove(bindingSet.getBinding("s").getValue(), bindingSet.getBinding("p").getValue(), bindingSet.getBinding("o").getValue(), null);
            }
        }
        //        connection.commit();
    }
}
