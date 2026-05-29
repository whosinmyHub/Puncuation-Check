import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.Pair;

class Rules_Map {
	private Map<String, HashMap<String, Character>> automaton;

	
	Rules_Map () {
		
		automaton = new HashMap<>();
		addTransition ("CC-[0-9]+", "(PRP-[0-9]+, VB.-[0-9]+)|(VB.-[0-9]+, PRP-[0-9]+)", ',');
		addTransition ("IN-[0-9]+", "(PRP-[0-9]+, VB.-[0-9]+)|(VB.-[0-9]+, PRP-[0-9]+)", ',');
		addTransition ("RB-[0-9]+", "(PRP-[0-9]+, VB.-[0-9]+)|(VB.-[0-9]+, PRP-[0-9]+)", ',');

	}

/***************************************************************************************************************/
	
	/*
	 * addTransition : inserts a grammatical rule into the automaton map if it does not already exists
	 * 
	 *  
	 * parameters:
	 * currentState	: the key that begins the grammatical rule
	 * input		: what must come after currentState to complete the grammatical rule
	 * output		: the specific punctuation mark that this grammatical rule corresponds to
	 *
	 */
	private void addTransition (String currentState, String input, Character output) {
		automaton.computeIfAbsent (currentState, k -> new HashMap<>()).put(input, output);

	
	}
	
/***************************************************************************************************************/
	
	/*
	 * findMatchingKeysPeriodSent : scans the tokens of a Sentence's dependency graph 
	 * 								& checks if there exists any parataxis 
	 * 
	 * note: this method is not yet comprehensive enough to check for all period-punctuation errors
	 *  
	 * parameters:
	 * sent	: the Sentence to be reviewed
	 * 
	 * return: an ArrayList of the errors in the Sentence as Pairs of 
	 * 			Integer, representing the position to eventually input the char, 
	 * 			and Character, representing the period punctuation mark 
	 * 
	 */
	ArrayList<Pair<Integer, Character>> findMatchingKeysPeriodSent (Sentence sent, int startPos) {
		
		String dependencies = sent.dependencyGraph().toDotFormat();
		IO.println (dependencies);
		
		var split = dependencies.split(";");
		
		ArrayList<Pair<Integer, Character>> info = new ArrayList<>();

		for (int i = 0; i < split.length; i++) {
			if (split[i].contains("parataxis")) {
				
				String SecondHalfOfString = split[i].split("-> N_")[1];
				int endPos = SecondHalfOfString.indexOf("[");
				
				int numInSentence = Integer.parseInt(SecondHalfOfString.substring(0, endPos - 1)) - 2;
				int positionToInsertPeriod = sent.tokens().get(numInSentence).beginPosition() + startPos;
				
				info.add (new Pair<>(positionToInsertPeriod, '.'));

			}
		}
		
		return info;
	}
	
	
/***************************************************************************************************************/
	
	/*
	 * findMatchingKeysCommaSent : scans the tokens of a Sentence's part-of-speech tagging yield
	 * 								& checks if there exists any errors as defined in the automaton map
	 * 
	 * note: the automaton map is not yet comprehensive enough to check for all comma-punctuation errors
	 *  
	 * parameters:
	 * sent	: the Sentence to be reviewed
	 * 
	 * return: an ArrayList of the errors in the Sentence as Pairs of 
	 * 			Integer, representing the position to eventually input the char, 
	 * 			and Character, representing the period punctuation mark 
	 * 
	 */
	  ArrayList<Pair<Integer, Character>> findMatchingKeysCommaSent (Sentence sent, int startPos) {

			List<CoreLabel> list = sent.parse().taggedLabeledYield();

			ArrayList<Pair<Integer, Character>> info = new ArrayList<>();
			
			for (int i = 0; i < list.size() - 2; i++) {
				Pair<Integer, Character> posAndChar = computeNextState (list.get(i).toString(), list.get(i + 1) + ", " + list.get(i + 2));
				
				if (posAndChar != null) {
					int numInSentence = posAndChar.first();
					Character punc = posAndChar.second();
					
					int positionToInsertComma = sent.tokens().get(numInSentence).beginPosition() + startPos;

					info.add(new Pair<>(positionToInsertComma, punc));
				}
			}
			
			return info;
	 }
			

 /***************************************************************************************************************/
	
