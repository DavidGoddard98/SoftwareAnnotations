package plantuml.statemachine.generation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

//code heavily influenced by - http://cogitolearning.co.uk/2013/04/writing-a-parser-in-java-the-tokenizer/

public class PatternIdentifier {
	
	public LinkedList<RegexInfo> patternStore;
	
	
	protected Pattern goodIfGuard = Pattern.compile("(if|\\}?\\s*else\\sif)\\s*\\(\\s*([a-zA-Z0-9\\s\\[\\];|&.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
	protected Pattern elseGuard = Pattern.compile("(\\}?\\s*else\\s)\\s*\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
	protected Pattern stateEqualityGuard = Pattern.compile( "(if|\\}?\\s*else\\sif)\\s*\\(\\s*(state)\\s+\\=\\=\\s+(valid_states\\.)([a-zA-Z0-9\\s\\[\\];|&\\.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
	protected Pattern stateInequalityGuard = Pattern.compile( "(if|\\}?\\s*else\\sif)\\s*\\(\\s*(state)\\s+\\!\\=\\s+(valid_states\\.)([a-zA-Z0-9\\s\\[\\];|&\\.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
    //SAME LINE IF CONDITIONALS?

	protected Pattern switchState = Pattern.compile("(switch)\\s*\\(\\s*(state)\\s*\\)\\s*\\{*([//]{2,}\\s*(.)*)*"); //switch(state) {
	protected Pattern stateChange = Pattern.compile( "([\\w]*\\s+)?(state)\\s*\\=\\s*(valid_states)\\.([a-zA-Z0-9_\\-.()]+)\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); //state = validStates. //group(4) =statNAme
	protected Pattern closedCurl = Pattern.compile("\\}([//]{2,}\\s*(.)*)*"); //}
	protected Pattern decleration = Pattern.compile( 	"([_\\.a-zA-Z0-9\\_\\(\\)])*\\s*(\\*)*(\\+)*(\\/)*(\\-)*\\s*([\\._\\-\\(\\)a-zA-Z0-9\\_\\(\\)])*\\s*\\=\\s*([\\._\\\\-\\\\(\\\\)a-zA-Z0-9\\_\\(\\)])*\\s*(\\*)*(\\+)*(\\/)*(\\-)*\\s*([\\._\\\\-\\\\(\\\\)a-zA-Z0-9\\_\\(\\)])*\\s*\\;\\s*([//]{2,}\\s*(.)*)*");

	protected Pattern simpleMethodCall = Pattern.compile("(([\\._\\-a-zA-Z0-9]*)\\s*\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); //getCall(); OR getCall(int something, int something); //group(1) = methodCall
	protected Pattern complexMethodCall = Pattern.compile( "(\\w)+\\s(\\w)+\\s*\\=\\s*(([\\._\\-a-zA-Z0-9]+)\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); // same as above but with String int getcall();//group(3) = methodcall
	protected Pattern potentialCallBack = Pattern.compile("(([\\w]+)\\.([\\._\\-a-zA-Z0-9]*)\\s*\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*");
    		
	protected Pattern caseState = Pattern.compile( 	"(case)\\s+([a-zA-Z0-9_\\-.()]*)\\s*\\:\\s*([//]{2,}\\s*(.)*)*" ); //case aState : //group(2) = state name
	protected Pattern breakRegex = Pattern.compile("(break)\\s*\\;([//]{2,}\\s*(.)*)*");  //break;

	protected Pattern fsmComment = Pattern.compile("(^\\s*//(FSM:))", Pattern.CASE_INSENSITIVE); //FSM:
	protected Pattern commentsPattern = Pattern.compile( 	"(//.*?$)|(/\\*.*?\\*/)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	protected Pattern whitespace = Pattern.compile("\\s*");

	protected Pattern methodDecleration = Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])([//]{2,}\\s*(.)*)*");
	protected Pattern methodDeclerationException = Pattern.compile("(public|protected|private|static|\\s+) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\)\\s+[\\w]*\\s+[\\w]* *(\\{?|[^;])([//]{2,}\\s*(.)*)*");
	
	protected Pattern whileLoop = Pattern.compile("while\\s*\\(([\\._\\-a-zA-Z0-9]*)\\)\\s*\\{*\\s*([//]{2,}\\s*(.)*)*"); //while (x) {
    
	
	public PatternIdentifier() {
		patternStore = new LinkedList<RegexInfo>();
		add(potentialCallBack, 0);
		add(goodIfGuard, 1);
		add(elseGuard, 2);
		add(switchState, 3);
		add(stateChange, 4);
		add(closedCurl, 5);
		
		add(decleration, 6);
		add(simpleMethodCall, 7);
		add(complexMethodCall, 8);
		
		add(caseState, 9);
		add(breakRegex, 10);
		add(whileLoop, 11);
		
		add(methodDecleration, 12);
		add(methodDeclerationException, 13);
		
		add(fsmComment, 14);
	}
	
	public void add(Pattern pattern, int identifier) {
		ArrayList<Integer> storedNumb = new ArrayList<Integer>();
		for (RegexInfo regex : patternStore) {
			storedNumb.add(regex.identifier);
		}
		if (!storedNumb.contains(identifier))  
			patternStore.add(new RegexInfo(pattern, identifier));
	}
	
	

	
	public class RegexInfo {
		 
		public Pattern pattern;
		public int identifier;
			
		public RegexInfo(Pattern pattern, int identifier) {
			this.pattern = pattern;
			this.identifier = identifier;
		}
	 }
	
	
	
	
	
}
