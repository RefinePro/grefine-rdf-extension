package org.deri.grefine.reconcile.rdf.executors;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;

import org.shaded.apache.http.auth.AuthScope;
import org.shaded.apache.http.auth.Credentials;
import org.shaded.apache.http.auth.UsernamePasswordCredentials;
import org.shaded.apache.http.client.CredentialsProvider;
import org.shaded.apache.http.client.HttpClient;
import org.shaded.apache.http.impl.client.BasicCredentialsProvider;
import org.shaded.apache.http.impl.client.HttpClients;
import org.shaded.apache.jena.query.QueryExecution;
import org.shaded.apache.jena.query.QueryExecutionFactory;
import org.shaded.apache.jena.query.ResultSet;
import org.shaded.apache.jena.query.ResultSetFactory;

/**
 * @author fadmaa
 * query a remote SPARQL endpoint
 */
public class RemoteQueryExecutor implements QueryExecutor {
    protected String sparqlEndpointUrl;
    protected String defaultGraphUri;

    public RemoteQueryExecutor(String sparqlEndpointUrl, String defaultGraphUri) {
        this.sparqlEndpointUrl = sparqlEndpointUrl;
        this.defaultGraphUri = defaultGraphUri;
    }

    @Override
    public ResultSet sparql(String sparql) {
        QueryExecution qExec;
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(System.getProperty("OR_AUTH_USERNAME"), System.getProperty("OR_AUTH_PASSWORD"));
        credProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credProvider)
                .build();
        if (defaultGraphUri == null) {
            qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, sparql, httpclient);
        } else {
            qExec = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, sparql, defaultGraphUri, httpclient);
        }
        ResultSet results = null;
        try {
            ResultSet res = qExec.execSelect();
            results = ResultSetFactory.copyResults(res);
        } finally {
            qExec.close();
        }
        return results;
    }

    @Override
    public void save(String serviceId, FileOutputStream baseDir) throws IOException {
        //nothing to save... all data is external
    }

    @Override
    public void write(JsonGenerator writer) throws JsonGenerationException, IOException {
        writer.writeStartObject();
        writer.writeStringField("type", "remote");
        writer.writeStringField("sparql-url", sparqlEndpointUrl);
        if (defaultGraphUri != null) {
            writer.writeStringField("default-graph-uri", defaultGraphUri);
        }
        writer.writeEndObject();
    }

    @Override
    public void initialize(FileInputStream in) {
        //nothing to initialize
    }
}
