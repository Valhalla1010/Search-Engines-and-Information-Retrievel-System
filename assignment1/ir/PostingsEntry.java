/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.Serializable;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    /** score used for ranked retrieval
     * in task 2.1 and 2.2 this stores cosine similarity
     * TF-IDF score, PageRank score, combination
    **/
    public double score = 0;
    public ArrayList<Integer> positions; // stores positions of a term(query) in the document, needed for (tf)
    /**
     *  PostingsEntries are compared by their score (only relevant
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    // Task 2.1 (entries are compared by score (descending order)
    // constructor for ranked retrieval
    public PostingsEntry(int docID, double score){
        this.docID = docID;
        this.score = score;
        this.positions = new ArrayList<>();
    }

    /**
     * compare postings entries by score in descending order
     * this is REQUIRED for ranked retrieval so that
     * higher scoring document appear first
     */
    @Override
    public int compareTo( PostingsEntry other ) {
       return Double.compare( other.score, this.score );
    }


    //
    // YOUR CODE HERE
    //
    // create document id and an empty positions list.

    // create a postings entry for one document
    public PostingsEntry(int docID){
        this.docID = docID;
        this.positions = new ArrayList<>();
    }

    //adds a word occurrence position to the list
    public void addPosition(int position) {
        positions.add(position);
    }




}

