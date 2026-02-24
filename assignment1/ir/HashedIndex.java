/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    // task 2.5 PageRank scores
    public HashMap<Integer, Double> pagerank = new HashMap<>();

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        // YOUR CODE HERE
        //
        String key = hashtoken(token);
        //create a postings list
        PostingsList postings = index.get(key);
        //If the token does not exist in the index, a new PostingsList is created and stored
        if (postings == null) {
            postings = new PostingsList();
            index.put(key, postings);
        }

        //Check if the token already exists in the same document
        // Get the last added entry.
        PostingsEntry lastEntry = postings.size() > 0 ? postings.get(postings.size() - 1) : null;
        //if the last added entry belongs to same document, it adds the new posistion
        if (lastEntry != null && lastEntry.docID == docID) {
            lastEntry.addPosition(offset);  // Add position to existing entry
        } else {
            //
            PostingsEntry newEntry = new PostingsEntry(docID);
            newEntry.addPosition(offset);  // Initialize with first position
            postings.add(newEntry);
        }
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        // look at the token in the index
        String key = hashtoken(token);
        //return index.get(key); //return the associated postings list.

        PostingsList postings = index.get(key);

        if (postings != null) {
            System.out.println("Postings for term \"" + token + "\": " + postings);
        } else {
            System.out.println("No postings found for term \"" + token + "\"");
        }

        return postings;
    }

    private String hashtoken(String token){
        return token;
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }

    // task 2.5
    @Override
    public void loadPageRank(String filename) {

        try(BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line;
            while ((line = br.readLine()) != null){
                String[] parts = line.split("\\s+");
                int docID = Integer.parseInt(parts[0]);
                double score = Double.parseDouble(parts[1]);
                pagerank.put(docID, score);
            }
            System.err.println("Loaded PageRank for " + pagerank.size() + " documents");
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


}
