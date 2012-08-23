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

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;

public class TestUpdatingData {
    public static void main(String[] args) throws SailException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        String sparql =
            "?observation <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time. ?time  <http://www.w3.org/2006/time#inXSDDateTime> ?timevalue. ?loc <http://www.opengis.net/rdf#hasWKT> ?coord. ?sensor <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?loc. ?observation <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sensor.";
        //        sparql = sparql.replaceAll("\\?observation", "<http://sensorweb.ncsa.uiuc.edu/data/observation/USGS/NWIS/05577500/00065/2011-05-10T15:30:00.000-06:00>");
        //        sparql = sparql.replaceAll("\\?loc", "_:node16f4isikhx1447143");
        sparql = "?sensor <http://www.loa-cnr.it/ontologies/DUL.owl#hasLocation> ?loc";
        sparql = sparql.replaceAll("\\?loc", "_:node16f4isikhx1444171");
        sparql = "select * where {" + sparql + "}";
        ParsedTupleQuery parsedQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, sparql, null);

        String dir = "repo2";
        Sail sail = new NativeStore(new File(dir));
        sail.initialize();
        SailRepositoryConnection connection = (new SailRepository(sail)).getConnection();

        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        TupleQueryResult queryResult = query.evaluate();
        int i = 0;
        while (queryResult.hasNext() && i++ < 500) {
            BindingSet bindingSet = queryResult.next();

            System.out.println(queryResult.next());

        }
        System.out.println(i);
    }
}
