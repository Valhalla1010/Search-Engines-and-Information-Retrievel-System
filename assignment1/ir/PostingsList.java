/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

/**
 * a postings list represents a list of documents matching a query.
 * In ranked retrieval, it scores document with their scores.
 * **/

package ir;

import java.util.ArrayList;
import java.util.Collections;
public class PostingsList {
    
    /** The postings list */
    public ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /** return Number of document in the postings list. */
    public int size() {
        return list.size();
    }

    /** Returns the ith posting. */
    //manage a list of documents (postings) for a specific token
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    // 
    //  YOUR CODE HERE
    //
    // adds a new postingsentry while ensuring the list remains sorted
    /*public void add(PostingsEntry entry){
        //Check for duplicate document IDs and maintain order
        for (int i = 0; i < size(); i++) {
            PostingsEntry ex_entry = list.get(i);
            //If the document ID already exists, it does nothing
            if (entry.docID == ex_entry.docID) {
                return; // Avoid duplicate document entries

                //it inserts the entry at the correct position
            } else if (entry.docID < ex_entry.docID) {
                list.add(i, ex_entry); // add in sorted order
                return;
            }
        }
         list.add(entry);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (PostingsEntry entry : list) {
            sb.append("(docID: ").append(entry.docID).append(", positions: ").append(entry.positions).append("), ");
        }
        if (!list.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove trailing comma and space
        }
        sb.append("]");
        return sb.toString();
    }*/

    /** Add postings entry
     * In ranked retrieval, we ensure that each document
     * appears only once in the result list
     * */
    public void add(PostingsEntry entry) {
        for (PostingsEntry e : list){
            if(e.docID == entry.docID){
                return; // avoid duplicate documents
            }
        }
        list.add(entry);
    }

    /** Sort postings list by score (ranked retrieval) */
    public void sort() {
        Collections.sort(list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PostingsEntry e : list) {
            sb.append("(")
                    .append(e.docID)
                    .append(", score=")
                    .append(e.score)
                    .append(") ");
        }
        return sb.toString();
    }

}

