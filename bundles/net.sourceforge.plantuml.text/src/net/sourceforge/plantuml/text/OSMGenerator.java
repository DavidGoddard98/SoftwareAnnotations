package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import net.sourceforge.plantuml.text.PatternIdentifier.RegexInfo;
import net.sourceforge.plantuml.text.StateTextDiagramHelper.PendingState;
import net.sourceforge.plantuml.text.StateTree.TransitionInformation;

public class OSMGenerator {
	
	final String prefix = "@start_OSM_generation", prefixRegex = prefix;
	final String suffix = "@end_OSM_generation", suffixRegex = suffix;
	
	private boolean patternsInitialized = false;
	private PatternIdentifier patternIdentifier;
	
    Pattern goodIfGuard = Pattern.compile("(if|\\}?\\s*else\\sif)\\s*\\(\\s*([a-zA-Z0-9\\s\\[\\];|&.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
    Pattern stateEqualityGuard = Pattern.compile( "(if|\\}?\\s*else\\sif)\\s*\\(\\s*(state)\\s+\\=\\=\\s+(valid_states\\.)([a-zA-Z0-9\\s\\[\\];|&\\.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
    Pattern stateInequalityGuard = Pattern.compile( "(if|\\}?\\s*else\\sif)\\s*\\(\\s*(state)\\s+\\!\\=\\s+(valid_states\\.)([a-zA-Z0-9\\s\\[\\];|&\\.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
    //SAME LINE IF CONDITIONALS?

    Pattern switchState = Pattern.compile("(switch)\\s*\\(\\s*(state)\\s*\\)\\s*\\{*([//]{2,}\\s*(.)*)*"); //switch(state) {
    Pattern stateChange = Pattern.compile( "([\\w]*\\s+)?(state)\\s*\\=\\s*(valid_states)\\.([a-zA-Z0-9_\\-.()]+)\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); //state = validStates. //group(4) =statNAme
    Pattern closedCurl = Pattern.compile("\\}([//]{2,}\\s*(.)*)*"); //}
    Pattern decleration = Pattern.compile( 	"([_\\.a-zA-Z0-9\\_\\(\\)])*\\s*(\\*)*(\\+)*(\\/)*(\\-)*\\s*([\\._\\-\\(\\)a-zA-Z0-9\\_\\(\\)])*\\s*\\=\\s*([\\._\\\\-\\\\(\\\\)a-zA-Z0-9\\_\\(\\)])*\\s*(\\*)*(\\+)*(\\/)*(\\-)*\\s*([\\._\\\\-\\\\(\\\\)a-zA-Z0-9\\_\\(\\)])*\\s*\\;\\s*([//]{2,}\\s*(.)*)*");

    Pattern simpleMethodCall = Pattern.compile("(([\\._\\-a-zA-Z0-9]*)\\s*\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); //getCall(); OR getCall(int something, int something); //group(1) = methodCall
    Pattern complexMethodCall = Pattern.compile( "(\\w)+\\s(\\w)+\\s*\\=\\s*(([\\._\\-a-zA-Z0-9]+)\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*"); // same as above but with String int getcall();//group(3) = methodcall
    Pattern potentialCallBack = Pattern.compile("(([\\w]+)\\.([\\._\\-a-zA-Z0-9]*)\\s*\\((.)*\\))\\s*\\;\\s*([//]{2,}\\s*(.)*)*");
    		
    Pattern caseState = Pattern.compile( 	"(case)\\s+([a-zA-Z0-9_\\-.()]*)\\s*\\:\\s*([//]{2,}\\s*(.)*)*" ); //case aState : //group(2) = state name
    Pattern breakRegex = Pattern.compile("(break)\\s*\\;([//]{2,}\\s*(.)*)*");  //break;

    Pattern fsmComment = Pattern.compile("(^\\s*//(FSM:))", Pattern.CASE_INSENSITIVE); //FSM:
    Pattern commentsPattern = Pattern.compile( 	"(//.*?$)|(/\\*.*?\\*/)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Pattern whitespace = Pattern.compile("\\s*");

    Pattern methodDecleration = Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])([//]{2,}\\s*(.)*)*");
    Pattern methodDeclerationException = Pattern.compile("(public|protected|private|static|\\s+) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\)\\s+[\\w]*\\s+[\\w]* *(\\{?|[^;])([//]{2,}\\s*(.)*)*");
	
    Pattern whileLoop = Pattern.compile("while\\s*\\(([\\._\\-a-zA-Z0-9]*)\\)\\s*\\{*\\s*([//]{2,}\\s*(.)*)*"); //while (x) {
    
	public OSMGenerator() {
		
	}
	static ArrayList<String> operators = new ArrayList<String>(
            Arrays.asList("==",
                          "!=",
                          ">",
                          "<",
                          ">=",
                          "<="));
	private static String negateCondition(String string) {
 		StringBuilder negation = new StringBuilder();
 		String tmp = "p";
 		negation.append(string);
 		for (int j=0; j<string.length(); j++) {
 			String c = String.valueOf(string.charAt(j));
 			String oldCAndNewC = tmp + c;
 			if (operators.contains(c)) {
 				if (c.equals(">")) {
 					negation.replace(j, j+1, "<=");
 					break;
 				}
 				else {
 					negation.replace(j, j+1, ">=");
 					break;
 				}
 			} else if (operators.contains(oldCAndNewC)) {
 				String negatedRelation = negateRelation(oldCAndNewC);
 				negation.replace(j-1, j+1, negatedRelation);
 			}
 			tmp = c;
 		}
 		if (negation.toString().equals(string)) { //no relational operator found
 			if (string.contains("!")) {
        return string.replaceAll("!", "");
      } else {
 			negation.insert(0, "!(");
 			negation.append(")");
      }
    }
 		return negation.toString();

	}

 	private static String negateRelation(String relation) {
 		if (relation.equals("==")) return "!=";
 		else if (relation.equals("!=")) return "==";
 		else if (relation.equals(">=")) return "<";
 		else  return ">";
 	}
	
	
	ArrayList<String> storedEvents;
	ArrayList<String> methodCalls;
	ArrayList<String> declaredMethods;
	ArrayList<String> exitConditions;
	ArrayList<String> invisibleNodes;
	Stack<String> currentBlock;
	Stack<String> events;
	Stack<Node> visibleStates;
	
	Node whileState;
	
	Stack<String> stateFound;
	Stack<String> ignoreStack;
	
	String initialState = null;
	String caseName = null;
	StringBuilder result;
	StateTree theTree;
	StateStore stateStore = null;
	StateStore unconditionalState = null;
	StringBuilder transition;
	//conditional
	//switch-state
	//case-state
	
	boolean callBack = false;
	boolean ignore = false;
	boolean stopIgnoring = false;
	boolean switchStateActive = false;
	boolean unConditionalState = false;
	boolean drawTree = false;
	boolean selfLoop = false;
	boolean afterLoopState = false;
	boolean nextLineConditionalValidate = false;
	boolean oneLineConditional = false;
	
	
	
	
	private void identifyPattern(String line, int lineNum) {
		System.out.println();
		for (int k = 0; k<currentBlock.size(); k++) { 
			System.out.println(currentBlock.get(k));
		}
		if (ignore) {
			if (line.contains("{")) {
				ignoreStack.push("ignore");
				return;
			} else if (line.contains("}") && ignoreStack.size() > 1) {
				ignoreStack.pop();
				return;
			}
			else if (line.contains("}") && ignoreStack.size() == 1) {
				ignoreStack.clear();
				ignore = false;
			} else
				return;
		}
		
		if (oneLineConditional) {
			currentBlock.pop();
			events.pop();
			oneLineConditional = false;
		}
		
		if (nextLineConditionalValidate) {
			if (!line.matches("\\s*\\{\\s*([//]{2,}\\s*(.)*)*")) {
				oneLineConditional = true;
			}
			nextLineConditionalValidate = false;
		}
		
		
		for (RegexInfo info : patternIdentifier.patternStore) {
			Matcher m = info.pattern.matcher(line);
			if (m.matches()) {
				
				normalFlow(info.identifier, line, lineNum, m);
				
			} else if (m.find() && info.identifier == 13) { //fsm comment
				System.out.println("fsm comment " + line);
				String removeFSM = line.substring(6);
				if (removeFSM.contains("EXIT")) {
					removeFSM = removeFSM.replaceAll("\\s*(EXIT)\\s*-\\s*", "");
					System.out.println(removeFSM);
					exitConditions = new ArrayList<String>(Arrays.asList(removeFSM.split("/")));
					System.out.println(exitConditions.get(0));
				} else {
					result.append(line.substring(6));
					result.append("\n");
				}
				
			}
		}
				
	}
	
	

	private StringBuilder buildTransitionsFromTree(StateTree theTree, boolean lastTree) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		StringBuilder result = new StringBuilder();
		String saveTrans = "";
		nodes = theTree.nodes;
		for (int j = 0; j<visibleStates.size(); j++) {
			
			Node from = visibleStates.get(j);
			nodes.remove(from);
			
			for (int i = 0 ; i<nodes.size(); i++) {
				
				Node to = nodes.get(i);
				if (to.index < from.index) continue;
				
				TransitionInformation transitionInformation = theTree.getRoute(from, to);
				if (transitionInformation == null) continue;
				
				StringBuilder transition = new StringBuilder();

				for (Node node : transitionInformation.route ) {
					if (node.event.isEmpty()) continue;
					if (node.event.equals("unconditional")) continue;
					transition.append(node.event + " && ");
				}
				
				for (Node node : transitionInformation.metStates) {
					if (node.event.isEmpty()) continue;
					if (node.event.equals("unconditional")) continue;

					transition.append(negateCondition(node.event) + " && ");
				}
				
				if (transition.length() > 3)
				transition.delete(transition.length()-3, transition.length());
				if (transition.length() == 0) transition.append("No event found");
				if (from.equals(theTree.root)) saveTrans = transition.toString();
			
				

				result.append(from.stateName + " -down-> " + to.stateName + " : " + transition + "\n"); 
				
			}
		}
		if (lastTree) {
			
		
			Node exitNode = theTree.getNode("[*]");
			
			for (Node invisibleNode : theTree.nodes) {
				
				if (invisibleNode.visible) continue;
				TransitionInformation transitionInformation = theTree.getRoute(invisibleNode, exitNode);
				if (transitionInformation == null) continue;
				StringBuilder transition = new StringBuilder();

				for (Node node : transitionInformation.route ) {
					if (node.event.isEmpty()) continue;
					transition.append(node.event + " && ");
				}
				
				for (Node node : transitionInformation.metStates) {
					if (node.event.isEmpty()) continue;
					transition.append(negateCondition(node.event) + " && ");
				}
				if (transition.length() > 3)
				transition.delete(transition.length()-3, transition.length());
				if (transition.length() == 0) transition.append("No event found");

			
				

				result.append(invisibleNode.stateName + " -down-> [*] : " + transition + "\n"); 
				
			}
		}
		return result;
	}

	private void addBackLogNode() {
		if (stateStore != null) {
			Node node = new Node(stateStore.name,stateStore.parent, stateStore.event, callBack);
			theTree.addNode(stateStore.parent, node);
			if (callBack) visibleStates.push(node);
		}
		callBack = false;
	}
	
	
	
	private void normalFlow(int patternNo, String line, int lineNum, Matcher m) {
		switch(patternNo) {

			case 0: //call back...
				//remove obvious once that arnt there.
				System.out.println("found a callback: " + line);
				if (line.contains("Systen.out.print")) break;
				callBack=true;
				
				
				break;
			case 1: //complex if guard
				 System.out.println("good if guard " + line + "statement: " + m.group(2)) ;
				 String expression = m.group(2);
				 
				 
					 
				 if (!m.group(3).equals("{")) { //check to see if the bracket is on the next line...
					 nextLineConditionalValidate = true;
				 }
				
				 if (!expression.replaceAll("(state)\\s+\\=\\=\\s+(valid_states\\.)", "").equals(expression)) {
					 currentBlock.push("conditional-state");
					 events.push(m.group(2)); //the condition
					 visibleStates = new Stack<Node>();
					 boolean equalityStatesFound = false;
					 System.out.println("here");
					 String aString = expression.replaceAll("(state)\\s+\\=\\=\\s+(valid_states\\.)", "equality");
					 //STOP SELF LOOPING
				   	 while (!equalityStatesFound) {
				   		 
				         if (aString.indexOf("equality") != -1) {
				        	 
		
				             int index = aString.indexOf("equality");
				        	 int anotherIndex = aString.indexOf(" ", index);
				        	 if (anotherIndex == -1) anotherIndex = aString.length();
				        	 String aState = aString.substring(index + 8, anotherIndex);
				        	 if (aState.equals("")) break;
				        	 System.out.println("pushing this state onto visible states : " + aState);
				        	// visibleStates.push(aState.trim());
				        	 aString = aString.substring(aString.indexOf(aState));
				         } else equalityStatesFound = true;
				   	 }
					
				 }else {
					 currentBlock.push("conditional");
					 events.push(m.group(2)); //the condition
				 }
				
						//equality state conditional
		//			 } else if (!anotherExpression.replaceAll("(state)\\s+\\!=\\=\\s+(valid_states\\.)", "inEquality").equals(anotherExpression)) {
		//				
		//				 boolean inEqualityStatesFound = false;
		//
		//				 
		//			   	 while (!inEqualityStatesFound) {
		//			   		 
		//			         if (anotherExpression.indexOf("inEquality") != -1) {
		//			        	 
		//
		//			             int index = anotherExpression.indexOf("inEquality");
		//			        	 int anotherIndex = anotherExpression.indexOf(" ", index);
		//			        	 if (anotherIndex == -1) anotherIndex = anotherExpression.length();
		//			        	 String aState = anotherExpression.substring(index + 8, anotherIndex);
		//			        	 visibleStates.push(aState.trim());
		//			        	 anotherExpression = anotherExpression.substring(anotherExpression.indexOf(aState));
		//			         } else inEqualityStatesFound = true;
		//			   	 }
						
				
				 
				 
			
			 
				
				break;
			case 2: //switch state
				
				
				currentBlock.push("switch-state");
				switchStateActive = true;
				System.out.println("SWITCHING TO CONTROL LOOP");
				visibleStates.clear();
				return;
			case 3: //state change
				System.out.println("state Change: " + line + "the State: " + m.group(4));
				drawTree = true;
				if (initialState == null) {
					
					initialState = m.group(4);
					Node node = new Node(initialState, null, "", true);
					theTree = new StateTree(node);
					stateFound.push(initialState);
					visibleStates.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
		
					break;
				} else if (afterLoopState) {
					
					
					result.append(whileState.stateName + " -> " + m.group(4) + " : Exit loop" + "\n");
					Node node = new Node(m.group(4), null, "", true);
					theTree = new StateTree(node);

					stateFound.push(m.group(4));
					visibleStates.push(node);
					afterLoopState = false;
					break;
				}
				
				if (unConditionalState) {
					//IF THIS IS VISIBLE
					if (callBack) {
						//AS END OF OLD TREE
						//Loop through all states and add them as a parent
						for (Node invisibleNode : theTree.nodes)
							if (!invisibleNode.visible) invisibleNodes.add(invisibleNode.stateName);
						Node node = new Node(unconditionalState.name, theTree.root, "unconditional", true);

						theTree.addNode(theTree.root, node);
						result.append(buildTransitionsFromTree(theTree, false));
						//AS ROOT FOR NEW TREE
						node = new Node(unconditionalState.name, null, "", true);
						visibleStates.clear();
						stateFound.clear();
						theTree = new StateTree(node);
						visibleStates.push(node);
						Node parentNode = node;
						if (!events.empty())  stateStore = new StateStore(m.group(4), events.peek(), parentNode);
						else stateStore = new StateStore(m.group(4), "", parentNode);
						stateFound.push(m.group(4));
						unConditionalState = false;
						callBack = false;
						break;
					}
					//IF INVISIBLE
					//Add as child to all other nodes with empty transition? - not sure
					//ignoring if that uncodnital state isnt visible atm...
					stateFound.pop();
					unConditionalState = false;
				} else {
					addBackLogNode();

				}
				
				if (!visibleStates.isEmpty()) {
					 if (currentBlock.empty()) {
						unConditionalState = true;
						
						unconditionalState = new StateStore(m.group(4), "", null);
						stateFound.push(m.group(4));
	
					
					}else if (currentBlock.peek().equals("conditional")) { 
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("state-conditional"); //therefore speicify this
						
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore = new StateStore(m.group(4), events.peek(), parentNode);
						stateFound.push(m.group(4));
		

					} else if (currentBlock.peek().equals("state-conditional")) {
						
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore  = new StateStore(m.group(4), "unconditional", parentNode);
					}  else if (currentBlock.peek().equals("case-state")) {
						Node parentNode = theTree.getNode(stateFound.peek());
						stateStore = new StateStore(m.group(4), "", parentNode);
						stateFound.push(m.group(4));
						callBack = true;
					}
						
					

							
				}
		
				break;
			case 4: //closed }
				
				if (!currentBlock.isEmpty()) {
					if (currentBlock.peek().equals("conditional-state")) {
//						visibleStates = savedVisibleStates;
						currentBlock.pop();
					} else if (currentBlock.peek().equals("state-conditional")) {
						stateFound.pop();
						currentBlock.pop();
						if (!currentBlock.contains("state-conditional") && stateFound.size() > 1) {
							stateFound.pop();
						}
					} else if (currentBlock.peek().equals("switch-state")) {
						switchStateActive=false;
						currentBlock.pop();
					} else if (currentBlock.peek().equals("while-loop")) {
						afterLoopState = true;
						result.append("}" + "\n");
						currentBlock.pop();
					} else {
						currentBlock.pop();
					}
					
				}
				
				
				
				if (!events.isEmpty()) {
					events.pop();
					
				}
				
				
				break;
			case 5: //decleration
				System.out.println("decleration: " + line);
				
				break;
			case 6: //simpleMethodCal
				int index = m.group(1).indexOf("(");
				String method = m.group(1).substring(0, index);
				if (declaredMethods.contains(method) && !methodCalls.contains(m.group(1))){
					methodCalls.add(m.group(1));
					System.out.println("addding :" + m.group(1));
				}
				if (exitConditions!= null && exitConditions.contains(m.group(1))) {
					selfLoop = false;
					result = result.append(caseName + " -down-> [*] : " + m.group(1) + "\n");
					
				}
				
			
				
				System.out.println("simple method call: " + line + "method: " + m.group(1));
				break;
			case 7: //complexMethodcall
				System.out.println("complex method call: " + line + "method: " + m.group(3));
				
				break;
			case 8: //case 
				visibleStates.clear();
				currentBlock.push("case-state"); //we know that the case is a state...
				storedEvents.clear();
				stateFound.clear();
				stateStore = null;
				if (currentBlock.contains("while-loop")) selfLoop = true;
				if (m.group(2).equals("INIT")) {
					initialState = m.group(2);
					
					Node node = new Node(m.group(2), null, "", true);
					theTree = new StateTree(node);
					stateFound.push(initialState);
					visibleStates.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
					caseName = "INIT";
				}
				else {
					Node node = new Node(m.group(2), null, "", true);
					theTree = new StateTree(node);
					stateFound.push(m.group(2));
					visibleStates.push(node);
					result.append("state " + m.group(2));
					result.append("\n");
					caseName = m.group(2);
				}
				

					
				System.out.println("caseState: " + line + "the stateName: " + m.group(2));
				
				
				break;
			case 9:  //break regex
				if (currentBlock.peek() == "conditional") {
					//probably dont need the .contains("case-state") above...
					ignore = true;
					ignoreStack.push("ignore");
					break;
					
				
				} else if (currentBlock.peek() == "case-state") {
					addBackLogNode();

					if (currentBlock.contains("while-loop") && !caseName.equals("INIT")) {
						transition = new StringBuilder();

						Node node = theTree.getNode(caseName);
						
						for (Node child : theTree.getChildren(node)) {
							if (child.event.equals("unconditional")) selfLoop = false;
							transition.append(negateCondition(child.event));
						}
						
					
						if (selfLoop) {
							if (transition.length() == 0) transition.append("No event found");
							result.append(caseName + " -> " + caseName + " : " + transition);
							result.append("\n");
							for (String methodCall : methodCalls) {
								result.append("state " + caseName + " : " + methodCall + ";");
								result.append("\n");
							}
						}
						
					}
					result.append(buildTransitionsFromTree(theTree, false));
					drawTree = false;
					methodCalls = new ArrayList<String>();
					visibleStates.clear();
					while(currentBlock.peek() != "case-state") currentBlock.pop(); 
					currentBlock.pop();
				}
				
				break;
				
				
				
				
				
				
			case 10: //whileLoop
				currentBlock.push("while-loop");
				addBackLogNode();
				whileState = new Node(m.group(1), theTree.findLastUnconditionalState(), "unconditional", true);	
				
				theTree.addNode(theTree.findLastUnconditionalState(), whileState);
				result.append(buildTransitionsFromTree(theTree, false));
				result.append("state " + m.group(1) + " { " + "\n"); 
				result.append("state \" WHILE LOOP:  " + m.group(1) + "\" as " + m.group(1) + "\n");
			case 11: //methodDecleration
				System.out.println("method decleration: " + line);
				String methodDec = m.group(2);
				System.out.println("adding method: " + methodDec);
				if (!declaredMethods.contains(methodDec))
					declaredMethods.add(methodDec);
				
				break;
			case 12: //method decleration exception
				System.out.println("method dec with exception: " + line );
			    methodDec = m.group(2);
				if (!declaredMethods.contains(methodDec)) {
					declaredMethods.add(methodDec);
					System.out.println("adding method: " + methodDec);
				}
				
				break;
			default: 
				System.out.println("no match found");
			}
		
		
	}
	
	private void initializePatterns() {
		patternIdentifier = new PatternIdentifier();
		patternIdentifier.add(potentialCallBack, 0);
		patternIdentifier.add(goodIfGuard, 1);
		patternIdentifier.add(switchState, 2);
		patternIdentifier.add(stateChange, 3);
		patternIdentifier.add(closedCurl, 4);
		
		patternIdentifier.add(decleration, 5);
		patternIdentifier.add(simpleMethodCall, 6);
		patternIdentifier.add(complexMethodCall, 7);
		
		patternIdentifier.add(caseState, 8);
		patternIdentifier.add(breakRegex, 9);
		patternIdentifier.add(whileLoop, 10);
		
		//NOT BEEN TESTED
		patternIdentifier.add(methodDecleration, 11);
		patternIdentifier.add(methodDeclerationException, 12);
		
		patternIdentifier.add(fsmComment, 13);
	}
	

	public StringBuilder getDiagramTextLines(IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");


		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IResource root = getRoot(editorInput);
		

		
		callBack = false;


		
		
		whileState = null;
		stateStore = null;
		currentBlock = new Stack<String>();
		visibleStates = new Stack<Node>();
		stateFound = new Stack<String>();
		unconditionalState = null;
		result = new StringBuilder();
		events = new Stack<String>();
		ignoreStack = new Stack<String>();
		storedEvents = new ArrayList<String>();
		initialState = null;
		caseName = null;
		declaredMethods = new ArrayList<String>();
		methodCalls = new ArrayList<String>();
		invisibleNodes = new ArrayList<String>();
		unConditionalState = false;
		drawTree =false;
		switchStateActive=false;
		afterLoopState = false;
		nextLineConditionalValidate = false;
		boolean oneLineConditional = false;
		//Initialize pattern store
		if(!patternsInitialized) {
			initializePatterns();
			patternsInitialized = true;

		}
		result.append("hide empty description" + "\n");
		result.append("skinparam maxmessagesize 200" + "\n");
		
		try {
			//The following document manipulation calculates the start/end of the descriptive region
			// search backward and forward start and end
			IRegion start = finder.find(selectionStart, prefixRegex, false, true, false, true);
			if (start == null) {
				// use a slightly larger selection offset, in case the cursor is within startuml
				int altSelectionStart = Math.min(selectionStart + prefix.length(), document.getLength());
				start = finder.find(altSelectionStart, prefixRegex, false, true, false, true);
				if (start == null) {
					altSelectionStart = Math.min(selectionStart + prefixRegex.length(), document.getLength());
					start = finder.find(altSelectionStart, prefixRegex, false, true, false, true);
				}
			}
			if (start != null) {
			
				final int startOffset = start.getOffset(),
						startLine = document.getLineOfOffset(startOffset + (includeStart ? 0 : start.getLength()));
	
				final IRegion end = finder.find(startOffset + start.getLength(), suffixRegex, true, true, false, true);
				if (end.getOffset() >= selectionStart) {
	
					final int endOffset = end.getOffset() + end.getLength();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());
					///////////////////////////////////////////////////////////////////////////////////////////

					
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();						
						identifyPattern(line, lineNum);
					}
					if (drawTree) {
						addBackLogNode();
	
						theTree.addNode(theTree.root, new Node("[*]", theTree.root, "", true));
						result.append(buildTransitionsFromTree(theTree, true));
					}
					


					
					
				}
					
				markerAttributes.put(IMarker.CHAR_START, start.getOffset());
				return result;
			
			}
			
			
			
		}  catch (BadLocationException e) {
			System.out.println("Bad location exception");
		}
		return null;
	}
	
	
	/**
	 * Obtain the resource given an editor
	 * @param editorInput  - the active editor
	 * @return - IResource root (used for marker creation primarily)
	 */
	protected static IResource getRoot(IEditorInput editorInput) { 
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		return wsRoot.findMember(path);

	}
	
}
