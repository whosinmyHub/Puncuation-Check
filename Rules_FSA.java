import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

class Rules_FSA implements Runnable{
	private Document inputDoc;
	private static Map<String, Map<String, String>> automaton;

	
	Rules_FSA () {
		
	}
	Rules_FSA (Document doc) {
		inputDoc = doc;
	}
	
	static {
		automaton = new HashMap<>();
		addTransition ("CC-[0-9]+", "(PRP-[0-9]+, VB.-[0-9]+)|(VB.-[0-9]+, PRP-[0-9]+)", ",");
		addTransition ("IN-[0-9]+", "(PRP-[0-9]+, VB.-[0-9]+)|(VB.-[0-9]+, PRP-[0-9]+)", ",");
		
	}
	
	private static void addTransition (String currentState, String input, String output) {
		automaton.computeIfAbsent (currentState, k -> new HashMap<>()).put(input, output);

	
	}
	
	// returns a list of string[] containing the sentence number in the paragraph/text, the position to insert the period, and the output char (which will be a period)
	List<String[][]> findMatchingKeysPeriod () {
		
		AtomicInteger sentCounter = new AtomicInteger();
		List<String[][]> pos = new ArrayList<>();
		
		for (Sentence sent : inputDoc.sentences()) {
			
			String dependencies = sent.dependencyGraph().toDotFormat();

			String[][] temp = Arrays.stream(dependencies.split(";"))
			.filter(dep -> dep.contains("parataxis"))
			.map(dep -> { 
				String str = dep.split("-> N_")[1]; 
				int endPos = str.indexOf("[");
				
				int numInSentence = Integer.parseInt(str.substring(0, endPos - 1)) - 2;
				String positionToInsertPeriod = sent.tokens().get(numInSentence).beginPosition() + "";

				String output = ".";
				String[] ret = {sentCounter.toString(), positionToInsertPeriod, output};
				
				return ret;
				})
			.toArray(String[][]::new);
			
			pos.add(temp);
			
			sentCounter.incrementAndGet();			
		}
		
		return pos;
	}
	
	List<String[][]> findMatchingKeysPeriodSent (Sentence sent, int sentCounter) {

		List<String[][]> pos = new ArrayList<>();
					
		String dependencies = sent.dependencyGraph().toDotFormat();

		String[][] temp = Arrays.stream(dependencies.split(";"))
								.parallel()
								.filter(dep -> dep.contains("parataxis"))
								.map(dep -> { 
									String str = dep.split("-> N_")[1]; 
									int endPos = str.indexOf("[");
									
									int numInSentence = Integer.parseInt(str.substring(0, endPos - 1)) - 2;
									String positionToInsertPeriod = sent.tokens().get(numInSentence).beginPosition() + "";
						
									String output = ".";
									String[] ret = {sentCounter + "", positionToInsertPeriod, output};
									
									return ret;
									})
								.toArray(String[][]::new);
		
		pos.add(temp);			
		
		
		return pos;
	}
	
	// returns a list where every even index is the sentence index & every odd index is the position in that sentence to insert the comma
	 List<String[]> findMatchingKeysComma () {
		List<String[]> positions = new ArrayList<>();
		
		AtomicInteger sentCounter = new AtomicInteger();
		for (Sentence sent : inputDoc.sentences()) {
			
			List<CoreLabel> list = sent.parse().taggedLabeledYield();

			for (int i = 0; i < list.size () - 2; i++) {
				
				if (!list.get(i).value().equals(",")) {
					
					var positionAndChar = computeNextState (list.get(i).toString (), list.get(i+1) + ", " + list.get(i+2));
					int numInSentence = 0;
					String punc = "";
					
					if (positionAndChar.length != 0) {
						numInSentence = Integer.parseInt (positionAndChar[0][0]);
						punc = positionAndChar[0][1];
					}
					
					if (numInSentence != 0) {
						String[] pos = new String[3];
						
						String positionToInsertComma = sent.tokens().get(numInSentence).beginPosition() + "";
						
						pos[0] = sentCounter + "";
						pos[1] = positionToInsertComma;
						pos[2] = punc;
						
						positions.add(pos);
					}
				}
			}
			
			sentCounter.incrementAndGet();
		}
		return positions;
	}
	
	
	// returns a list where every even index is the sentence index & every odd index is the position in that sentence to insert the comma
		public List<String[]> findMatchingKeysCommaSent (Sentence sent, int sentCounter) {
			List<String[]> positions = new ArrayList<>();
							
			List<CoreLabel> list = sent.parse().taggedLabeledYield();

			for (int i = 0; i < list.size () - 2; i++) {
				
				if (!list.get(i).value().equals(",")) {
					
					var positionAndChar = computeNextState (list.get(i).toString (), list.get(i+1) + ", " + list.get(i+2));
					int numInSentence = 0;
					String punc = "";
					
					if (positionAndChar.length != 0) {
						numInSentence = Integer.parseInt (positionAndChar[0][0]);
						punc = positionAndChar[0][1];
					}
					
					if (numInSentence != 0) {
						String[] pos = new String[3];
						
						String positionToInsertComma = sent.tokens().get(numInSentence).beginPosition() + "";
						
						pos[0] = sentCounter + "";
						pos[1] = positionToInsertComma;
						pos[2] = punc;
						
						positions.add(pos);
					}
				}
			}
							
			return positions;
		}
	
	// returns numInSentence, ex: currentState = CC-9 will return 9
	public String[][] computeNextState (String currentState, String input) {
		// Search if a key (rule) exists
		
		return automaton.keySet().parallelStream()
			.filter(key -> currentState.matches(key))
			.map(key -> automaton.get(key))
			.filter(value -> input.matches(value.keySet().iterator().next()))
			
			// return a pair containing the place value in the sentence to put the output String & the output String (the punctuation String to insert)
			.map (value -> 
				{ 
					String output = value.get(value.keySet().iterator().next()); 
					String[] pair = { currentState.substring(currentState.length() - 1), output }; 
					return pair; 
				} )
			.toArray(String[][]::new);
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
}
