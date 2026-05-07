/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;


/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     * 
     * why: 0.2 and 0.8, query will be too dominated by feedback
     */  
    double alpha = 0.2; //0.2; 0.5

    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 0.8; //0.8,   1.0
    
    
    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
    
    
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }
    
    
    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }
    
    
    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results_before.txt of the previous query.
     *  @param docIsRelevant A boolean array representing which query results_before.txt the user deemed relevant.
     *  @param engine The search engine object
     */
    // Assignment_3
    /**
     * I implements the Rocchio relevance feedback algorithm.
     * First, it counts the documents selected as relevant by the user.
     * Then it creates a new query by combining the original query 
     * weighted by α and the terms from relevant documents weighted by β.
     * Terms from relevant documents are extracted using the tokenizer and
     * their frequencies are used to compute new weights.
     * 
     */
        // task 3.1
    public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Engine engine) {
        //
        // YOUR CODE HERE
        //
        // if there are no results, stop process
        if (results == null || results.size() == 0){
            return;
        } 
        //Count number of relevant documents selected by the user
        int Relevant = 0;
        for (boolean bool : docIsRelevant) { // go relevance flags
            if (bool) {                     // if document is marked relevant
                Relevant++;                 // increase counter
            }
        }
        // if user didn't mark any document relevant, stop
        if (Relevant == 0){
            return;
        } 

        // Create new query that will replace the old query
        Query newQuery = new Query();

        // Add original query terms with weight (scaled by alpha)
        // Rocchio formula: q_new = alpha * q_orginal + beta * (centroid of relevant docs)
        // approximation of relevant documents using term frequencies 
        for (QueryTerm qt : queryterm) {
            // multiply original query weight by alpha
            double q = qt.weight * alpha;
            //add weighted term to the new query
            newQuery.queryterm.add(new QueryTerm(qt.term, q));
        }

        // Process each relevant documents
        for (int i = 0; i < docIsRelevant.length; i++) {
            // skip docs that are not market as relevant
            if (!docIsRelevant[i]) continue;
            // get doc ID from results list
            int docID = results.get(i).docID;
            // get doc filname from the index
            String docName = engine.index.docNames.get(docID);

            try {
                // read the relevant document using UTF-8 encoding
                Reader reader = new InputStreamReader(
                        new FileInputStream(docName), StandardCharsets.UTF_8);
                // Create tokenizer to extract tokens from the document
                Tokenizer tok = new Tokenizer(reader, true, false, true, engine.patterns_file);
                // HashMap to store term frequencies in this document
                HashMap<String, Integer> termFreq = new HashMap<>();

                // Count term frequencies in the relevant document
                while (tok.hasMoreTokens()) {
                    // create a token
                    String token = tok.nextToken();
                    //increase frequency count for this token
                    termFreq.put(token, termFreq.getOrDefault(token, 0) + 1);
                }

                reader.close();

                // Add terms to new query 
                for (String term : termFreq.keySet()) {
                    // Compute weight using Rocchio beta component
                    // term frequency divided by number of relevant docs
                    // this match with β*<weight of term in doc>/<number of relevant documents>
                    // term frequency normalized by document length
                    double weight = beta * (termFreq.get(term) / (double) Relevant);
                    boolean found = false;
                    // add this term to the new query 
                    for (QueryTerm qt : newQuery.queryterm) {
                        if (qt.term.equals(term)) {
                            // if term already exists, increase its weight
                            qt.weight += weight;
                            // mark as found and break out of loop
                            found = true;
                            break;
                        }
                    }
                    // if term is not already in the query, add it
                    if (!found) {
                        // add new term with its computed weight to the new query
                        newQuery.queryterm.add(new QueryTerm(term, weight));
                    }
                }

            } catch (IOException e) {// handle file reading exceptions
                e.printStackTrace(); // print stack trace for debugging
            }
        }

        // Replace old query
        queryterm = newQuery.queryterm;
    }
    
}


