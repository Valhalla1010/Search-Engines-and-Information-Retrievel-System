package pagerank;
import java.util.*;
import java.io.*;

/**
 * computation using power iteration
 * **/
public class PageRank {

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

	/**
     *   Mapping from document names to document numbers.
     */
    HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];


    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a HashMap, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a HashMap whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
	 *   ex: p_1 = [2, 3, 4]
     */
    HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */

    final static double EPSILON = 0.0001;

       
    /* --------------------------------------------- */
	// reads documents and starts PageRank iteration

    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );  // read link graph
	iterate( noOfDocs, 1000 ); // start power iteration
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *	each line in the file has format: 1; 2, 3, 4
	 * this means page 1 links to page 2, 3, 4
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0; // keeps track of number of documents
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		// find position of the semicolon
		int index = line.indexOf( ";" );
		// extract the document title
		String title = line.substring( 0, index );
		// check if document already exists
		Integer fromdoc = docNumber.get( title );
		// if document is new
		if ( fromdoc == null ) {	
		    // assign a new doc ID
		    fromdoc = fileIndex++;
			// store mapping title to ID
		    docNumber.put( title, fromdoc );
			// store mapping ID to title
		    docName[fromdoc] = title;
		}
		//parse all outgoing links from document
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		// process each linked document
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
			// check if the linked doc already exists
		    Integer otherDoc = docNumber.get( otherTitle );
			// if other doc ID is new
		    if ( otherDoc == null ) {
			// create a new ID for unseen doc
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }

		    // If not already created, create link
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
			// add the outgoing link
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			// outlink count
			out[fromdoc]++;
		    }
		}
	    }
		// check if memory limit reached
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex; // total number of doc
    }


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     *	power iteration algorithm
     * repeatedly multiplies the probability vector with the transition matrix
     * until the value converge
     */
    void iterate( int numberOfDocs, int maxIterations ) {
		// YOUR CODE HERE
		// PageRank values of document
		double[] pagerank = new double[numberOfDocs];
		//array for storing new pageRank values
		double[] newPagerank = new double[numberOfDocs];

		// iterate PageRank uniformly
		for (int i = 0; i < numberOfDocs; i++){
			// gets 1/N probability
			pagerank[i] = 1.0 / numberOfDocs;
		}
		// main power iteration
		for (int iter = 0; iter < maxIterations; iter++){
			// reset next iteration
			Arrays.fill(newPagerank, 0.0);

			// iterate distribute pageRank from each doc
			for(int i = 0; i < numberOfDocs; i++ ){
				// if document has outgoing links
				// it's pageRank is distributed equally among it's outlinks
				if(out[i] > 0){
					// share pagerank equally among outlinks
					double share = pagerank[i] / out[i];
					// get all linked doc
					HashMap<Integer, Boolean> targets = link.get(i);
					// check if the node has outgoing targets
					if (targets != null){
						// iterate each target node , linked page
						for(Integer j : targets.keySet()){
							// add PageRank contribution to the target node new pagerank
							newPagerank[j] += share;
						}
					}
					// if it's dangling node,
					// it's pagerank is distributed uniformly to all page
				}else {
					// dangling node ,no outgoing links
					double share  = pagerank[i] / numberOfDocs;
					// distribute uniformly to all document
					for(int j = 0; j < numberOfDocs; j++){
						newPagerank[j] += share;
					}
				}
			}
			/**
			 * PageRank - first attempt
			 * PR(p) = sum(q E in(p)) PR(q) / L_q
			 * PR_p = the probability that the random surfer will be at page
			 * p at any given point in time.
			 * in(p) is the set of pages linking to p
			 * L_q is the number of outlinks from q
			 * **/
			// compute convergence difference
			double diff = 0.0;
			// iterate distribute pageRank from each doc
			for(int i = 0; i < numberOfDocs; i++){
				// apply damping factor(random jump), this represent probability that the surfer random jumps to other page
				// BORED / numberOfDocs : probability that the random surfer jumps to any page.
				// 1-BORED: probability 1 - C, the surfer is bored, "stop following links" and "restart" random page
				// 1-BORED * newPageRank: probability of following links and keeping the accumulated rank.
				newPagerank[i] = BORED / numberOfDocs + (1.0 - BORED) * newPagerank[i];
				// compute change between iterations
				diff += Math.abs(newPagerank[i] - pagerank[i]);
				// This prevents rank sinks (page without links) and ensures convergence.
			}
			// Convergence check, this is for stop iteration, compute the difference between new and old Pagerank
			// update pageRank vector
			pagerank = newPagerank.clone();
			// if the diff < EPSILON, algorithm stop
			if(diff < EPSILON){
				System.err.println("Converged after " + iter + " iterations");
				break;
			}// this means the pagerank have converged.
		}
		printTop30(pagerank, numberOfDocs);
		writePageRankToFile("pagerank.txt", pagerank, numberOfDocs);
		writeTop30ToFile("pagerank_top30.txt", pagerank, numberOfDocs);

    }
	// prints the top 30 documents with highest pagerank score
	void printTop30(double[] pagerank, int numberOfDocs) {
		// create index to store document indices
		Integer[] idx = new Integer[numberOfDocs];
		// iterate pageRank from each doc
		for (int i = 0; i < numberOfDocs; i++) {
			idx[i] = i;
		}

		final double[] finalPagerank = pagerank;
		// sort document by PageRank descending
		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				// compare PageRank so that higher scores come first
				return Double.compare(finalPagerank[b], finalPagerank[a]);
			}
		});


		System.out.println("\nTop 30 PageRank pages:");
		// print top 30 document.
		for (int i = 0; i < 30 && i < numberOfDocs; i++) {
			int doc = idx[i]; // get the doc index
			System.out.printf(
					// %d2 rank number with minimum width 2, used for (i + 1)
					// %30s string, doc name with Minimum width 30
					// %5d: show pagerank score with 5 decimal
					"%2d. %30s %.5f\n",
					i + 1, // ranking position
					docName[doc],
					pagerank[doc] // score
			);
		}

	}

	// 2.5
	/**
	 * write pagerank values for all document to a file
	 *
	 * **/
	void writePageRankToFile(String filename, double[] pagerank, int numberOfDocs){
		// open file for writing with buffered writer
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename))){
			// write score for each document
			for(int i = 0; i < numberOfDocs; i++){
				//bw.write(docName[i] + " " + pagerank[i]);
				bw.write(docName[i] + " " + String.format(Locale.US, "%.8f", pagerank[i]));
				// move to next line
				bw.newLine();
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	/**
	 * write only top 30 pagerank document to a file
	 * **/
	void writeTop30ToFile(String filename, double[] pagerank, int numberOfDocs) {
		Integer[] idx = new Integer[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) idx[i] = i;

		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return Double.compare(pagerank[b], pagerank[a]);
			}
		});

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
			for (int i = 0; i < 30 && i < numberOfDocs; i++) {
				int doc = idx[i];
				bw.write(docName[doc] + " " + pagerank[doc]);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /* --------------------------------------------- */


    public static void main( String[] args ) {
		// check if filename argument provided
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}