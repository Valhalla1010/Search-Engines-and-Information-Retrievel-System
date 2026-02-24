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
    final static double BORED = 0.15;  // standard power iteration method with bored factor of 0.15

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
	// the algorithm converged after a limited number of iterations and
	// completed within one minute, using 1GB of memory.
    final static double EPSILON = 0.0001;

       
    /* --------------------------------------------- */
	// reads documents and starts PageRank iteration

    public PageRank( String filename ) {
	int noOfDocs = readDocs( filename );  // read link graph
	iterate( noOfDocs, 1000 ); // compute PageRank
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and fills the data structures. 
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new HashMap<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
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
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Chooses a probability vector a, and repeatedly computes
     *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
     *
     */
    void iterate( int numberOfDocs, int maxIterations ) {
		// YOUR CODE HERE
		// PageRank vector for each document
		double[] pagerank = new double[numberOfDocs];
		// temporary vector to store PageRank value for the next iteration
		double[] newPagerank = new double[numberOfDocs];

		// initialize uniformly:
		for (int i = 0; i < numberOfDocs; i++){
			// each document gets 1/N probability
			pagerank[i] = 1.0 / numberOfDocs;
		}
		// power iteration
		for (int iter = 0; iter < maxIterations; iter++){
			// reset new PageRank values before computing next iteration
			Arrays.fill(newPagerank, 0.0);

			// distribute pageRank mass from each document
			for(int i = 0; i < numberOfDocs; i++ ){
				// if document has outgoing links
				if(out[i] > 0){
					// divide pagerank equally among all outlinks
					double share = pagerank[i] / out[i];
					// get all documents that i links to
					HashMap<Integer, Boolean> targets = link.get(i);
					// distribute pagerank share to all linked documents
					if (targets != null){
						for(Integer j : targets.keySet()){
							newPagerank[j] += share;
						}
					}
				}else {
					// dangling node
					// distribute pagerank uniformly to all document
					double share  = pagerank[i] / numberOfDocs;
					for(int j = 0; j < numberOfDocs; j++){
						newPagerank[j] += share;
					}
				}
			}
			/**
			 * PageRank - first attempt
			 * PR(p) = sum(q E in(p)) PR(q) / L_q
			 * p and q are pages
			 * in(p) is the set of pages linking to p
			 * L_q is the number of out-links from q
			 * **/
			// to here -> Power-iteretion step:  PR_new (j) = (sum(i->j) PR(i) / out(i))
			// apply boredom factor and compute convergence difference
			double diff = 0.0;
			for(int i = 0; i < numberOfDocs; i++){
				// apply random jump probability:  PR = BORED / N + ( 1 - BORED) * PR (accumulated PageRank)
				newPagerank[i] = BORED / numberOfDocs + (1.0 - BORED) * newPagerank[i];
				// accumulate total difference for convergence check
				diff += Math.abs(newPagerank[i] - pagerank[i]);
			}
			// update pageRank vector for nex iteration
			pagerank = newPagerank.clone();
			// stop iterations if pagerank value have converged
			if(diff < EPSILON){
				System.err.println("Converged after " + iter + " iterations");
				break;
			}
		}
		printTop30(pagerank, numberOfDocs);
		writePageRankToFile("pagerank.txt", pagerank, numberOfDocs);
		writeTop30ToFile("pagerank_top30.txt", pagerank, numberOfDocs);

    }
	// prints the top-30 documents ranked by pagerank
	void printTop30(double[] pagerank, int numberOfDocs) {
		// create array of document indices
		Integer[] idx = new Integer[numberOfDocs];
		for (int i = 0; i < numberOfDocs; i++) {
			idx[i] = i;
		}

		final double[] finalPagerank = pagerank;
		// sort document indices by descending pagerank score
		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return Double.compare(finalPagerank[b], finalPagerank[a]);
			}
		});


		System.out.println("\nTop 30 PageRank pages:");
		// print top 30 document.
		for (int i = 0; i < 30 && i < numberOfDocs; i++) {
			int doc = idx[i];
			System.out.printf(
					"%2d. %30s %.5f\n",
					i + 1,
					docName[doc],
					pagerank[doc]
			);
		}

	}

	// 2.5
	/**
	 * write pagerank values for all document to a file
	 * each line: documentName pagerank score
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
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}