package plantuml.statemachine.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import plantuml.statemachine.generation.StateDiagram;
import plantuml.statemachine.generation.PatternIdentifier.RegexInfo;
import utils.Action;
import utils.Event;
import utils.Node;
import utils.PendingState;
import utils.StateReference;
import utils.StateTree;
import utils.Transition;
import utils.StateTree.TransitionInformation;

public class StateMachineGenerator extends StateTextDiagramHelper {

	
	final String prefix = "@start_OSM_generation", prefixRegex = prefix;
	final String suffix = "@end_OSM_generation", suffixRegex = suffix;
	private PatternIdentifier patternIdentifier = new PatternIdentifier();
 	
 	public StateMachineGenerator(StateDiagram stateDiagram, String stateSelected, int selectedLineNum, StringBuilder result) {
 		this.stateDiagram = stateDiagram;
 		this.stateSelected = stateSelected;
 		this.selectedLineNum = selectedLineNum;		
	}
 	
 	public StateMachineGenerator() {
 		
 	}
 	
 	//LINKING DIAGRAM TO TEXT VARIABLES//////
 	
 	StateDiagram stateDiagram;
 	int selectedLineNum;
	String stateSelected;
	protected HashSet<String> plantMarkerKey = new HashSet<String>();
	StateTextDiagramHelper stateTextDiagramHelper;
	StateDiagram stateDiagram2;
	StateMachineGenerator osmGenerator;
	
	ArrayList<String> methodCalls;
	ArrayList<String> declaredMethods;
	ArrayList<String> exitConditions;
	ArrayList<Node> exitStates;
	ArrayList<Node> conditionalBlock;
	ArrayList<String> ignoreArray;
	ArrayList<String> addedStates;
	Stack<String> currentBlock;
	Stack<Event> events;
	Stack<PendingState> pendingStates;
	int selStart;
	
	String whileStateName;
	Stack<Node> stateFound;
	Stack<String> ignoreStack;
	StringBuilder result;
	StateTree theTree;
	StateTree storeTree;
	
	boolean ignore = false;
	boolean stopIgnoring = false;
	boolean unConditionalState = false;
	boolean drawTree = false;
	boolean selfLoop = false;
	boolean afterLoopState = false;
	boolean nextLineConditionalValidate = false;
	boolean oneLineConditional = false;
	boolean certainEvent = true;
	
