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

public class OSMGenerator {
	
	final String prefix = "@start_OSM_generation", prefixRegex = prefix;
	final String suffix = "@end_OSM_generation", suffixRegex = suffix;
	
	private boolean patternsInitialized = false;
	private PatternIdentifier patternIdentifier;
	
	Pattern ifGuard = Pattern.compile( 	"(if|\\}*else\\sif)\\s*\\(\\s*([a-zA-Z0-9=><*\\+\\-\\s]*)\\s*\\)\\s*(\\{)*\\s*([//]{2,}\\s*(.)*)*");
    Pattern goodIfGuard = Pattern.compile("(if|\\}?\\s*else\\sif)\\s*\\(\\s*([a-zA-Z0-9\\s\\[\\];|&.,()!=_\\-<>+*]*)\\s*\\)\\s*(\\{?)\\s*([//]{2,}\\s*(.)*)*");
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
    Pattern methodDeclerationException = Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) [\\w]*\\s[\\w]* *(\\{?|[^;])([//]{2,}\\s*(.)*)*");
	
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
	
	StringBuilder result;
	ArrayList<String> addedTransitions;
	ArrayList<String> storedEvents;
	
	Stack<String> currentBlock;
	Stack<String> events;
	Stack<String> visibleStates;
	String lastState = null;
	String caseName = null;
	//conditional
	//switch-state
	//case-state
	
	boolean callBack = false;
	boolean ignore = false;
	boolean stopIgnoring = false;
	
	
	Stack<String> ignoreStack;
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
		System.out.println("STORED EVENTS .SIZE: " + storedEvents.size());
		System.out.println("VISIBLE STATES .SIZE" + visibleStates.size());
		
		for (RegexInfo info : patternIdentifier.patternStore) {
			Matcher m = info.pattern.matcher(line);
			if (m.matches()) {
				System.out.println("VISIBLE STATES .SIZE" + visibleStates.size());
				System.out.println(lastState);
				switch(info.identifier) {
				
				case 0: //call back...
					//remove obvious once that arnt there.
					System.out.println("found a callback: " + line);
					if (line.contains("Systen.out.print")) break;
					if (!visibleStates.contains(lastState) && lastState != null) {
						System.out.println(lastState);
						visibleStates.push(lastState);
						events.push("call-back");
						
					}
					break;
				case 1: //complex if guard
					 System.out.println("good if guard " + line + "statement: " + m.group(2)) ;
					currentBlock.push("conditional");
					events.push(m.group(2)); //the condition
					
					
					break;
				case 2: //switch state
					currentBlock.push("switch-state");
					break;
				case 3: //state change
					System.out.println("state Change: " + line + "the State: " + m.group(4));
					StringBuilder transition = new StringBuilder();

					if (lastState == null) {
						lastState = m.group(4);
						result.append("state " + lastState);
						result.append("\n");
					} 
					
					if (currentBlock.size() > 1 && currentBlock.peek() == "conditional" && currentBlock.get(currentBlock.size()-2) != "conditional")
						storedEvents.add(negateCondition(events.peek()));
					if (!visibleStates.isEmpty()) {
						for (int j=events.size()-1; j>=0; j--) {
							if (events.get(j).equals("call-back")) {
								System.out.println("callback = true");
								callBack =true;
								break;
							
							} 
							transition.append(events.get(j));
							if (j>0) transition.append("; ");

						}	
						if (transition.length() == 0) transition.append("No event found");
						if (callBack) {
							System.out.println("appending transition " + visibleStates.peek() + " -down-> " + m.group(4) + " : " + transition);
							result.append(visibleStates.peek() + " -down-> " + m.group(4) + " : " + transition);
							result.append("\n");
						} else {
							for (String visibleState: visibleStates) {
								System.out.println("appending transition " + visibleState + " -down-> " + m.group(4) + " : " + transition);
								result.append(visibleState + " -down-> " + m.group(4) + " : " + transition);
								result.append("\n");

							}
						}
						
						
					}
					callBack = false;

					lastState = m.group(4);

					

					
					break;
				case 4: //closed }
					
					if (!currentBlock.isEmpty()) currentBlock.pop();
					
					if (!events.isEmpty()) {
						if (events.size() >1 && events.peek() == "call-back") {
							events.pop();
							events.pop();
						} else {
							events.pop();
						}
				
					}
					
					
					break;
				case 5: //decleration
					System.out.println("decleration: " + line);
					
					break;
				case 6: //simpleMethodCal
					System.out.println("simple method call: " + line + "method: " + m.group(1));
					
					break;
				case 7: //complexMethodcall
					System.out.println("complex method call: " + line + "method: " + m.group(3));
					
					break;
				case 8: //case 
					if (currentBlock.contains("switch-state")) {
						visibleStates.clear();
						currentBlock.push("case-state"); //we know that the case is a state...
						storedEvents.clear();
						if (m.group(2).equals("INIT")) {
							visibleStates.push("[*]");
						}
						else {
							visibleStates.push(m.group(2));
							result.append("state " + m.group(2));
							result.append("\n");
							lastState = m.group(2);
						}
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
						if (currentBlock.contains("while-loop") && !caseName.equals("INIT")) {
							transition = new StringBuilder();
							for (int j =0; j<storedEvents.size(); j++) {
								transition.append(storedEvents.get(j));
								if (j != storedEvents.size() -1) transition.append(" && ");
							}
							if (transition.length() == 0) transition.append("No event found");
							System.out.println("here :" + caseName);
							result.append(caseName + " -> " + caseName + " : " + transition);
							result.append("\n");
						}
						visibleStates.clear();
						while(currentBlock.peek() != "case-state") currentBlock.pop(); 
						currentBlock.pop();
					}
					
					break;
				case 10: //whileLoop
					currentBlock.push("while-loop");
				case 11: //methodDecleration
					System.out.println("method decleration: " + line);
					
					break;
				case 12: //method decleration exception
					System.out.println("method dec with exception: " + line);
					
					break;
				default: 
					System.out.println("no match found");
				}
			} else if (m.find() && info.identifier == 13) { //fsm comment
				System.out.println("fsm comment " + line);
				result.append(line.substring(6));
				result.append("\n");
			}
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
		

		
		
		addedTransitions = new ArrayList<String>();
		currentBlock = new Stack<String>();
		visibleStates = new Stack<String>();
		result = new StringBuilder();
		events = new Stack<String>();
		ignoreStack = new Stack<String>();
		storedEvents = new ArrayList<String>();
		lastState = null;
		caseName = null;
		//Initialize pattern store
		if(!patternsInitialized) {
			initializePatterns();
			patternsInitialized = true;

		}
			
		
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
