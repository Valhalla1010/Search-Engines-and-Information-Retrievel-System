/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;
import ir.Query.QueryTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;
    
    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
        // load all PageRank scores
        //index.loadPageRank("pagerank.txt");
    }

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */
    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normType ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //

        /**for task 1.3
         * retrieves postings for each query term.
         * intersection to find documents that contain all words
         * */
        //Boolean AND - Multiword Query
        if (queryType == QueryType.INTERSECTION_QUERY) {
            PostingsList result = null; //if is empty


            //get the postings list for the first term in query
            for (QueryTerm term : query.queryterm) {
                String token = term.term;
                PostingsList postings = index.getPostings(token); // get postings for the term

                // if a term has no postings, the result is empty
                if (postings == null || postings.size() == 0) {

                    return new PostingsList();
                }

                if (result == null) {
                    // first term
                    result = postings;
                } else {

                    result = Intersect(result, postings);
                }
            }
            return result; // intersection of all postings


        }
        //task 1.4
        //phrase query searches for exactly contiguous words
        else if (queryType == QueryType.PHRASE_QUERY) {
            return phraseSearch(query);
        }

        // ******* task 2.2: Ranked MultiWord Retrieval *******
        /** else if (queryType == QueryType.RANKED_QUERY){
           return rankedMultiwordSearch(query);
        }**/

        // task 2.5
        else if (queryType == QueryType.RANKED_QUERY) {
            if(rankingType == RankingType.TF_IDF){
                return rankedTFidf(query);
            } else if (rankingType == RankingType.PAGERANK) {
                return rankedPageRank(query);
            } else if (rankingType == RankingType.COMBINATION) {
                return rankedCombination(query);
            }
        }

        return null;
    }

    // Task 1.3: Intersection Algorithm (Boolean AND Query)
    /**
     * Performs an intersection between two postings lists.
     * This method finds documents that contain both query terms.
     *
     * @param p1 The first postings list.
     * @param p2 The second postings list.
     * @return A new postings list containing only documents that appear in both lists.
     */
    private PostingsList Intersect(PostingsList p1, PostingsList p2){
        PostingsList answer = new PostingsList(); // stores the final result
        int i =0;
        int j=0;

        // two pointers to efficiently find documents that contain both position
        while (i < p1.size()  && j < p2.size()){
            PostingsEntry entry1 = p1.get(i);
            PostingsEntry entry2 = p2.get(j);

            if(entry1.docID == entry2.docID){
                // if docIDs match, added the document to the result

                answer.add(entry1);
                i++;
                j++;
            }else if (entry1.docID < entry2.docID){
                //move the pointer in the first list forward
                i++;
            }else {
                //move the pointer in the second list
                j++;
            }
        }
        return answer;
    }




    // Task 1.4: Phrase Query Search (Exact Phrase Matching)
    /**
     * Performs a phrase search over multiple query terms.
     * This ensures that words appear in the same document and in the correct order.
     *
     * @param query The phrase query containing multiple words.
     * @return A postings list containing only documents where the words appear sequentially.
     */
    private PostingsList phraseSearch(Query query){
        PostingsList result = null;// Stores the intermediate phrase search result

        for(QueryTerm term: query.queryterm){
            String token = term.term;
            // Retrieve the postings list for the term
            PostingsList postings = index.getPostings(token);
            // If a term is missing in the dataset, return an empty result (phrase cannot exist)
            if(postings == null || postings.size() == 0){
               return new PostingsList();
            }

            if(result == null){
                // First term, initialize the result with its postings list
                result = postings;

            }else{
                // Perform phrase intersection with the next term's postings
                result = phraseIntersect(result, postings);
                if(result.size() == 0){
                    // If no phrase matches are found, return an empty result
                    return result;
                }
            }
        }
        return result;
    }

    /**
     * Performs phrase intersection between two postings lists.
     * This ensures that words appear in the same document and in consecutive positions.
     * @return A new postings list containing only the documents where terms appear consecutively.
     */
    private PostingsList phraseIntersect(PostingsList p1, PostingsList p2) {
        //if either postings list is null, return empty position
        if (p1 == null || p2 == null) return new PostingsList();

        PostingsList answer = new PostingsList();
        //pointer for iterating through both postings lists
        int i = 0, j = 0;

        while (i < p1.size() && j < p2.size()) {
            PostingsEntry entry1 = p1.get(i);
            PostingsEntry entry2 = p2.get(j);

            // if both entries refer to the same document
            if (entry1.docID == entry2.docID) {
                if (entry1.positions == null || entry2.positions == null) {
                    i++;
                    j++;
                    continue;
                }
                //positions of term1 and term2
                ArrayList<Integer> pos1 = entry1.positions;
                ArrayList<Integer> pos2 = entry2.positions;
                PostingsEntry newEntry = new PostingsEntry(entry1.docID);

                //pointers for positions within the document
                int k = 0, l = 0;

                //compare positions to check if words appear consecutively
                while (k < pos1.size() && l < pos2.size()) {
                    //if word2 appears immediately after word1, add it to new entry
                    if (pos2.get(l) == pos1.get(k) + 1) {
                        newEntry.addPosition(pos2.get(l));
                        k++;
                        l++;
                    } else if (pos1.get(k) < pos2.get(l)) {
                        // move pointer in pos1 if word1 appears earlier than word2
                        k++;
                    } else {
                        l++;
                    }
                }
                //if there are valid positions found, add the entry to the result list
                if (!newEntry.positions.isEmpty()) {
                    answer.add(newEntry);
                }
                //move to the next document
                i++;
                j++;
            } else if (entry1.docID < entry2.docID) {
                //move pointer in p1 if its docID is smaller
                i++;
            } else {
                j++;
            }
        }

        return answer;

    }

    // Task 2.2: Ranked Multiword Retrieval and Task 2.1 Ranked Retrieval (cosine similarity)
    /*private PostingsList rankedMultiwordSearch(Query query){
        HashMap<Integer, Double> accumulator = new HashMap<>();
        int N = index.docLengths.size();  // total document

        for (QueryTerm qt : query.queryterm){
            String term = qt.term;
            PostingsList postings = index.getPostings(term);
            if (postings == null){
                return new PostingsList();
            }
            int df = postings.size(); // document frequency
            double idf = Math.log((double) N / df); // idf_t = ln ( N / df_t)

            for (PostingsEntry entry : postings.list){
                int tf = entry.positions.size();
                int len_d = index.docLengths.get(entry.docID);
                 /** score_dt = tf_dt * idf_t / len_d
                 * tf_dt = [ # occurrences of t in d]
                 * N = [# documents in the corpus],
                 * dft = [# documents in the corpus which contain t]
                 * lend = [# words in d]
                double score = (tf * idf) / len_d;
                accumulator.put(entry.docID, accumulator.getOrDefault(entry.docID, 0.0) + score);
            }
        }
        PostingsList result = new PostingsList();
        for (Integer docID : accumulator.keySet()){
            PostingsEntry entry = new PostingsEntry(docID);
            entry.score = accumulator.get(docID);
            result.add(entry);
        }

        Collections.sort(result.list);
        return result;
    }*/

    /**
     * Ranked retrieval using TF-IDF scoring.
     * This computes cosine similarity using:
     * cos(q,d) = q * d / |q| * |d|
     * q_i is the tf-idf weight of term i in the query
     * d_i is the tf-idf weight of term i in the document
     * The final score is the sum over all query terms.
     * **/
    // Task 2.1 and 2.2
    private PostingsList rankedTFidf(Query query) {
        int N = index.docLengths.size(); // number of documents
        double[] scores = new double[N]; // accumulator for document scores
        // for loop over all query terms
        for (QueryTerm qt : query.queryterm) {
            // get postings for the query term
            PostingsList postings = index.getPostings(qt.term);
            // if the term does not exist in the index, skip it.
            if (postings == null) continue;
            // idf = ln( N / df)     df = documents in the corpus which contain t(query).
            // document frequency (df)
            // compute inverse document frequency
            double idf = Math.log((double) N / postings.size()); // idf (inverse document frequency) of t(query)
            // for documents containing the query (term)
            for (PostingsEntry pe : postings.list) {
                int docID = pe.docID;
                // term frequency
                int tf = pe.positions.size();  // number occurrences of t(query) in d(document)
                // normalize by document length
                int len = index.docLengths.get(docID);   // words in d
                // cosine similarity
                scores[docID] += (tf * idf) / len; // score_dt = tf_dt * idf_t / len_d -> d(document), t(query term)
            }
        }
        // build result list (one entry per document)
        PostingsList result = new PostingsList();
        for (int docID = 0; docID < N; docID++) {
            if (scores[docID] > 0) {
                PostingsEntry e = new PostingsEntry(docID);
                e.score = scores[docID];   // assign score
                result.add(e);
            }
        }
        // Sort documents by descending score
        result.sort();
        return result;
    }

    //Task 2.5
    /**
     * Ranked retrieval using only PageRank scores
     * Each document is scored using its precomputed PageRank value.
     * **/
    private PostingsList rankedPageRank(Query query) {
        int N = index.docLengths.size(); // number of documents
        // keeps track of which documents have already been added
        // and this prevents the same document from appearing multiple times
        boolean[] seen = new boolean[N];
        double[] scores = new double[N]; // scores from document
        // for loop over all query terms
        for (QueryTerm qt : query.queryterm) {
            // get postings list
            PostingsList postings = index.getPostings(qt.term);
            // if the term does not exist in the index, skip it.
            if (postings == null) continue;
            // all documents containing this query term
            for (PostingsEntry pe : postings.list) {
                int docID = pe.docID;
                // if this document has not yet been processed
                if (!seen[docID]) {
                    // retrieve the PageRank score for this document
                    // and PageRank values are precomputed and stored in the index
                    scores[docID] = ((HashedIndex)index).pagerank.getOrDefault(docID, 0.0);
                    // mark the document as seen so it's not added again
                    seen[docID] = true;
                }
            }
        }
        //result posting list
        PostingsList result = new PostingsList();
        // all documents
        for (int docID = 0; docID < N; docID++) {
            // only add documents that appeared in at least one posting list
            if (seen[docID]) {
                // new postings for the document
                PostingsEntry e = new PostingsEntry(docID);
                // assign the PageRank score to the entry
                e.score = scores[docID];
                result.add(e);
            }
        }
        //Collections.sort(result.list);
        result.sort();
        return result;
    }
    // task 2.5  combining tf-idf and pagerank scores
    /**
     * combination ranking using TF-IDF and PageRank
     * final score is
     * alpha * TF-IDF + beta * PageRank.
     * allow weighting between two scores
     * **/
    private PostingsList rankedCombination(Query query) {
        int N = index.docLengths.size();
        double[] tfidf = new double[N];  // array to accumulate TF-IDF score for each document
        for (QueryTerm qt : query.queryterm) {
            PostingsList postings = index.getPostings(qt.term);
            if (postings == null) continue;
            // compute inverse document frequency
            double idf = Math.log((double) N / postings.size());
            // all documents containing the term
            for (PostingsEntry pe : postings.list) {
                int docID = pe.docID;
                // compute term frequency
                int tf = pe.positions.size();
                // add normalized TF-IDF score to accumulator
                tfidf[docID] += (tf * idf) / index.docLengths.get(docID);
            }
        }
        // find the maximum PageRank value for normalization
        double maxPR = Collections.max(((HashedIndex)index).pagerank.values());
        // weight for TF-IDF component
        double alpha = 0.7;
        // Weight for PageRank component
        double beta = 0.3;
        // result postings list
        PostingsList result = new PostingsList();
        // all document
        for (int docID = 0; docID < N; docID++) {
            // only include documents that have a TF-IDF score
            if (tfidf[docID] > 0) {
                // normalize PageRank value to [0,1]
                double pr = ((HashedIndex)index).pagerank.getOrDefault(docID, 0.0) / maxPR;
                // new postings for the document
                PostingsEntry e = new PostingsEntry(docID);
                // combine TF-IDF and PageRank into a single score
                e.score = alpha * tfidf[docID] + beta * pr;
                result.add(e);
            }
        }
        //Collections.sort(result.list);
        result.sort();
        return result;
    }
}


