import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
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
	private ConcurrentSkipListMap<Integer, Character> puncErrors;
	private Rules_Map rules;
	
	GrammarCheckDriver () {
		
		inputText = new StringBuffer ();
		reverse = new AtomicInteger(1);
		
		puncErrors = new ConcurrentSkipListMap<>();
		rules = new Rules_Map ();
		execPunc = Executors.newVirtualThreadPerTaskExecutor();
		execSent = Executors.newVirtualThreadPerTaskExecutor();

		readText ();

		IO.println (inputText);
	}
	
/***************************************************************************************************************/
	
	/*
	 * insertQueue : inserts all punctuation errors into the Concurrent Skip List Map, a thread-safe map that maintains the order of the keys 
	 * 
	 * parameters:
	 * err	: an ArrayList of Pairs of an Integer, representing the position to insert the char, and 
	 * 			Character, representing the punctuation mark to be inserted
	 * 
	 */
	private void insertQueue (ArrayList<Pair<Integer, Character>> err) {
		for (Pair<Integer, Character> p : err)  
			puncErrors.put(p.first(), p.second());
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
				.supplyAsync (
						() -> { return rules.findMatchingKeysPeriodSent(sent, startPos); }, execPunc)
				.thenAccept(
						result -> { insertQueue (result); })
				.exceptionally(
						ex -> { ex.printStackTrace(); return null; });   					
				
		
		var comma = CompletableFuture
				.supplyAsync (
						() -> { return rules.findMatchingKeysCommaSent(sent, startPos); }, execPunc)
				.thenAccept(
						result -> { insertQueue (result); })
				.exceptionally(
						ex -> { ex.printStackTrace(); return null; });   	
		
		CompletableFuture.allOf(period, comma).join();

    }
 
/***************************************************************************************************************/

    /*
	 * insertPunc : reverses the order of the map's keys and inserts the punctuation  
	 * 
	 */
	private void insertPunc () {
		
		for (var p : puncErrors.descendingMap().entrySet())
			inputText.insert(p.getKey().intValue() - 1, p.getValue());

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
			int readChar;
			
			while ((readChar = reader.read ()) != -1 ) {

				char c = (char) readChar;
				inputText.append(c);
				++endPos;
				
				if (c == '.') {
					Sentence sent = new Sentence (inputText.substring(startPos, endPos));
					int originalSentStart = startPos;

					CompletableFuture.runAsync (
							() -> puncCheck (sent, originalSentStart), execSent)
					.exceptionally(
							ex -> { ex.printStackTrace(); return null;});
					
					startPos = endPos;

				}
			}
			
			execSent.close();
			insertPunc ();

						
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