	String initialState = null;
	
	
	
	
	private void identifyPattern(String line, int lineNum, int startOfRegion, int selectionStart) throws BadLocationException, CoreException {
		
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
			if (!currentBlock.empty()) currentBlock.pop();
			if (!events.empty()) events.pop();
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
				String removeFSM = StateTextDiagramHelper.stateDescriptor(line);
				if (removeFSM.equals("null")) return;
				if (removeFSM.contains("EXIT")) {
					removeFSM = removeFSM.replaceAll("\\s*(EXIT)\\s*-\\s*", "");
					exitConditions = new ArrayList<String>(Arrays.asList(removeFSM.split("/")));
				} else if (removeFSM.contains("REMOVE")) {
					removeFSM = removeFSM.replaceAll("\\s*(REMOVE)\\s*-\\s*", "");
					ignoreArray.add(removeFSM.trim());
				} else {
				
					
					if (line.contains("{")) {
						PendingState pendingState = new PendingState(removeFSM, line, lineNum);
						pendingStates.push(pendingState);
					}
					else if (line.contains("}") && pendingStates.size() > 0) {
						PendingState pendingState = pendingStates.pop();
						stateTextDiagramHelper.appendToLists(pendingState.theLine, pendingState.editorLine, pendingState.lineNum, lineNum, startOfRegion);
					} else if (line.contains("}")) { //no opening '{' so ignore - user error
						return;
					} else {
						stateTextDiagramHelper.appendToLists(removeFSM, line, lineNum, -1, startOfRegion);
					}
					stateTextDiagramHelper.instantiateTransitionStateMap();
					stateTextDiagramHelper.isStateSelected(); //if selectedLineNum == lineNum of a state then highlight all references to state
					stateTextDiagramHelper.appendTransitionStates(selStart);
					stateTextDiagramHelper.appendStateAndTransitions();
					System.out.println(removeFSM);
					
					

				}
				
			}
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
					 
				 } else if (!currentBlock.empty() && currentBlock.peek().equals("state-conditional")) {
					 conditionalBlock.add(stateFound.peek());
				 }
				 
				
				
				 certainEvent = true;
				 if (!m.group(3).equals("{")) { //check to see if the bracket is on the next line...
					 nextLineConditionalValidate = true;
				 } 
				 if (m.group(1).contains("}") && !currentBlock.empty()) {
					 switch(currentBlock.peek()) {
						case "state-conditional":
							certainEvent = false;
							stateFound.pop();
							break;
						case "conditional":
							methodCalls.clear();
							break;
					 }
					 currentBlock.pop();
				 }
				 
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
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				 
				event = new Event("unconditional", line, charStart, charEnd, lineNum);
	
				
				
				if (!currentBlock.empty() && currentBlock.peek().equals("state-conditional"))
					conditionalBlock.add(stateFound.peek());
				if (m.group(1).contains("}") && !currentBlock.empty()) {
					
					switch(currentBlock.peek()) {
					
						case "state-conditional":
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
					Node node = new Node(initialState, line,  true, charStart, charEnd, lineNum, new Event(""));
					theTree = new StateTree(node);
					stateFound.push(node);
					result.append("[*] -> " + initialState);
					result.append("\n");
	
					break;
					
				} else if (afterLoopState) {
					certainEvent = true;
					result.append(whileStateName + " -> " + m.group(4) + " : Exit loop" + "\n");
					Node node = new Node(m.group(4), line, true, charStart, charEnd, lineNum, new Event(""));
					theTree = new StateTree(node);
					stateFound.push(node);
					afterLoopState = false;
	
					break;
				}
				
				if (unConditionalState) {
					//IF THIS IS VISIBLE
					if (stateFound.peek().visible) {
						String lastStateName = stateFound.peek().stateName;
						Node newRoot = new Node(lastStateName, line, true, stateFound.peek().charStart, stateFound.peek().charEnd, stateFound.peek().lineNum, new Event(""));
	
						buildStateTree(false);
						appendStateAndTransitions();
						appendPlantUML();
						result.append(stateDiagramAsString());
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
					Node node = new Node(m.group(4), line, false, charStart, charEnd, lineNum, new Event("unconditional"));   
					theTree.addNode(theTree.root, node);
					unConditionalState = true;
					stateFound.push(node);
					
				} else if (!stateFound.empty() && !currentBlock.empty()) {
					
					
					if (currentBlock.peek().equals("conditional")) { 
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("state-conditional"); //therefore speicify this
						Node node = new Node(m.group(4), line, false, charStart, charEnd, lineNum, events.peek());   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
						
					} else if (currentBlock.peek().equals("else-conditional")) {
						currentBlock.pop(); //we know the conditional is for a state 
						currentBlock.push("else-state-conditional"); //therefore speicify this
						Node node = new Node(m.group(4), line, false, charStart, charEnd, lineNum, new Event("unconditional"));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
					} else if (currentBlock.peek().equals("state-conditional")) {
						
						Node node = new Node(m.group(4), line, false, charStart, charEnd, lineNum, new Event("unconditional"));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						
					}  else if (currentBlock.peek().equals("case-state") || currentBlock.peek().equals("while-loop") ) {
						Node node = new Node(m.group(4), line, false, charStart, charEnd, lineNum, new Event(""));   
						theTree.addNode(stateFound.peek(), node);
						stateFound.push(node);
						selfLoop = false;
						
					} 
				} else if (!currentBlock.empty() && currentBlock.peek().equals("while-loop") ) {
					result.append("[*] -> " + m.group(4) + "\n");
					Node node = new Node(m.group(4), line, true, charStart, charEnd, lineNum , new Event("")); 
					theTree = new StateTree(node);
					stateFound.push(node);
				} 
	
		
				break;
			case 5: //closed }
				if (!currentBlock.isEmpty()) {
					switch(currentBlock.peek()) {
						case "state-conditional":
							certainEvent = false;
							stateFound.pop();
							break;
						case "conditional":
							methodCalls.clear();
							break;
						case "else-state-conditional":
							if (stateFound.peek().visible) {
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
							appendPlantUML();
	
							result.append(stateTextDiagramHelper.stateDiagramAsString());
							stateTextDiagramHelper.stateDiagram.textualDiagram = new HashMap<String, LinkedHashSet<String>>();
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
				if (!stateFound.empty() && declaredMethods.contains(method)){
					
					for (Node descendant : theTree.getNodeAndAllDescendants(stateFound.peek())) {
						descendant.action.add(new Action(m.group(1), theTree.currentIndex-1));
					}
				}
				if (exitConditions!= null && exitConditions.contains(m.group(1)) && !stateFound.empty()) {
					selfLoop = false;
					String stateName = stateFound.peek().stateName;
					Node currentNode = stateFound.peek();
					result = result.append(stateName + " -down-> [*] : " + m.group(1) + "\n");
					StateReference theState = new StateReference(stateName, currentNode.editorLine, currentNode.lineNum, currentNode.charStart, currentNode.charEnd, false);
					appendToLists(theState, "");
				}
				
			
				
				break;
			case 8: //complexMethodcall
				
				break;
			case 9: //case 
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				if (!currentBlock.empty() && currentBlock.peek().equals("case-state")) currentBlock.pop(); //no break inbetween
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
				Node node = new Node(m.group(2), line, true, charStart, charEnd, lineNum, new Event(""));
				theTree = new StateTree(node);
				stateFound.push(node);
			
				
				
				break;
			case 10:  //break regex
				if (!currentBlock.empty()) {
					if (currentBlock.peek() == "conditional") {
						//probably dont need the .contains("case-state") above...
						ignore = true;
						ignoreStack.push("ignore");
						break;
						
					
					} else if (currentBlock.peek() == "case-state") {
						String caseName = stateFound.firstElement().stateName;
						Node caseNode = stateFound.firstElement();
						if (selfLoop) {
							StringBuilder transition = new StringBuilder();
	
							node = stateFound.firstElement();
							
							for (Node child : theTree.getChildren(node)) {
								transition.append(negateCondition(child.event.event));
							}
							
							
							
							if (transition.length() == 0) transition.append("No event found");
							else {
								transition.insert(0, '[');
								transition.append("]");
							}
							if (!caseNode.action.isEmpty()) {
								boolean toggle = true;
								for (Action action: caseNode.action) {
									if (action.index == caseNode.index) {
										if (toggle) {
											transition.append(" / ");
											toggle = false;
										}
										transition.append(action.action + " ; ");
									}
								}
							}
	
							String stateName = node.stateName;
							if(stateName.equals("INIT")) stateName = "[*]";
							StateReference theState = new StateReference(stateName, node.editorLine, node.lineNum, node.charStart, node.charEnd, false);
							
							if (!ignoreArray.contains(caseName + " -> " + caseName + " : " + transition)) {
								result.append(caseName + " -> " + caseName + " : " + transition + "\n");
	
							} 
							appendToLists(theState, "" );			
	
							
							
						}
						methodCalls.clear();
						buildStateTree(false);
						appendPlantUML();
	
						while(currentBlock.peek() != "case-state") currentBlock.pop(); 
						
						currentBlock.pop();
					}
					
					
				}
				
				break;
				
				
				
				
				
				
			case 11: //whileLoop
				
				currentBlock.push("while-loop");
				selfLoop = true;
				charStart = stateDiagram.document.getLineOffset(lineNum);
				charEnd = charStart + stateDiagram.document.getLineLength(lineNum);
				
				Node whileState = new Node(m.group(1), line, true, charStart, charEnd, lineNum, new Event("unconditional"));	
				whileStateName = m.group(1);
				
				addNodeBuildTree(whileState, theTree.root, false);
				appendStateAndTransitions();
				appendPlantUML();
	
				result.append(stateDiagramAsString());
				//stateFound.push(whileState);
	
	
				result.append("state " + m.group(1) + " { " + "\n"); 
				result.append("state \" WHILE LOOP:  " + m.group(1) + "\" as " + m.group(1) + "\n");
				break;
				
	//
	//
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
	

	
	//Couldnt think of a better way of getting if-else statesments to go around the else unconditional block...
	public void duplicateTransitions(Node from, Node to, StringBuilder transition) { 
		for (Node node : theTree.noLink.get(from)) {
			if (!node.equals(from)) {
				result.append(node.stateName + " -> " +  to.stateName + " : "  + transition + "\n");
			}
		}
	}
	
		

	private StringBuilder buildTransitionsFromTree(boolean lastTree) {
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
				
				if (transition.length() > 3) {
					transition.delete(transition.length()-3, transition.length());
					transition.insert(0, '[');
					transition.append("]");
				}
				if (!from.action.isEmpty()) {
					boolean toggle = true;
					for (Action action: from.action) {
						if (action.index < to.index) {
							if (toggle) {
								transition.append(" / ");
								toggle = false;
							}
							transition.append(action.action + " ; ");
						}
					}
				}
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
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////
				if (theTree.noLink.containsKey(from)) duplicateTransitions(from, to, transition);
				
				
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
		if (ignoreArray.contains(stateName)) return;
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
	
	
	private void appendPlantUML() throws CoreException {
		result.append(stateTextDiagramHelper.stateDiagramAsString());
		stateTextDiagramHelper.stateDiagram.clearStorage();
	}
	
	private void buildStateTree(boolean lastTree)  {
		result.append(buildTransitionsFromTree(lastTree));
		drawTree = false;
		stateFound.clear();
	}
	
	private void addNodeBuildTree(Node nodeToAdd, Node nodeToAddNodeTo, boolean lastTree) {
		theTree.addNode(nodeToAddNodeTo, nodeToAdd);
		buildStateTree(lastTree);

	}

	public StringBuilder getDiagramTextLines(IDocument document, final int selectionStart, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");
		IWorkbench workspace = PlatformUI.getWorkbench();

		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IResource root = StateTextDiagramHelper.getRoot(editorInput);
		
		selStart = selectionStart;
		
		
		conditionalBlock = new ArrayList<Node>();
		exitStates = new ArrayList<Node>();
		addedStates = new ArrayList<String>();
		pendingStates = new Stack<PendingState>();
		currentBlock = new Stack<String>();
		stateFound = new Stack<Node>();
		result = new StringBuilder();
		events = new Stack<Event>();
		ignoreStack = new Stack<String>();
		declaredMethods = new ArrayList<String>();
		ignoreArray = new ArrayList<String>();
		methodCalls = new ArrayList<String>();
		unConditionalState = false;
		drawTree =false;
		afterLoopState = false;
		nextLineConditionalValidate = false;
		oneLineConditional = false;
		selfLoop = false;
		exitConditions = new ArrayList<String>();
		initialState = null;
		//Initialize pattern store
		
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
					stateDiagram2 = new StateDiagram(finder, document, root, path);

					selectedLineNum = document.getLineOfOffset(selectionStart);
					stateSelected = "";
					stateTextDiagramHelper = new StateTextDiagramHelper(stateDiagram2, stateSelected, selectedLineNum, plantMarkerKey);

					
					
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();						
						identifyPattern(line, lineNum, startOffset, selectionStart);
					}
					
					
					if (drawTree) {
						addNodeBuildTree( new Node("[*]", "", true, 0, 0, -1, new Event("")), theTree.root, true);
						appendStateAndTransitions();
						appendPlantUML();

						result.append(stateDiagramAsString());

					}
					

					
				}
					
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
		stateDiagram.textualDiagram.clear();

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
							appendTextualDiagram(stateDiagram, stateName, backwardStateLink(stateDiagram, stateName, lineNum) + "\n");
							appendTextualDiagram(stateDiagram, stateName, colorState + "\n"); 
						}
						
						if (!stateReference.isPlantUML ) {
							displayHighlights(stateName, stateReference, null);
						}
							
								
						
						
					} else { //create the link from the diagram to the editor
						String backStateLink = backwardStateLink(stateDiagram, stateName, lineNum);
						if (!stateName.equals("[*]") && !addedStates.contains(backStateLink)) {
							addedStates.add(backStateLink);
							appendTextualDiagram(stateDiagram, stateName, backStateLink + "\n");
						}
					}
				}
				
				else  {
					//TRANSITION							
				
					//transitionDone ?
					if (stateDiagram.addedTransitions.contains(line)) continue;

					if (selectedLineNum == lineNum && lineNum > 0) { //color that transition lime and create a link back to line in editor
						
						String colorTransition = forwardTransitionLink(line);
					
						appendTextualDiagram(stateDiagram, stateName, backwardTransitionLink(stateDiagram, colorTransition, lineNum, line) + "\n");
						displayTransitionStateTransitionMarkers(stateReference);
						
					} else { //dont color transition but create the link back
						if (lineNum == 0) {
							appendTextualDiagram(stateDiagram, stateName, line + "\n");

						} else {
							appendTextualDiagram(stateDiagram,stateName, backwardTransitionLink(stateDiagram, line, lineNum, line) + "\n");
						}					
					}
					stateDiagram.addedTransitions.add(line);
				}
				

			}	
			
		}
	}
	
	//Loops through the textualDiagram map and constructs the diagramtext as a StringBuilder. This is what is returned from getDiagramTextLines()
	protected StringBuilder stateDiagramAsString() {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, LinkedHashSet<String>> entry : stateDiagram.textualDiagram.entrySet()) {
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
	public void createKey(StateReference stateReference) throws CoreException {
		
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
			return;
		}
		if (possibleChangedMarker(theLine, lineNum, charStart, charEnd)) { //returns true if the diagram text hasnt changed but its positions had. It updates the key + marker
			return;
		}
		//else brand new line so create new marker and key
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
				
			}
				
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
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
					appendTextualDiagram(stateDiagram, stateName, aStateReference.theLine + "\n");
		
				} else {
					appendTextualDiagram(stateDiagram, stateName, backwardTransitionLink(stateDiagram, colorTransition, lineNum, aStateReference.theLine) + "\n");
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

}

	
		