package plantuml.statemachine.generation;

import java.util.LinkedList;
import java.util.regex.Pattern;

//code heavily influenced by - http://cogitolearning.co.uk/2013/04/writing-a-parser-in-java-the-tokenizer/

public class PatternIdentifier {
	
	protected LinkedList<RegexInfo> patternStore;
	
	
	public PatternIdentifier() {
		patternStore = new LinkedList<RegexInfo>();
	}
	
	public void add(Pattern pattern, int identifier) {
	    patternStore.add(new RegexInfo(pattern, identifier));
	}
	
	
	protected class RegexInfo {
		 
		protected Pattern pattern;
		protected int identifier;
			
		public RegexInfo(Pattern pattern, int identifier) {
			this.pattern = pattern;
			this.identifier = identifier;
		}
	 }
	
	
	
	
	
}
