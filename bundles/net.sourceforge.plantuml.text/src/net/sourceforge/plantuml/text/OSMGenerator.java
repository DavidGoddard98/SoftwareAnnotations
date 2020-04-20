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
    Pattern elseGuard = Pattern.compile("(\\}?\\s*else\\s)\\s*\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
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
 	
 	public OSMGenerator() {
		
	}
	
	
	ArrayList<String> methodCalls;
	ArrayList<String> declaredMethods;
	ArrayList<String> exitConditions;
	ArrayList<Node> exitStates;
	ArrayList<Node> conditionalBlock;
	Stack<String> currentBlock;
	Stack<String> events;
	
	
	String whileStateName;
	Stack<Node> stateFound;
	Stack<String> ignoreStack;
	
	String initialState = null;
	StringBuilder result;
	StateTree theTree;
	
	boolean ignore = false;
	boolean stopIgnoring = false;
	boolean unConditionalState = false;
	boolean drawTree = false;
	boolean selfLoop = false;
	boolean afterLoopState = false;
	boolean nextLineConditionalValidate = false;
	boolean oneLineConditional = false;
	boolean certainEvent = true;
	
	
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
				
			} else if (m.find() && info.identifier == 14) { //fsm comment
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
	
	//Couldnt think of a better way of getting if-else statesments to go around the else unconditional block...
	public void duplicateTransitions(Node from, Node to, StringBuilder transition) { 
		System.out.println("heeeere");
		for (Node node : theTree.noLink.get(from)) {
			if (!node.equals(from)) {
				result.append(node.stateName + " -> " +  to.stateName + " : "  + transition + "\n");
			}
		}
	}
	
	

	private StringBuilder buildTransitionsFromTree(StateTree theTree, boolean lastTree) {
		StringBuilder result = new StringBuilder();

		ArrayList<Node> visibleStates = new ArrayList<Node>();
		for (Node aNode : theTree.nodes) {
			if (aNode.stateName.equals("[*]")) continue;
			if (aNode.visible) visibleStates.add(aNode);
		}
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes = theTree.nodes;
		for (int j = 0; j<visibleStates.size(); j++) {
			
			Node from = visibleStates.get(j);
			if (exitStates.contains(from)) continue;
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
				
				if (theTree.noLink.containsKey(from)) duplicateTransitions(from, to, transition);
				
				if (from.stateName.equals("INIT"))	result.append("[*]" + " -down-> " + to.stateName + "\n"); 
				else result.append(from.stateName + " -down-> " + to.stateName + " : " + transition + "\n"); 
				
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

	
	
	
	private void normalFlow(int patternNo, String line, int lineNum, Matcher m) {
		switch(patternNo) {

			case 0: //call back...
				
				System.out.println("found a callback: " + line);
				if (line.contains("Systen.out.print")) break;
				if (exitConditions.contains(m.group(1))) {
					exitStates.add(stateFound.peek());
				}
				if (!stateFound.empty()) stateFound.peek().setVisible();
				
				
				break;
			case 1: //complex if guard
				 System.out.println("good if guard " + line + "statement: " + m.group(2)) ;
				 if (m.group(1).equals("if")) {
					 conditionalBlock = new ArrayList<Node>();
					 
				 } else if (currentBlock.peek().equals("state-conditional")) {
					 conditionalBlock.add(stateFound.peek());
				 }
				
				
				
				 certainEvent = true;
				 if (!m.group(3).equals("{")) { //check to see if the bracket is on the next line...
					 nextLineConditionalValidate = true;
				 } 
				 if (m.group(1).contains("}") && !currentBlock.empty()) {
					 switch(currentBlock.peek()) {
						case "state-conditional":
							appendMethodCalls();
							certainEvent = false;
							stateFound.pop();
							break;
						case "conditional":
							methodCalls.clear();
							break;
					 }
					 currentBlock.pop();
				 }
				 appendMethodCalls();
				 
				 String expression = m.group(2);
				 if (!expression.replaceAll("(state)\\s+\\=\\=\\s+(valid_states\\.)", "").equals(expression)) {
					 currentBlock.push("conditional-state");
					 events.push(m.group(2)); //the condition
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
		//			        	 anotherExpression = anotherExpression.substring(anotherExpression.indexOf(aState));
		//			         } else inEqualityStatesFound = true;
		//			   	 }
						
				
				 
				 
			
			 
				
				break;
			case 2: //else guard
				System.out.println("HERE IN ELSE CASE 2");
				appendMethodCalls();
				
				if (currentBlock.peek().equals("state-conditional"))
					conditionalBlock.add(stateFound.peek());
				if (m.group(1).contains("}") && !currentBlock.empty()) {
					
					switch(currentBlock.peek()) {
					
						case "state-conditional":
							appendMethodCalls();
							certainEvent = false;
							stateFound.pop();
							break;
						case "conditional":
							methodCalls.clear();
							break;
					 }
					 currentBlock.pop();
				}
				currentBlock.push("else-conditional");
				
				break;
			case 3: //switch state
		
				currentBlock.push("switch-state");
				return;
			case 4: //state change
				System.out.println("state Change: " + line + "the State: " + m.group(4));
				drawTree = true; //Informs that a new tree must be drawn back in diagramTextLines
				

				if (initialState == null) {
					certainEvent = true;
					initialState = m.group(4);
					Node node = new Node(initialState, null, "", true);
					theTree = new StateTree(node);
					stateFound.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
					appendMethodCalls();

					break;
					
				} else if (afterLoopState) {
					certainEvent = true;
					result.append(whileStateName + " -> " + m.group(4) + " : Exit loop" + "\n");
					Node node = new Node(m.group(4), null, "", true);
					theTree = new StateTree(node);
					stateFound.push(node);
					afterLoopState = false;
					appendMethodCalls();

					break;
				}
				
				if (unConditionalState) {
					//IF THIS IS VISIBLE
					if (stateFound.peek().visible) {
						String lastStateName = stateFound.peek().stateName;
						buildStateTree(false);
						Node newRoot = new Node(lastStateName, null, "", true);
						theTree = new StateTree(newRoot);

						stateFound.push(newRoot);
						drawTree = true;
					} else {
						//remove last node and continue like normal;
						theTree.removeLastNode();
						stateFound.pop();
						
					}
					unConditionalState = false;	
				} 
				
				
				if (currentBlock.empty()) {
					
					certainEvent = true;
					Node node = new Node(m.group(4), theTree.root, "unconditional", false);   
					theTree.addNode(theTree.root, node);
					unConditionalState = true;
					stateFound.push(node);
					
				} else if (!stateFound.empty() && !currentBlock.empty()) {
					
					
					if (currentBlock.peek().equals("conditional")) { 
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("state-conditional"); //therefore speicify this
						
						Node node = new Node(m.group(4), stateFound.peek(), events.peek(), false);   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
						
					} else if (currentBlock.peek().equals("else-conditional")) {
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("else-state-conditional"); //therefore speicify this
						Node node = new Node(m.group(4), stateFound.peek(), "unconditional", false);   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
					} else if (currentBlock.peek().equals("state-conditional")) {
						
						Node node = new Node(m.group(4), stateFound.peek(), "unconditional", false);   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
					}  else if (currentBlock.peek().equals("case-state") || currentBlock.peek().equals("while-loop") ) {
						appendMethodCalls();
						Node node = new Node(m.group(4), stateFound.peek(), "", false);   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						selfLoop = false;
						
					} 
				} else if (!currentBlock.empty() && currentBlock.peek().equals("while-loop")) {
					result.append("[*] -> " + m.group(4) + "\n");
					Node node = new Node(m.group(4), null, "", true); 
					theTree = new StateTree(node);
					stateFound.push(node);
				}
				appendMethodCalls();

		
				break;
			case 5: //closed }
				if (!currentBlock.isEmpty()) {
					switch(currentBlock.peek()) {
						case "state-conditional":
							appendMethodCalls();
							certainEvent = false;
							stateFound.pop();
							break;
						case "conditional":
							methodCalls.clear();
							break;
						case "else-state-conditional":
							if (stateFound.peek().visible) {
								appendMethodCalls();
								conditionalBlock.add(stateFound.peek());
								certainEvent = false;
								if (conditionalBlock.size() > 1) {
									for (Node conditionalBlockNode : conditionalBlock) {
										System.out.println("ADDING NODE TO CONDITONALBLOCKNODE: " + conditionalBlockNode);
										theTree.noLink.put(conditionalBlockNode, conditionalBlock);
									}
								}
								stateFound.pop();
							}
							break;
						case "while-loop":
							afterLoopState = true;
							buildStateTree(false);
							result.append("}" + "\n");
							break;
					}
					currentBlock.pop();
				}
				if (!events.isEmpty()) {
					events.pop();
				}
			

				
				break;
			case 6: //decleration
				System.out.println("decleration: " + line);
				
				break;
			case 7: //simpleMethodCal
				
				int index = m.group(1).indexOf("(");
				String method = m.group(1).substring(0, index);
				if (declaredMethods.contains(method) && !methodCalls.contains(m.group(1))){
					
					methodCalls.add(m.group(1));
					System.out.println("addding :" + m.group(1));
				}
				if (exitConditions!= null && exitConditions.contains(m.group(1)) && !stateFound.empty()) {
					selfLoop = false;
					String lastStateName = stateFound.peek().stateName;
					result = result.append(lastStateName + " -down-> [*] : " + m.group(1) + "\n");
					
				}
				
			
				
				System.out.println("simple method call: " + line + "method: " + m.group(1));
				break;
			case 8: //complexMethodcall
				System.out.println("complex method call: " + line + "method: " + m.group(3));
				
				break;
			case 9: //case 
				if (currentBlock.peek().equals("case-state")) currentBlock.pop(); //no break inbetween
				
				currentBlock.push("case-state"); //we know that the case is a state...
				stateFound.clear();
				certainEvent = true;
				if (m.group(2).equals("INIT")) {	
					if( currentBlock.contains("while-loop")) selfLoop = false;
				}
				else {
					result.append("state " + m.group(2) + "\n");
					if( currentBlock.contains("while-loop")) selfLoop = true;
				}
				Node node = new Node(m.group(2), null, "", true);
				theTree = new StateTree(node);
				stateFound.push(node);

					
				System.out.println("caseState: " + line + "the stateName: " + m.group(2));
				
				
				break;
			case 10:  //break regex
				if (currentBlock.peek() == "conditional") {
					//probably dont need the .contains("case-state") above...
					ignore = true;
					ignoreStack.push("ignore");
					break;
					
				
				} else if (currentBlock.peek() == "case-state") {
					String caseName = stateFound.firstElement().stateName;

					if (selfLoop) {
						StringBuilder transition = new StringBuilder();

						node = stateFound.firstElement();
						
						for (Node child : theTree.getChildren(node)) {
							transition.append(negateCondition(child.event));
						}

						if (transition.length() == 0) transition.append("No event found");
						result.append(caseName + " -> " + caseName + " : " + transition + "\n");
						appendMethodCalls();

						
						
					}
					methodCalls.clear();
					if (theTree.root.stateName.equals("[*]")) System.out.println(theTree.nodes);
					buildStateTree(false);
					while(currentBlock.peek() != "case-state") currentBlock.pop(); 
					
					currentBlock.pop();
				}
				
				break;
				
				
				
				
				
				
			case 11: //whileLoop
				currentBlock.push("while-loop");
				selfLoop = true;
				
				
				Node whileState = new Node(m.group(1), theTree.root, "unconditional", true);	
				whileStateName = m.group(1);
				
				addNodeBuildTree(whileState, theTree.root, false);
				//stateFound.push(whileState);

				
				result.append("state " + m.group(1) + " { " + "\n"); 
				result.append("state \" WHILE LOOP:  " + m.group(1) + "\" as " + m.group(1) + "\n");
			
			case 12: //methodDecleration
				
				String methodDec = m.group(2);
				if (!declaredMethods.contains(methodDec))
					declaredMethods.add(methodDec);
				
				break;
			
			case 13: //method decleration exception

				methodDec = m.group(2);
				if (!declaredMethods.contains(methodDec)) {
					declaredMethods.add(methodDec);
				}
				
				break;
			default: 
				System.out.println("no match found");
			}
		
		
	}
	
	private void appendMethodCalls() {
		if (certainEvent && !stateFound.empty() && stateFound.peek().visible) {
			for (String methodCall : methodCalls) {
				result.append("state " + stateFound.peek().stateName + " : " + methodCall + ";");
				result.append("\n");
			}
		}
		methodCalls.clear();
	}
	
	private void buildStateTree(boolean lastTree) {
		
		result.append(buildTransitionsFromTree(theTree, lastTree));
		drawTree = false;
		stateFound.clear();
	}
	
	private void addNodeBuildTree(Node nodeToAdd, Node nodeToAddNodeTo, boolean lastTree) {
		theTree.addNode(nodeToAddNodeTo, nodeToAdd);
		buildStateTree(lastTree);

	}
	private void initializePatterns() {
		patternIdentifier = new PatternIdentifier();
		patternIdentifier.add(potentialCallBack, 0);
		patternIdentifier.add(goodIfGuard, 1);
		patternIdentifier.add(elseGuard, 2);
		patternIdentifier.add(switchState, 3);
		patternIdentifier.add(stateChange, 4);
		patternIdentifier.add(closedCurl, 5);
		
		patternIdentifier.add(decleration, 6);
		patternIdentifier.add(simpleMethodCall, 7);
		patternIdentifier.add(complexMethodCall, 8);
		
		patternIdentifier.add(caseState, 9);
		patternIdentifier.add(breakRegex, 10);
		patternIdentifier.add(whileLoop, 11);
		
		//NOT BEEN TESTED
		patternIdentifier.add(methodDecleration, 12);
		patternIdentifier.add(methodDeclerationException, 13);
		
		patternIdentifier.add(fsmComment, 14);
	}
	

	public StringBuilder getDiagramTextLines(IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");


		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IResource root = getRoot(editorInput);
		

		
		conditionalBlock = new ArrayList<Node>();
		exitStates = new ArrayList<Node>();
		currentBlock = new Stack<String>();
		stateFound = new Stack<Node>();
		result = new StringBuilder();
		events = new Stack<String>();
		ignoreStack = new Stack<String>();
		initialState = null;
		declaredMethods = new ArrayList<String>();
		methodCalls = new ArrayList<String>();
		unConditionalState = false;
		drawTree =false;
		afterLoopState = false;
		nextLineConditionalValidate = false;
		oneLineConditional = false;
		selfLoop = false;
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
						for (Node node : theTree.nodes) { 
							System.out.println(node);
						}
						addNodeBuildTree( new Node("[*]", theTree.root, "", true), theTree.root, true);
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
