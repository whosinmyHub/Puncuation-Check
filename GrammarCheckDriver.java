
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.util.Pair;

class GrammarCheckDriver {

	private StringBuffer inputText;
	private AtomicInteger reverse;
	private ExecutorService execPunc;
	private ExecutorService execSent;
	private ConcurrentLinkedQueue<Pair<Integer, Character>> puncErrors;
	private Rules_Map rules;
	
	GrammarCheckDriver () {
		
		inputText = new StringBuffer ();
		reverse = new AtomicInteger(1);
		
		puncErrors = new ConcurrentLinkedQueue<>();
		rules = new Rules_Map ();
		execPunc = Executors.newVirtualThreadPerTaskExecutor();
		execSent = Executors.newVirtualThreadPerTaskExecutor();

		readText ();

		IO.println (inputText);
	}
	
/***************************************************************************************************************/
	
	/*
	 * insertQueue : inserts all punctuation errors into the Concurrent Linked Queue 
	 * 
	 * parameters:
	 * err	: an ArrayList of Pairs of an Integer, representing the position to insert the char, and 
	 * 			Character, representing the punctuation mark to be inserted
	 * 
	 */
	private void insertQueue (ArrayList<Pair<Integer, Character>> err) {
		for (Pair<Integer, Character> p : err) 
			puncErrors.add(p);
	}

  
/***************************************************************************************************************/

    /*
	 * puncCheck : inserts the sentence into a CompletableFuture to check for individual kinds of punctuation errors,
	 * 				then pushes the result into the queue holding all errors,
	 * 				then calls the function that inserts the punctuation char in the correct spot
	 * 
	 * parameters:
	 * sent		: the Sentence to be checked for errors
	 * startPos	: the original starting postition in the StringBuffer for the sentence being examined

	 * return: a CompletableFuture that returns when all the other CompletableFutures have completed
	 * 
	 */
    private void puncCheck (Sentence sent, int startPos) {

    	var period = CompletableFuture
				.supplyAsync (() -> { return rules.findMatchingKeysPeriodSent(sent); }, execPunc)
				.thenAccept(result -> { insertQueue (result); })
				.thenAccept(voidRes -> { insertPunc (startPos); })
				.exceptionally(ex -> { ex.printStackTrace(); return null; });   					
				
		
		var comma = CompletableFuture
				.supplyAsync (() -> { return rules.findMatchingKeysCommaSent(sent); }, execPunc)
				.thenAccept(result -> { insertQueue (result); })
				.thenAccept(voidRes -> { insertPunc (startPos); })
				.exceptionally(ex -> { ex.printStackTrace(); return null; });   					

		
		
		CompletableFuture.allOf(period, comma).join();
    }
 
/***************************************************************************************************************/

    /*
	 * insertPunc : grabs the head of the queue holding the punctuation errors
	 * 				 and inserts the punctuation char in the correct spot 
	 * 
	 * parameters:
	 * startPos	: the original starting postition in the StringBuffer for the sentence being corrected
	 * 
	 */
	private void insertPunc (int startPos) {
    	
    	Pair<Integer, Character> pair = puncErrors.poll();

    	if (pair != null) {
	    	
	    	inputText.insert(pair.first() - reverse.intValue() + startPos, pair.second());
	    	reverse.decrementAndGet();
	    	}
    }
   
/***************************************************************************************************************/

	/*
	 * readText : reads from the Input.txt file, character by character, and inputs that into the StringBuffer, inputText.
	 * 				Once a period mark has been detected, a Sentence object is created of the whole sentence
	 * 				and sent to puncCheck through a CompletableFuture. Sentences are, therefore, being read 
	 * 				from the input file, written into the StringBuffer, and sent for punctuation analysis
	 * 				concurrently.
	 * 
	 * return: returns the StringBuffer obj, inputText, as a String
	 */
	private void readText () {
		
		
		try (Reader reader = new BufferedReader (new FileReader (new File ("Input.txt")))) {
			
			int startPos = 0;
			int endPos = 0;
			int readChar = reader.read ();
			
			while (readChar != -1 ) {

				while ((char) readChar != '.' && readChar != -1) {
					inputText.append((char) readChar); 
					
					endPos++;
					readChar = reader.read();
				}
				
				inputText.append((char) readChar); 
				
				String text = inputText.substring(startPos, endPos).toString();
				Sentence sent = new Sentence (text);
				
				reader.read ();
				int originalSentStart = startPos;
				CompletableFuture.runAsync (() -> puncCheck (sent, originalSentStart), execSent);

				startPos = endPos;
				readChar = reader.read();

				}
			
			execSent.close();
			reader.close();

						
		}  catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}


/***************************************************************************************************************/

	static void main () {
		new GrammarCheckDriver ();
	}
}