	/*
	 * computeNextState : checks if there exists a defined value in the automaton 
	 * 						map for a current state and desired input
	 * 
	 * note: the automaton map is not yet comprehensive enough to check for all comma-punctuation errors
	 *  
	 * parameters:
	 * currentState	: the key which will either have corresponding value in the map, 
	 * 					indicating there might be a punctuation error, or will not
	 * 
	 * input		: the value which might be apart of the corresponding value to currentState or not
	 * 
	 * return: a pair of Integer, the index of the word (which currentState refers to) 
	 * 			in the sentence (ex: currentState == CC-9 then Integer = 9), and a Character, which
	 * 			is the mapped-to punctuation mark. OR return null if there exists no accepted pathway in the map
	 * 
	 */
	public Pair<Integer, Character> computeNextState (String currentState, String input) {

			for (var key : automaton.keySet ()) {
				if (currentState.matches (key) && input.toString ().matches (automaton.get (key).keySet ().iterator ().next ())) {
					String value = automaton.get (key).keySet ().iterator ().next ();
					
					int numInSentence = Integer.parseInt (currentState.substring(currentState.length() - 1));
					Character puncMark = automaton.get (key).get (value);
					
					return new Pair<Integer, Character> (numInSentence, puncMark);
					}
				
				}
	
			return null;
			}

}
		automaton.computeIfAbsent (currentState, k -> new HashMap<>()).put(input, output);

	
	}
	
/***************************************************************************************************************/
	
	/*
	 * findMatchingKeysPeriodSent : scans the tokens of a Sentence's dependency graph 
	 * 								& checks if there exists any parataxis 
	 * 
	 * note: this method is not yet comprehensive enough to check for all period-punctuation errors
	 *  
	 * parameters:
	 * sent	: the Sentence to be reviewed
	 * 
	 * return: an ArrayList of the errors in the Sentence as Pairs of 
	 * 			Integer, representing the position to eventually input the char, 
	 * 			and Character, representing the period punctuation mark 
	 * 
	 */
	ArrayList<Pair<Integer, Character>> findMatchingKeysPeriodSent (Sentence sent, int startPos) {
		
		String dependencies = sent.dependencyGraph().toDotFormat();
		IO.println (dependencies);
		
		var split = dependencies.split(";");
		
		ArrayList<Pair<Integer, Character>> info = new ArrayList<>();

		for (int i = 0; i < split.length; i++) {
			if (split[i].contains("parataxis")) {
				
				String SecondHalfOfString = split[i].split("-> N_")[1];
				int endPos = SecondHalfOfString.indexOf("[");
				
				int numInSentence = Integer.parseInt(SecondHalfOfString.substring(0, endPos - 1)) - 2;
				int positionToInsertPeriod = sent.tokens().get(numInSentence).beginPosition() + startPos;
				
				info.add (new Pair<>(positionToInsertPeriod, '.'));

			}
		}
		
		return info;
	}
	
	
/***************************************************************************************************************/
	
	/*
	 * findMatchingKeysCommaSent : scans the tokens of a Sentence's part-of-speech tagging yield
	 * 								& checks if there exists any errors as defined in the automaton map
	 * 
	 * note: the automaton map is not yet comprehensive enough to check for all comma-punctuation errors
	 *  
	 * parameters:
	 * sent	: the Sentence to be reviewed
	 * 
	 * return: an ArrayList of the errors in the Sentence as Pairs of 
	 * 			Integer, representing the position to eventually input the char, 
	 * 			and Character, representing the period punctuation mark 
	 * 
	 */
	  ArrayList<Pair<Integer, Character>> findMatchingKeysCommaSent (Sentence sent, int startPos) {

			List<CoreLabel> list = sent.parse().taggedLabeledYield();
			
			ArrayList<Pair<Integer, Character>> info = new ArrayList<>();
			
			for (int i = 0; i < list.size() - 2; i++) {
				Pair<Integer, Character> posAndChar = computeNextState (list.get(i).toString(), list.get(i + 1) + ", " + list.get(i + 2));
				
				if (posAndChar != null) {
					int numInSentence = posAndChar.first();
					Character punc = posAndChar.second();
					
					int positionToInsertComma = sent.tokens().get(numInSentence).beginPosition() + startPos;

					info.add(new Pair<>(positionToInsertComma, punc));
				}
			}
			
			return info;
	 }
			

 /***************************************************************************************************************/
	
	/*
	 * computeNextState : checks if there exists a defined value in the automaton 
	 * 						map for a current state and desired input
	 * 
	 * note: the automaton map is not yet comprehensive enough to check for all comma-punctuation errors
	 *  
	 * parameters:
	 * currentState	: the key which will either have corresponding value in the map, 
	 * 					indicating there might be a punctuation error, or will not
	 * 
	 * input		: the value which might be apart of the corresponding value to currentState or not
	 * 
	 * return: a pair of Integer, the index of the word (which currentState refers to) 
	 * 			in the sentence (ex: currentState == CC-9 then Integer = 9), and a Character, which
	 * 			is the mapped-to punctuation mark. OR return null if there exists no accepted pathway in the map
	 * 
	 */
	public Pair<Integer, Character> computeNextState (String currentState, String input) {

			for (var key : automaton.keySet ()) {
				if (currentState.matches (key) && input.toString ().matches (automaton.get (key).keySet ().iterator ().next ())) {
					String value = automaton.get (key).keySet ().iterator ().next ();
					
					int numInSentence = Integer.parseInt (currentState.substring(currentState.length() - 1));
					Character puncMark = automaton.get (key).get (value);
					
					return new Pair<Integer, Character> (numInSentence, puncMark);
					}
				
				}
	
			return null;
			}

}
