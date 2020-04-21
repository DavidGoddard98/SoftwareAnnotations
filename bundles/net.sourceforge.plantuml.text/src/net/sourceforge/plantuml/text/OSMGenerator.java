package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import net.sourceforge.plantuml.text.StateDiagram;

public class OSMGenerator {

	
	final String prefix = "@start_OSM_generation", prefixRegex = prefix;
	final String suffix = "@end_OSM_generation", suffixRegex = suffix;
	final protected static String[] allTransitionHighlights = {"FSM.Transition.Highlight_1", "FSM.Transition.Highlight_2", "FSM.Transition.Highlight_3", "FSM.Transition.Highlight_4", "FSM.Transition.Highlight_5", "FSM.Transition.Highlight_6"};
	final protected static String[] transitionColors = {"#Lime", "#Gold", "#FireBrick", "#HotPink", "#DarkOrchid", "#DarkGreen"};
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
 	
 	//LINKING DIAGRAM TO TEXT VARIABLES//////
 	
 	StateDiagram stateDiagram;
 	int selectedLineNum;
	String stateSelected;
	private HashSet<String> plantMarkerKey = new HashSet<String>();
	protected HashMap<String, LinkedHashSet<String>> textualDiagram; //a map of strings that will eventually make up the string sent to plantuml
	
	
	
	ArrayList<String> methodCalls;
	ArrayList<String> declaredMethods;
	ArrayList<String> exitConditions;
	ArrayList<Node> exitStates;
	ArrayList<Node> conditionalBlock;
	ArrayList<String> ignoreArray;
	Stack<String> currentBlock;
	Stack<Event> events;
	
	
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
	
	
	private void identifyPattern(String line, int lineNum, int startOfRegion) throws BadLocationException, CoreException {
		
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

		//IRegion markerRegion = stateDiagram.finder.find(startOfRegion, line, true, true, false, false);

		for (RegexInfo info : patternIdentifier.patternStore) {
			Matcher m = info.pattern.matcher(line);
			if (m.matches()) {
				
				normalFlow(info.identifier, line, lineNum, m);
				
			} else if (m.find() && info.identifier == 14) { //fsm comment
				System.out.println("fsm comment " + line);
				String removeFSM = line.substring(6);
				if (removeFSM.contains("EXIT")) {
					removeFSM = removeFSM.replaceAll("\\s*(EXIT)\\s*-\\s*", "");
					exitConditions = new ArrayList<String>(Arrays.asList(removeFSM.split("/")));
				} else if (removeFSM.contains("REMOVE")) {
					removeFSM = removeFSM.replaceAll("\\s*(REMOVE)\\s*-\\s*", "");
					ignoreArray.add(removeFSM.trim());
				} else {
					result.append(line.substring(6));
					result.append("\n");
				}
				
			}
		}
				
	}
	
	//Couldnt think of a better way of getting if-else statesments to go around the else unconditional block...
	public void duplicateTransitions(Node from, Node to, StringBuilder transition) { 
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
					if (node.event.event.isEmpty()) continue;
					if (node.event.event.equals("unconditional")) continue;
					transition.append(node.event.event + " && ");
				}
				
				for (Node node : transitionInformation.metStates) {
					if (node.event.event.isEmpty()) continue;
					if (node.event.event.equals("unconditional")) continue;

					transition.append(negateCondition(node.event.event) + " && ");
				}
				
				if (transition.length() > 3)
				transition.delete(transition.length()-3, transition.length());
				if (transition.length() == 0) transition.append("No event found");
				
				String leftState = from.stateName;
				String rightState = to.stateName;
				
			
				if (leftState.equals("INIT"))  leftState = "[*]";
				

				
				Transition aTransition = new Transition(leftState, rightState, from.charStart, from.charEnd, 
						to.charStart, to.charEnd, to.event.multiLineStart, to.event.multiLineEnd);
				
				String line = leftState + " -down-> " + rightState + " : " + transition;
				String aLineNoDown = leftState + " -> " + rightState + " : " + transition;
				

