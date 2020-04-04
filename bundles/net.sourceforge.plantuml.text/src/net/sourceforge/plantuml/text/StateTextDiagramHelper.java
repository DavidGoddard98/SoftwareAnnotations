package net.sourceforge.plantuml.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

public class StateTextDiagramHelper  {
	
	final String prefix = "@start_state_machine", prefixRegex = prefix;
	final String suffix = "@end_state_machine", suffixRegex = suffix;
	int selectedLineNum;
	String stateSelected;
	
	private boolean toggle = true;
	private static HashSet<String> plantMarkerKey = new HashSet<String>();


	
	public StateTextDiagramHelper() {
	
	}
	
	class PendingState extends StateTextDiagramHelper {
		String theLine;
		String editorLine;
		int lineNum;
		
		PendingState(String theLine, String editorLine, int lineNum) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
		}
	}
	
	class StateReference extends StateTextDiagramHelper {
		String theLine;
		String editorLine;
		int lineNum;
		int charStart;
		int charEnd;
		boolean isTransition;
		Transition transition;
		
		StateReference(String theLine, String editorLine, int lineNum, int charStart, int charEnd) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
			this.charStart = charStart;
			this.charEnd = charEnd;
			this.isTransition = false;
		}
		
		StateReference(String theLine, String editorLine, int lineNum, Transition transition) {
			this.theLine = theLine;
			this.editorLine = editorLine;
			this.lineNum = lineNum;
			this.isTransition = true;
			this.transition = transition;
		}
		
		StateReference() {
			
		}
		
		public String toString() {
			return this.theLine;
		}
	}
	
	class Transition extends StateTextDiagramHelper {
		String leftState;
		int leftCharStart;
		int leftCharEnd;
	
		String rightState;
		int rightCharStart;
		int rightCharEnd;
		
		int multiLineStart;
		int multiLineEnd;
		boolean multiLineTransition;
		
		Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd) {
			this.leftState = leftState;
			this.rightState = rightState;
			this.leftCharStart = leftCharStart;
			this.leftCharEnd = leftCharEnd;
			this.rightCharStart = rightCharStart;
			this.rightCharEnd = rightCharEnd;
			this.multiLineTransition = false;
		}
		
		Transition(String leftState, String rightState, int leftCharStart, int leftCharEnd, int rightCharStart, int rightCharEnd, int multiLineStart, int multiLineEnd) {
			this.leftState = leftState;
			this.rightState = rightState;
			this.leftCharStart = leftCharStart;
			this.leftCharEnd = leftCharEnd;
			this.rightCharStart = rightCharStart;
			this.rightCharEnd = rightCharEnd;
			this.multiLineStart = multiLineStart;
			this.multiLineEnd = multiLineEnd;
			this.multiLineTransition = true;
		}
		
		
		Transition() {
			
		}
		
		public String toString() {
			return this.leftState + " -> " + this.rightState;
		}
		
		
	}
	
	//Check whether line is a descriptor or not
	private static String stateDescriptor(String line) {
	    String theLine = line.replaceAll("\\s+", "").toLowerCase();
	    if (theLine.length() <= 6) 
	    	return null;
	    if (theLine.substring(0,6).equals("//fsm:")) {
	      int index = line.indexOf(":") + 1;
	      if (line.contains("->") || line.contains("<-")) {
	    	  if (line.contains("{")) {
	    		  int anotherIndex = line.indexOf("{");
	    		  return line.substring(index, anotherIndex);
	    	  }
	      }
	      return line.substring(index, line.length()).trim();
	    }
	    return null;
	  }
	
	
	private void appendToLists(String line, String editorLine, int lineNum, int multiLineEnd, ArrayList<String> actualStates,
			HashMap<String, ArrayList<StateReference>> stateLinkers, FindReplaceDocumentAdapter finder, IDocument document) throws BadLocationException {
		////////////////////
		IRegion markerRegion = finder.find(0, line, true, true, false, false);
		///////////////////
		int charStart;
		int charEnd;
		int index;
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();

		//TRANSITIONS
		if (line.contains("->") || line.contains("<-")) {
			String leftState = "" ;
			String rightState = "";
			Transition transition = new Transition();
			StateReference stateReference = new StateReference();
			
			if (line.contains("->") ) {
				
				index = line.indexOf("-") - 1;
				leftState = line.substring(0, index).trim();
				
				int leftCharStart = markerRegion.getOffset();
				int leftCharEnd = markerRegion.getOffset() + leftState.length();
				
				index = line.indexOf(">") + 1;
				int anotherIndex = line.length();
				if (line.contains(":"))
					anotherIndex = line.indexOf(":") - 1;
				rightState = line.substring(index, anotherIndex).trim();
				int rightCharStart = markerRegion.getOffset() + index + 1;
				int rightCharEnd = markerRegion.getOffset() + anotherIndex;
				
				if (multiLineEnd == -1) {
					transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd);

				} else {
					leftCharStart ++;
					leftCharEnd ++;
					transition = new Transition(leftState, rightState, leftCharStart, leftCharEnd, rightCharStart, rightCharEnd, rightCharEnd, document.getLineOffset(multiLineEnd));
				}
				stateReference = new StateReference(line, editorLine, lineNum, transition);
				
				
			} else if (line.contains("<-")) {
				//
			}
			
			//ADD THE TRANSITION'S STATE REFERENCES

			if (stateLinkers.containsKey(leftState)) {
				stateReferences = stateLinkers.get(leftState);
				stateReferences.add(stateReference);
				stateLinkers.put(leftState, stateReferences);
				
			} else {

				stateReferences.add(stateReference);
				stateLinkers.put(leftState, stateReferences);
			}
			stateReferences = new ArrayList<StateReference>();

			if (stateLinkers.containsKey(rightState)) {

				stateReferences = stateLinkers.get(rightState);
				stateReferences.add(stateReference);
				stateLinkers.put(rightState, stateReferences);
			} else {

				stateReferences.add(stateReference);
				stateLinkers.put(rightState, stateReferences);
			}
	
		} else if (line.contains(":") || line.trim().substring(0, 5).equals("state")) {
			//STATES
			String stateName = "";
			StateReference stateReference = new StateReference();
		
			if (line.trim().substring(0, 5).equals("state")) {
			
				line = line.trim().substring(6);
				
				if (line.contains(" ")) {
					
					index = line.indexOf(" ");
					stateName = line.substring(0, index);
				} else {
					stateName = line;
				}
				
				charStart = markerRegion.getOffset();
				if (multiLineEnd == -1) {
					charEnd = markerRegion.getOffset() + markerRegion.getLength();

				} else {
					charEnd = document.getLineOffset(multiLineEnd);
				}
				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
			} else if (line.contains(":")) {
				index = line.indexOf(":") - 1;
				stateName = line.substring(0, index).trim();
				charStart = markerRegion.getOffset();
				if (multiLineEnd == -1) {
					charEnd = markerRegion.getOffset() + markerRegion.getLength();

				} else {
					charEnd = document.getLineOffset(multiLineEnd);
				}				
				stateReference = new StateReference(line, editorLine, lineNum, charStart, charEnd);
				
				
				
			}

			if (stateLinkers.containsKey(stateName)) {
				stateReferences = stateLinkers.get(stateName);
				stateReferences.add(stateReference);
				stateLinkers.put(stateName, stateReferences);
				
			} else {
				stateReferences.add(stateReference);
				stateLinkers.put(stateName, stateReferences);
			}
			if (lineNum == selectedLineNum) stateSelected = stateName;
			actualStates.add(stateName);

		}
			
	}
	
	private void displayTransitionMarkers(StateReference stateReference, IPath path, IResource root) throws CoreException {
		int lineNum = stateReference.lineNum;
		Transition transition = stateReference.transition;
		int charStart = transition.leftCharStart;
		int charEnd = transition.rightCharEnd;
		if (transition.multiLineTransition) {
			int multiLineStart = transition.multiLineStart;
			int multiLineEnd = transition.multiLineEnd;
			addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
		}
		addHighlightTask(lineNum, charStart, charEnd, root, path);
	}


	private String displayTransitionStateMarkers(int lineNum, IResource root, IPath path, IDocument document, int selectionStart, 
			HashMap<String, ArrayList<StateReference>> stateLinkers) throws CoreException, BadLocationException { 
		boolean test = true;
		StringBuilder word = new StringBuilder();
		int saveSelection = selectionStart;
		while (test) {
			char c = document.getChar(saveSelection);
			if (c == ' ' || c == '\n' || c == '\r') {
				break;
			}
			word.append(c);
			
			saveSelection ++;
			
		}
		String wordString = word.toString();
		
		//if here need to check if word is a transitionState...
		ArrayList<StateReference> stateReferences = stateLinkers.get(wordString);
		
		if (stateReferences == null) return null;
		for (StateReference stateReference : stateReferences) {
			if (!stateReference.isTransition) return null;
		}
		
		int charStart = 0;
	    int charEnd = 0;
	    
		for (StateReference stateReference : stateReferences) {
			Transition transition = stateReference.transition;
			String leftState = transition.leftState;
			String rightState = transition.rightState;
			if (leftState.equals(wordString)) {
				charStart = transition.leftCharStart;
				charEnd = transition.leftCharEnd;
				if (transition.multiLineTransition) {
					int multiLineStart = transition.multiLineStart;
					int multiLineEnd = transition.multiLineEnd;
					addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
				}
			}
			if (rightState.equals(wordString)) {
				charStart = transition.rightCharStart;
				charEnd = transition.rightCharEnd;
				if (transition.multiLineTransition) {
					int multiLineStart = transition.multiLineStart;
					int multiLineEnd = transition.multiLineEnd;
					addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
				}
			}
			addHighlightTask(lineNum, charStart, charEnd, root, path);

		}
		return wordString;
	}
	
	
	public StringBuilder getDiagramTextLines(final IDocument document, final int selectionStart,
			final Map<String, Object> markerAttributes, IEditorInput editorInput) {
		final boolean includeStart = prefix.startsWith("@"), includeEnd = suffix.startsWith("@");
		final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(document);
		IPath path = ((IFileEditorInput) editorInput).getFile().getFullPath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wsRoot = workspace.getRoot();
		IResource root = wsRoot.findMember(path);
		try {

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
					StringBuilder result = new StringBuilder();
					final int maxLine = Math.min(document.getLineOfOffset(endOffset) + (includeEnd ? 1 : 0),
							document.getNumberOfLines());
//					System.out.println();
//					System.out.println();
//					int i = 0;
//					for (String key : plantMarkerKey) {
//						System.out.println(i + key);
//						i ++;
//					}
//					System.out.println("key size: " + plantMarkerKey.size());
					IMarker[] allMarkers = root.findMarkers("FSM.MARKER", false, IResource.DEPTH_ZERO);
					int i = 0;
					for (IMarker marker : allMarkers) {
						System.out.println(marker.getAttribute(IMarker.MESSAGE));

						i ++;
					}
					System.out.println("markerSize: " + allMarkers.length);
					selectedLineNum = document.getLineOfOffset(selectionStart);
					
					Stack<PendingState> pendingStates = new Stack<PendingState>();
					stateSelected = null;
					HashMap<String, ArrayList<StateReference>> stateLinkers = new HashMap<String, ArrayList<StateReference>> (); 
				    ArrayList<String> actualStates = new ArrayList<String>();
				  
				    
				    
		
				 
					for (int lineNum = startLine + (includeStart ? 0 : 1); lineNum < maxLine; lineNum++) {
						final String line = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum)).trim();
						String newLine = stateDescriptor(line);
						if (newLine != null){
							
							if (line.contains("}") && pendingStates.size() > 0) {
								PendingState pendingState = pendingStates.pop();
								appendToLists(pendingState.theLine, pendingState.editorLine, pendingState.lineNum, lineNum, actualStates, stateLinkers, finder, document);
							} else if (line.contains("{")) {
								PendingState pendingState = new PendingState(newLine, line, lineNum);
								pendingStates.push(pendingState);
							} else {
								appendToLists(newLine, line, lineNum, -1, actualStates, stateLinkers, finder, document);
							}
							
						}
					}
						
					
					

					String className = path.toFile().getName();
					
					removeHighlights(root);
				
					String stateName; 
					String line;
					int lineNum; 
					ArrayList<StateReference> stateReferences;
					ArrayList<String> addedTransitions = new ArrayList<String>();
					boolean displayedMarkers = false;
					String transitionStateName = "";
					boolean transitionStateSelected = false;

					for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {
						ArrayList<StateReference> addedTransitionRef = new ArrayList<StateReference>();


						stateName = entry.getKey();
						stateReferences = entry.getValue();
						boolean onlyTransitions = true;
						for (StateReference stateReference : stateReferences) {
							line = stateReference.theLine;
							lineNum = stateReference.lineNum;
							
							
							createKey(stateReference, className, path, document, root);
							
							if (stateReference.isTransition) {
								//TRANSITION
								//transitionDone ?
								addedTransitionRef.add(stateReference);
								if (addedTransitions.contains(line)) continue;
								if (selectedLineNum == lineNum) {
									transitionStateName = displayTransitionStateMarkers(lineNum, root, path, document, selectionStart, stateLinkers);
									if (transitionStateName == null) {
										String colorTransition = forwardTransitionLink(line);
										result.append(backwardTransitionLink(colorTransition, className, lineNum));
										displayTransitionMarkers(stateReference, path, root);
									} else {
										transitionStateSelected = true;
										result.append(backwardTransitionLink(line, className, lineNum));
									}
										
								} else
									result.append(backwardTransitionLink(line, className, lineNum));
								addedTransitions.add(line);
							}
							
							else {
								//STATE
								onlyTransitions = false;
								if (stateName.equals(stateSelected) ) {
									String colorState = forwardStateLink(stateName);
						    		
									result.append(line);
									result.append("\n");
									result.append(backwardStateLink(stateName, className, lineNum));
						    		result.append("\n");
									result.append(colorState);
									if (!displayedMarkers) {
										displayMarkers(stateName, stateLinkers, path, root);
										displayedMarkers = true;
									}
		
								} else {
									result.append(backwardStateLink(stateName, className, lineNum));
						    		result.append("\n");
									result.append(line);
								}
							}
							if (!line.endsWith("\n")) {
								result.append("\n");
							}
							
						}
					
						
						if (onlyTransitions) {
							lineNum = 0;
							for (StateReference addedTransition : addedTransitionRef) {
								String leftStateName = addedTransition.transition.leftState;
								String rightStateName = addedTransition.transition.rightState;
								String theLine = addedTransition.editorLine;
								int charStart, charEnd;
							    lineNum = addedTransition.lineNum;
								if (!actualStates.contains(leftStateName)) {
									charStart = addedTransition.transition.leftCharStart;
									charEnd = addedTransition.transition.leftCharEnd;
									StateReference leftStateRef = new StateReference(theLine, "transitionState" + leftStateName, lineNum, charStart, charEnd);
									result.append(backwardsTransitionStateLink(leftStateRef, leftStateName, className, path, document, root));
									result.append("\n");
								}		

								if (!actualStates.contains(rightStateName)) {

									charStart = addedTransition.transition.rightCharStart;
									charEnd = addedTransition.transition.rightCharEnd;
									StateReference rightStateRef = new StateReference(theLine, "transitionState" + rightStateName, lineNum, charStart, charEnd);
									result.append(backwardsTransitionStateLink(rightStateRef, rightStateName, className, path, document, root));
									result.append("\n ");

								}
			
							}
							if (transitionStateSelected) {
								result.append(forwardStateLink(transitionStateName));
								result.append("\n");
							}
							
							
						}
						
						
						
					}
					markerAttributes.put(IMarker.CHAR_START, start.getOffset());
					return result;
				}
			}

		} catch (final BadLocationException e) {
		} catch (CoreException e) {
			
		}
		return null;
	}
	

	

	private void createKey(StateReference stateReference, String className, IPath path, IDocument document, IResource root) {
		
		String theLine = stateReference.editorLine;
		int lineNum = stateReference.lineNum;
		
		int charStart;
		int charEnd;
		if (stateReference.isTransition) {
			charStart = stateReference.transition.leftCharStart + 1;
			charEnd = stateReference.transition.rightCharEnd;
		} else {
			charStart = stateReference.charStart;
			charEnd = stateReference.charEnd;
			
				
		}
		 

		String key = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) + String.valueOf(charEnd);
		if (plantMarkerKey.contains(key)) {
			System.out.println("Key exists");
			return;
		}
		if (possibleChangedMarker(theLine, lineNum, className, path, document, root, charStart, charEnd)) {
			System.out.println("marker changed");
			return;
		}
		//else brand new line so create new marker and key
		System.out.println("key doesnt exist, adding key");
		plantMarkerKey.add(key);
		addMarkerTask(theLine, lineNum, path, charStart, charEnd, root);
		
	}

	private static void addMarkerTask(String theLine, int lineNum, IPath path, int charStart, int charEnd, IResource root) {
		// use Platform.run to batch the marker creation and attribute setting
		Platform.run(new ISafeRunnable() {
			public void run() throws Exception {

				IMarker marker = root.createMarker("FSM.MARKER");
				marker.setAttribute(IMarker.MESSAGE, theLine);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
				marker.setAttribute(IMarker.SOURCE_ID, path.toString());
				marker.setAttribute(IMarker.CHAR_START, charStart);
				marker.setAttribute(IMarker.CHAR_END, charEnd);
			}
		});

	}
	
	private void addHighlightTask(int lineNum, int charStart, int charEnd, IResource root, IPath path) throws CoreException {
		
		IMarker marker = root.createMarker("FSM.State.Highlight");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, path.toString());
        marker.setAttribute(IMarker.CHAR_START,charStart);
        marker.setAttribute(IMarker.CHAR_END,charEnd);
	}
	
	private void addHighlightTransition(int lineNum, int multiLineStart, int multiLineEnd, IResource root, IPath path) throws CoreException {
		IMarker marker = root.createMarker("FSM.Transition.Highlight_1");
        marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
        marker.setAttribute(IMarker.SOURCE_ID, path.toString());
        marker.setAttribute(IMarker.CHAR_START,multiLineStart);
        marker.setAttribute(IMarker.CHAR_END,multiLineEnd);
        
	}

	private static boolean possibleChangedMarker(String theLine, int lineNum, String className, IPath iPath, IDocument document, IResource root, int charStart, int charEnd) {
		try {
			IMarker[] allMarkers;
		
			allMarkers = root.findMarkers("FSM.MARKER", false, IResource.DEPTH_ZERO);
			
			
			String path = iPath.toString();
			for (IMarker aMarker : allMarkers) {
				//Marker
				String markerMessage = (String)aMarker.getAttribute(IMarker.MESSAGE);
				
				if (theLine.contains("transitionState") && !theLine.equals(markerMessage)) continue;
				
				int markerLine = (int)aMarker.getAttribute(IMarker.LINE_NUMBER);
				
				String markerPath = (String)aMarker.getAttribute(IMarker.SOURCE_ID);
				String[] tmp = markerPath.split("/");
				String markerClassName = tmp[tmp.length-1];

				int markerCharStart = (int)aMarker.getAttribute(IMarker.CHAR_START);
				int markerCharEnd = (int)aMarker.getAttribute(IMarker.CHAR_END);
				
				String sameLineInDoc = document.get(document.getLineOffset(markerLine-1), document.getLineLength(markerLine-1)).trim();
				
				if (markerLine == (lineNum + 1) && markerPath.equals(path)) {
					if (!markerMessage.equals(theLine) || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);
						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, iPath, charStart, charEnd, root);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				if (markerPath.equals(path) && markerMessage.equals(theLine)) {
					if (markerLine != lineNum + 1 || markerCharStart != charStart || markerCharEnd != charEnd) {
						String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
						String newKey = className + theLine + String.valueOf(lineNum + 1) + String.valueOf(charStart) +  String.valueOf(charEnd);

						//delete old marker
						aMarker.delete();
						//and its key...
						plantMarkerKey.remove(oldKey);
						
						//create new marker
						addMarkerTask(theLine, lineNum, iPath, charStart, charEnd, root);
						// and its new key
						plantMarkerKey.add(newKey);
						return true;
					}
				}
				
				//line deleted - delete marker and key
				if (!sameLineInDoc.equals(markerMessage)) {
					String oldKey = markerClassName + markerMessage + String.valueOf(markerLine)  + String.valueOf(markerCharStart) + String.valueOf(markerCharEnd);
					
					//delete key
					plantMarkerKey.remove(oldKey);
					//and marker 
					aMarker.delete();
				}
				
			}
				
		} catch (CoreException e) {
			System.out.println("Failed to initialise keys");
		} catch (BadLocationException e) {
			System.out.println("Cant find same line in doc");
		}
		return false;

	}

	//Link from the editor to the diagram
	private String forwardStateLink(String stateName) {
		return "state " + stateName + " #Cyan";
	}

	//Link from the editor to the diagram
	private String forwardTransitionLink(String selectedLine) {
		String colorTransition = "";
		int indexOfArrow = selectedLine.indexOf("-");
		String theArrow = selectedLine.substring(indexOfArrow+1, indexOfArrow +2);
		colorTransition = selectedLine.substring(0, indexOfArrow) +  "-[thickness=5,#blue]" + theArrow + selectedLine.substring(indexOfArrow +2, selectedLine.length());
		return colorTransition;
	}

	private String backwardStateLink(String stateName, String className, int lineNum) {
		String link = "state " + stateName + "[["+className+"#FSM#state#"+ lineNum +"]]";
		return link;

	}

	private String backwardTransitionLink(String aLine, String className, int lineNum) {
		return aLine + " : " + "[["+className+"#FSM#transition#"+lineNum+"]]";

	}
	
	private String backwardsTransitionStateLink(StateReference stateReference, String stateName, String className, IPath path, IDocument document, IResource root) {
		createKey(stateReference, className, path, document, root);
		String link = "state " + stateName + "[["+className+"#FSM#"+stateReference.editorLine+"#"+stateReference.lineNum+"]]";
		return link;
	}
	
	private void displayMarkers(String stateName, HashMap<String, ArrayList<StateReference>> stateLinkers, IPath path , IResource root) throws CoreException {
		//
		ArrayList<StateReference> stateReferences = new ArrayList<StateReference>();

		int lineNum = 0;
		int charStart = 0;
		int charEnd = 0;
		for (Map.Entry<String, ArrayList<StateReference>> entry : stateLinkers.entrySet()) {
			String referenceStateName = entry.getKey();
			if (referenceStateName.equals(stateName)) {
				stateReferences = entry.getValue();
				
				for (StateReference stateReference : stateReferences) {
					lineNum = stateReference.lineNum;
					
					if (stateReference.isTransition) {
						if (stateReference.transition.leftState.contentEquals(stateName)) {
							charStart = stateReference.transition.leftCharStart;
							charEnd = stateReference.transition.leftCharEnd;
						} else {
							charStart = stateReference.transition.rightCharStart;
							charEnd = stateReference.transition.rightCharEnd;
						}
						
						if (stateReference.transition.multiLineTransition) {
							int multiLineStart = stateReference.transition.multiLineStart;
							int multiLineEnd = stateReference.transition.multiLineEnd;
							addHighlightTransition(lineNum, multiLineStart, multiLineEnd, root, path);
						}
						
					} else {
						charStart = stateReference.charStart;
						charEnd = stateReference.charEnd;
					}
					addHighlightTask(lineNum, charStart, charEnd, root, path);

			        
				}
			}
		}
	}
	//@start_state_machine
	//FSM: State.l1 : a
	//FSM: State1 -> State2
	
	//FSM: State3 -> state2
	
	//@end_state_machine
	
	/**
	 * @startuml
	 * State1 -> State2
	 * @enduml
	 
	 */
	 
	
	protected static void removeHighlights(IResource resource) {
		try {
			IMarker[] markers = resource.findMarkers("FSM.State.Highlight", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
			markers = resource.findMarkers("FSM.Transition.Highlight", true, IResource.DEPTH_INFINITE);
			for (IMarker m : markers) {
				m.delete();
			}
		} catch (CoreException e) {
			System.out.println("Couldnt remove highlights");
		}
		
	}

}
