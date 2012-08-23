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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailException;


public class RemoteServerLoader {

    public static void main(String[] args) throws RepositoryException, RDFParseException, FileNotFoundException, RDFHandlerException, SailException, IOException {
        String dir = "C:\\work\\programming\\OData\\LinkedData\\xml\\rdf_wkt\\all";
        GenericLoader loader = new GenericLoader();

        Repository repository = new HTTPRepository("http://iacat-dev.ncsa.illinois.edu:8080/openrdf-sesame", "IACAT2");
        repository.getConnection().clear();
        loader.loadData(repository, new File(dir), RDFFormat.RDFXML);

        repository.initialize();
        repository.shutDown();
    }

    //    SailRepository repository = loader.getPgSQLRepo();
    //    //        repository.getConnection().clear();

    //    //        RepositoryConnection connection = repository.getConnection();
    //    //        connection.clear();
    //    //        connection.commit();
    //    //        connection.close();
    //    //        repository.shutDown();
    //    repository.shutDown();
}