				StateReference theTransition = new StateReference(line, to.event.event, to.event.lineNum, aTransition);
				appendToLists(theTransition, aLineNoDown);
				StateReference theLeftState = new StateReference(leftState, from.editorLine, from.lineNum, from.charStart, from.charEnd, false);
				appendToLists(theLeftState, leftState);
				StateReference theRightState = new StateReference(rightState, to.editorLine, to.lineNum, to.charStart, to.charEnd, false);
				appendToLists(theRightState, rightState);
				
				
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				//if (theTree.noLink.containsKey(from)) duplicateTransitions(from, to, transition);
				
				
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
					if (node.event.event.isEmpty()) continue;
					transition.append(node.event + " && ");
				}
				
				for (Node node : transitionInformation.metStates) {
					if (node.event.event.isEmpty()) continue;
					transition.append(negateCondition(node.event.event) + " && ");
				}
				if (transition.length() > 3)
				transition.delete(transition.length()-3, transition.length());
				if (transition.length() == 0) transition.append("No event found");

				result.append(invisibleNode.stateName + " -down-> [*] : " + transition + "\n"); 
				
			}
		}
		return result;
	}
	
	
	private void appendToLists(StateReference stateReference, String lineToIgnore) {
		String stateName = stateReference.stateName;
		
		if (stateReference.lineNum == selectedLineNum) stateSelected = stateName;
		
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();
		
		if (lineToIgnore != "" && ignoreArray.contains(lineToIgnore.trim())) return;
		if ( ignoreArray.contains(stateName)) return;
		if (stateReference.isTransition && ignoreArray.contains(stateReference.transition.leftState) || stateReference.isTransition && ignoreArray.contains(stateReference.transition.rightState)) return;
		if (stateDiagram.stateLinkers.containsKey(stateName)) {

			stateReferences = stateDiagram.stateLinkers.get(stateName);
			if (!stateReferences.contains(stateReference)) stateReferences.add(stateReference);
			stateDiagram.stateLinkers.put(stateName, stateReferences);
		} else {
			if (!stateReferences.contains(stateReference)) stateReferences.add(stateReference);
			stateDiagram.stateLinkers.put(stateName, stateReferences);
		}
	}
	

	
	
	private void normalFlow(int patternNo, String line, int lineNum, Matcher m) throws CoreException, BadLocationException {
		
		switch(patternNo) {
			
			case 0: //call back...
				
				if (line.contains("Systen.out.print")) break;
				if (exitConditions.contains(m.group(1))) {
					exitStates.add(stateFound.peek());
				}
				if (!stateFound.empty()) stateFound.peek().setVisible();
				
				
				break;
			case 1: //complex if guard
				int charStart = stateDiagram.document.getLineOffset(lineNum);
				int charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				 
				
				 Event event = new Event(m.group(2), line, charStart, charEnd, lineNum);
				 
				 
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
					 //events.push(stateReference); //the condition
					 boolean equalityStatesFound = false;
					 String aString = expression.replaceAll("(state)\\s+\\=\\=\\s+(valid_states\\.)", "equality");
					 //STOP SELF LOOPING
				   	 while (!equalityStatesFound) {
				   		 
				         if (aString.indexOf("equality") != -1) {
				        	 
		
				             int index = aString.indexOf("equality");
				        	 int anotherIndex = aString.indexOf(" ", index);
				        	 if (anotherIndex == -1) anotherIndex = aString.length();
				        	 String aState = aString.substring(index + 8, anotherIndex);
				        	 if (aState.equals("")) break;
				        	 aString = aString.substring(aString.indexOf(aState));
				         } else equalityStatesFound = true;
				   	 }
					
				 }else {
					 currentBlock.push("conditional");
					 events.push(event);
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
				appendMethodCalls();
				
//				multiLineStart = markerRegion.getOffset();
//				multiLineEnd = markerRegion.getOffset() + line.length(); //for now, will change when we find }
//				transition = new Transition(multiLineStart, multiLineEnd, m.group(2));
//				stateReference = new StateReference(line, line, lineNum, transition);
				
				
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
				
				if (lineNum == selectedLineNum) stateSelected = m.group(4);
				
				drawTree = true; //Informs that a new tree must be drawn back in diagramTextLines
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
			
				if (initialState == null) {
					certainEvent = true;
					initialState = m.group(4);
					Node node = new Node(initialState, line, null, true, charStart, charEnd, lineNum, new Event(""));
					theTree = new StateTree(node);
					stateFound.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
					appendMethodCalls();

					break;
					
				} else if (afterLoopState) {
					certainEvent = true;
					result.append(whileStateName + " -> " + m.group(4) + " : Exit loop" + "\n");
					Node node = new Node(m.group(4), line, null, true, charStart, charEnd, lineNum, new Event(""));
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
						appendStateAndTransitions();
						result.append(stateDiagramAsString());
						Node newRoot = new Node(lastStateName, line, null, true, charStart, charEnd, lineNum, new Event(""));
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
					Node node = new Node(m.group(4), line, theTree.root, false, charStart, charEnd, lineNum, new Event("unconditional"));   
					theTree.addNode(theTree.root, node);
					unConditionalState = true;
					stateFound.push(node);
					
				} else if (!stateFound.empty() && !currentBlock.empty()) {
					
					
					if (currentBlock.peek().equals("conditional")) { 
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("state-conditional"); //therefore speicify this
						Node node = new Node(m.group(4), line, stateFound.peek(), false, charStart, charEnd, lineNum, events.peek());   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
						
					} else if (currentBlock.peek().equals("else-conditional")) {
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("else-state-conditional"); //therefore speicify this
						Node node = new Node(m.group(4), line, stateFound.peek(), false, charStart, charEnd, lineNum, new Event("unconditional"));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
					} else if (currentBlock.peek().equals("state-conditional")) {
						
						Node node = new Node(m.group(4), line, stateFound.peek(), false, charStart, charEnd, lineNum, new Event("unconditional"));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
					}  else if (currentBlock.peek().equals("case-state") || currentBlock.peek().equals("while-loop") ) {
						appendMethodCalls();
						Node node = new Node(m.group(4), line, stateFound.peek(), false, charStart, charEnd, lineNum, new Event(""));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						selfLoop = false;
						
					} 
				} else if (!currentBlock.empty() && currentBlock.peek().equals("while-loop")) {
					result.append("[*] -> " + m.group(4) + "\n");
					Node node = new Node(m.group(4), line, null, true, charStart, charEnd, lineNum , new Event("")); 
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
//							events.peek().setLineEnd(charStart);
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
							appendStateAndTransitions();
					
							result.append(stateDiagramAsString());
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
				
				break;
			case 7: //simpleMethodCal
				
				int index = m.group(1).indexOf("(");
				String method = m.group(1).substring(0, index);
				if (declaredMethods.contains(method) && !methodCalls.contains(m.group(1))){
					
					methodCalls.add(m.group(1));
				}
				if (exitConditions!= null && exitConditions.contains(m.group(1)) && !stateFound.empty()) {
					selfLoop = false;
					String stateName = stateFound.peek().stateName;
					Node currentNode = stateFound.peek();
					result = result.append(stateName + " -down-> [*] : " + m.group(1) + "\n");
					StateReference theState = new StateReference(stateName, currentNode.editorLine, currentNode.lineNum, currentNode.charStart, currentNode.charEnd, false);
					appendMethodCalls();
					appendToLists(theState, "");
				}
				
			
				
				break;
			case 8: //complexMethodcall
				
				break;
			case 9: //case 
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				if (currentBlock.peek().equals("case-state")) currentBlock.pop(); //no break inbetween
				if (ignoreArray.contains(m.group(2))) break;

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
				Node node = new Node(m.group(2), line, null, true, charStart, charEnd, lineNum, new Event(""));
				theTree = new StateTree(node);
				stateFound.push(node);
			
				
				
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
							transition.append(negateCondition(child.event.event));
						}

						if (transition.length() == 0) transition.append("No event found");
						
						String stateName = node.stateName;
						if(stateName.equals("INIT")) stateName = "[*]";
						StateReference theState = new StateReference(stateName, node.editorLine, node.lineNum, node.charStart, node.charEnd, false);
						
						if (!ignoreArray.contains(caseName + " -> " + caseName + " : " + transition)) {
							result.append(caseName + " -> " + caseName + " : " + transition + "\n");

						} 
						appendToLists(theState, "");			
						appendMethodCalls();

						
						
					}
					
					methodCalls.clear();
					buildStateTree(false);
					
					while(currentBlock.peek() != "case-state") currentBlock.pop(); 
					
					currentBlock.pop();
				}
				
				break;
				
				
				
				
				
				
			case 11: //whileLoop
				currentBlock.push("while-loop");
				selfLoop = true;
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				
				Node whileState = new Node(m.group(1), line, theTree.root, true, charStart, charEnd, lineNum, new Event("unconditional"));	
				whileStateName = m.group(1);
				
				addNodeBuildTree(whileState, theTree.root, false);
				appendStateAndTransitions();

				
				result.append(stateDiagramAsString());
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
	
	private void buildStateTree(boolean lastTree)  {
		
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
		IResource root = StateTextDiagramHelper.getRoot(editorInput);
		

		
		conditionalBlock = new ArrayList<Node>();
		exitStates = new ArrayList<Node>();
		currentBlock = new Stack<String>();
		stateFound = new Stack<Node>();
		result = new StringBuilder();
		events = new Stack<Event>();
		ignoreStack = new Stack<String>();
		initialState = null;
		declaredMethods = new ArrayList<String>();
		ignoreArray = new ArrayList<String>();
		methodCalls = new ArrayList<String>();
		textualDiagram = new HashMap<String, LinkedHashSet<String>> (); 
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
					
					
					
					stateDiagram = new StateDiagram(finder, document, root, path);
					
					selectedLineNum = document.getLineOfOffset(selectionStart);
					stateSelected = "";
					
					IMarker[] allMarkers;
					
					allMarkers = stateDiagram.root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
					System.out.println("NUMBER OF MARKERS : " + allMarkers.length);
					System.out.println("NUMBER OF KEYS : " + plantMarkerKey.size());
					for (String key : plantMarkerKey) {
						System.out.println(key);
					}
					System.out.println("SELECTED LINE NUM : " + selectedLineNum);
					
					
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();						
						identifyPattern(line, lineNum, startOffset);
					}
					
					if (drawTree) {
						for (Node node : theTree.nodes) { 
							System.out.println(node);
						}
						addNodeBuildTree( new Node("[*]", "", theTree.root, true, 0, 0, -1, new Event("")), theTree.root, true);
						appendStateAndTransitions();

						
						result.append(stateDiagramAsString());
					}
					
					//isStateSelected(); //if selectedLineNum == lineNum of a state then highlight all references to state

					
				}
					
				markerAttributes.put(IMarker.CHAR_START, start.getOffset());
				return result;
			
			}
			
			
			
		}  catch (BadLocationException e) {
			System.out.println("Bad location exception");
		} catch (CoreException e) {
			System.out.println("Core exception exception");
		}
		return null;
	}
	
	
	
	
	
	
	

	
	
	
	/**
	 * Creates links from the diagram back to the text for 'normal' states.
	 * AND visualizations from text to diagram for both 'normal states' & transitionStates. In other words...
	 * If 'StateA' is selected, then it will append  'state StateA #cyan' (colors the node cyan in the diagram)
	 * For transitions.. If the line StateA -> StateB is selected, it will append 'StateA -[thickness=5,#Lime]> StateB'
	 * @throws CoreException
	 */
	protected void appendStateAndTransitions() throws CoreException {
		textualDiagram = new HashMap<String, LinkedHashSet<String>> (); 

		for (Map.Entry<String, ArrayList<StateReference>> entry : stateDiagram.stateLinkers.entrySet()) {


			String stateName = entry.getKey();
			ArrayList<StateReference> stateReferences = entry.getValue();
			for (StateReference stateReference : stateReferences) {
				String line = stateReference.theLine;
				int lineNum = stateReference.lineNum;
				
				
				createKey(stateReference);
				
				if (!stateReference.isTransition) {
					//STATE
					
					if (stateName.equals(stateSelected)) { //color that node cyan
						if (!stateName.equals("[*]")) {
							String colorState = forwardStateLink(stateName);
							appendTextualDiagram(stateName, backwardStateLink(stateName, lineNum) + "\n");
							appendTextualDiagram(stateName, colorState + "\n"); 
						}
						System.out.println();
						System.out.println(stateReference.editorLine);
						System.out.println(stateReference.isPlantUML);
							if (!stateReference.isPlantUML )
								System.out.println("state references .size : " + stateReferences.size());
								displayHighlights(stateName, stateReference, null);
						
						
					} else { //create the link from the diagram to the editor
						if (!stateName.equals("[*]"))
						appendTextualDiagram(stateName, backwardStateLink(stateName, lineNum) + "\n");
					}
				}
				
				else  {
					//TRANSITION							
				
					//transitionDone ?
					if (stateDiagram.addedTransitions.contains(line)) continue;

					if (selectedLineNum == lineNum && lineNum > 0) { //color that transition lime and create a link back to line in editor
						
						String colorTransition = forwardTransitionLink(line);
					
						appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, lineNum, line) + "\n");
						displayTransitionStateTransitionMarkers(stateReference);

						
					} else { //dont color transition but create the link back
						if (lineNum == 0) {
							appendTextualDiagram(stateName, line + "\n");

						} else {
							appendTextualDiagram(stateName, backwardTransitionLink(line, lineNum, line) + "\n");
						}					
					}
					stateDiagram.addedTransitions.add(line);
				}
				

			}	
			
		}
	}
	
	//Link from the editor to the diagram for states
	protected String forwardStateLink(String stateName) {
		return "state " + stateName + " #Cyan";
	}

	//Link from the editor to the diagram for transitions
	protected String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfArrow = selectedLine.indexOf("-");
		String theArrow = selectedLine.substring(indexOfArrow+1, indexOfArrow +2);
		String transColor = "#Lime";
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=3,"+transColor+"]" + theArrow + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}

	//create link from diagram to the editor for a state
	protected String backwardStateLink(String stateName, int lineNum) {
		String link = "state " + stateName + "[["+stateDiagram.className+"#FSM#state#"+ lineNum +"]]";
		return link;

	}
	
	//create a link from diagram to the editor for a transitionState
	protected String backwardsTransitionStateLink(StateReference stateReference, String stateName) throws CoreException {
		createKey(stateReference);
		String link = "state " + stateName + "[["+stateDiagram.className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]";
		return link;
	}

	//create a link from diagram to the editor for a transition
	protected String backwardTransitionLink(String aLine, int lineNum, String originalLine) {
		if (aLine.contains(":")) {
			int index = aLine.indexOf(":");
			String transitionLabel = aLine.substring(index + 1, aLine.length());
			return aLine.substring(0, index -1) + " : " + "[["+stateDiagram.className+"#FSM#transition#"+lineNum+ transitionLabel +"]]";
		}
		
		return aLine + " : " + "[["+stateDiagram.className+"#FSM#transition#"+lineNum+" "+originalLine+"]]";

	}
	
	
	/**
	 * Similar to the hashmap used to store the statereferences, this does the same but instead of statereferences it stores the line as a string.
	 * Essentially it stores all of the lines used to describe the diagram and once they have all been collected it iterates through them appending
	 * them to a StringBuilder to be sent to the plant uml library. This is done so that the diagram remains consistent (the positioning of its components).
	 * I was getting various errors as the order of the lines matters. 
	 * @param stateName
	 * @param line
	 */
	protected void appendTextualDiagram(String stateName, String line) {
		//all textual descs for this state
		LinkedHashSet<String> stateTextualDesc = new LinkedHashSet<String>();

		if (textualDiagram.containsKey(stateName)) {
			stateTextualDesc = textualDiagram.get(stateName);
			if (stateTextualDesc.contains(line)) return;
			stateTextualDesc.add(line);
			textualDiagram.put(stateName, stateTextualDesc);
			
		} else {
			stateTextualDesc.add(line);
			textualDiagram.put(stateName, stateTextualDesc);
		}
	}
	
	//Loops through the textualDiagram map and constructs the diagramtext as a StringBuilder. This is what is returned from getDiagramTextLines()
	protected StringBuilder stateDiagramAsString() {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, LinkedHashSet<String>> entry : textualDiagram.entrySet()) {
			LinkedHashSet<String> textualDesc = entry.getValue();
			String stateName = entry.getKey();
			String tmpSave = null;
			for (String text : textualDesc) {
				
				if (text.contains("state " + stateName + " #")) {
					tmpSave = text;
					continue;
				}
				result.append(text);
			}
			if (tmpSave != null) result.append(tmpSave);
		}
		return result;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//-----------------------------------------------MARKER AND KEY CREATION-----------------------------------------------------------------------
	
	//Deals with the creation, maintenance and deletion of FSM.MARKER's. 
	//The markers are created for every state, transitionState and transition and essentially track their positions in the document. 
	//They are an essential part of linking back from the diagram to the text and are used by the class StateLinkOpener.java in 'net.sourceforge.plantuml.eclipse' package.
	//The keys ensure duplicate markers are not created and allow for the markers to be updated if aspects of their positioning i.e lineNum change.
	
	
	//A key is created for every diagram text line and if it doesnt exist then a marker of that line and the relevant positions is created.
	protected void createKey(StateReference stateReference) throws CoreException {
		
		String theLine = stateReference.editorLine;
		int lineNum = stateReference.lineNum;
		
		int charStart;
		int charEnd;
		if (stateReference.isTransition) {
			theLine = stateReference.theLine;
			charStart = stateReference.transition.multiLineStart;
			charEnd = stateReference.transition.multiLineEnd;
		} else {
			charStart = stateReference.charStart;
			charEnd = stateReference.charEnd;	
		}
		if (lineNum == 0 && charStart == 0 && charEnd == 0) return;

		String key = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) + String.valueOf(charEnd);
		
		if (plantMarkerKey.contains(key)) {
			System.out.println("Key exists");
			return;
		}
		if (possibleChangedMarker(theLine, lineNum, charStart, charEnd)) { //returns true if the diagram text hasnt changed but its positions had. It updates the key + marker
			System.out.println("marker changed");
			return;
		}
		//else brand new line so create new marker and key
		System.out.println("key doesnt exist, adding key");
		plantMarkerKey.add(key);
		
		addMarkerTask(theLine, lineNum, charStart, charEnd);
		
	}
	
	/**
	 * Checks the possibility that the marker positions could have changed for a particular diagramText.
	 * If so it deletes the old marker and key and creates new ones with the updated positions. 
	 * @param theLine - the line in the editor
	 * @param lineNum - ...
	 * @param charStart - start offset of the transition/state
	 * @param charEnd - end offset of the transition/state
	 * @return
	 */
	protected boolean possibleChangedMarker(String theLine, int lineNum, int charStart, int charEnd) {
		try {
			IMarker[] allMarkers;
		
			allMarkers = stateDiagram.root.findMarkers("FSM.MARKER", true, IResource.DEPTH_INFINITE);
			
			String path = stateDiagram.path.toString();
			for (IMarker aMarker : allMarkers) {
		
				String markerMessage = (String)aMarker.getAttribute(IMarker.MESSAGE);
				
				if (theLine.contains("transitionState") && !theLine.equals(markerMessage)) continue;
				
				int markerLine = (int)aMarker.getAttribute(IMarker.LINE_NUMBER);
				
				String markerPath = (String)aMarker.getAttribute(IMarker.SOURCE_ID);
				String[] tmp = markerPath.split("/");
				String markerClassName = tmp[tmp.length-1];

				int markerCharStart = (int)aMarker.getAttribute(IMarker.CHAR_START);
				int markerCharEnd = (int)aMarker.getAttribute(IMarker.CHAR_END);
				
				IDocument document = stateDiagram.document;
				String sameLineInDoc = document.get(document.getLineOffset(markerLine-1), document.getLineLength(markerLine-1)).trim();
				
				//Crossreferences old marker on that line with the diagramText on that line to see if different
				if (markerLine == (lineNum + 1) && markerPath.equals(path)) {
					if (!markerMessage.equals(theLine) || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);
						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, charStart, charEnd);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
				//Finds the marker with the same message as the diagram text and updates it if linNum or offsets have changed
				if (markerPath.equals(path) && markerMessage.equals(theLine)) {
					if (markerLine != lineNum + 1 || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = stateDiagram.className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);

						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, charStart, charEnd);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
//				//line deleted - delete marker and key
//				if (!sameLineInDoc.equals(markerMessage)) {
//					String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
//					
//					//delete key
//					plantMarkerKey.remove(oldKey);
//					//and marker 
//					aMarker.delete();
//				}
				
			}
				
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
		} catch (BadLocationException e) {
			System.out.println("Cant find same line in doc");
		}
		return false;

	}
	
	

	//creates a Marker of type FSM.MARKER
	protected void addMarkerTask(String theLine, int lineNum, int charStart, int charEnd) throws CoreException {
		
		IMarker marker = stateDiagram.root.createMarker("FSM.MARKER");
		marker.setAttribute(IMarker.MESSAGE, theLine);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
		marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
		marker.setAttribute(IMarker.CHAR_START, charStart);
		marker.setAttribute(IMarker.CHAR_END, charEnd);
	}
	
	
	/**
	
	
	//-----------------------------------------------HIGHLIGHTING PARTS OF DOCUMENT-----------------------------------------------------------
	

	
	//NOTE: The name given to states which don't have a direct reference - i.e. are only referenced within transitions, is transitionState.
	/**
	 * Yes the name is correct. For a given transtionState, this highlights all other transitions which reference that transitionState
	 * @param stateReference
	 */
	protected void displayTransitionStateTransitionMarkers(StateReference stateReference) throws CoreException {
		int lineNum = stateReference.lineNum;
		Transition transition = stateReference.transition;
		int charStart = transition.rightCharStart;
		int charEnd = transition.rightCharEnd;
		addHighlightState(lineNum, charStart, charEnd);
		
		
		if (transition.multiLineTransition) {
			int multiLineStart = transition.multiLineStart;
			int multiLineEnd = transition.multiLineEnd;
			addHighlightTransition(lineNum, multiLineStart, multiLineEnd);
		}
	}
	
	/**
	 * For a given state, this highlights all references to that state in the editor
	 * @param stateName - ...
	 * @param stateReferences - all state references of that state - i.e any mention of it in the editor. 
	 */
	protected void displayHighlights(String stateName, StateReference stateReference, ArrayList<StateReference> stateReferences) throws CoreException {
		int lineNum = stateReference.lineNum;
		
		if (stateReference.isTransition) {
			Transition transition = stateReference.transition;

			int charStart = transition.rightCharStart;
			int charEnd = transition.rightCharEnd;
			addHighlightState(lineNum, charStart, charEnd);
			
			int multiLineStart = transition.multiLineStart;
			int multiLineEnd = transition.multiLineEnd;
			addHighlightTransition(lineNum, multiLineStart, multiLineEnd);
		} else {
			int charStart = stateReference.charStart;
			int charEnd = stateReference.charEnd;
			addHighlightState(lineNum, charStart, charEnd);
		}
		

		if (stateReferences != null) {
			String event = stateReference.editorLine;
			String negateEvent = negateCondition(event);
			for (StateReference aStateReference : stateReferences) {
				if (!aStateReference.theLine.contains(event) || aStateReference.theLine.contains(negateEvent)) continue;
				lineNum = aStateReference.lineNum;
				String theLine = aStateReference.theLine;
		
				//Color the transitions on the diagram in the same colors you colored the transitions in the editor
				String colorTransition = forwardTransitionLink(aStateReference.theLine);
				if (lineNum == 0) {
					appendTextualDiagram(stateName, aStateReference.theLine + "\n");
		
				} else {
					appendTextualDiagram(stateName, backwardTransitionLink(colorTransition, lineNum, aStateReference.theLine) + "\n");
				}
				stateDiagram.addedTransitions.add(theLine);
				stateDiagram.colorCounter++;
		}
				
			

		}					
	}
	
	//Highlights states in the editor
	protected void addHighlightState(int lineNum, int charStart, int charEnd) throws CoreException {
		
		IMarker marker = stateDiagram.root.createMarker("FSM.State.Highlight");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
        marker.setAttribute(IMarker.CHAR_START,charStart);
        marker.setAttribute(IMarker.CHAR_END,charEnd);
	}
	
	//Highlights transitions in the editor
	protected void addHighlightTransition(int lineNum, int multiLineStart, int multiLineEnd) throws CoreException {
		
		IMarker marker = stateDiagram.root.createMarker("FSM.Transition.Highlight_1");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, stateDiagram.path.toString());
        marker.setAttribute(IMarker.CHAR_START,multiLineStart);
        marker.setAttribute(IMarker.CHAR_END,multiLineEnd);
	}
	
	
	//Clears all of the highlights in the editor
	protected void removeHighlights(IResource resource) {
		try {
			
			IMarker[] markers = resource.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
			for (int j=0; j<allTransitionHighlights.length; j++) {
				markers = resource.findMarkers(allTransitionHighlights[j], true, IResource.DEPTH_INFINITE);
				for (IMarker m : markers) {
					m.delete();
				}
			}
			
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
	}
	

	
	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		